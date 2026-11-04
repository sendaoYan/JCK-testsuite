/*
 * @test
 * @summary javax.swing.text.html.TableView conformance (package-private):
 *          shape, factory-produced cell/row views and span queries on an
 *          unlayout table (headless-safe; reflection needs --add-opens on
 *          JDK-9+).
 * @run main TableViewTest
 */

import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class TableViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    static Element findByTag(HTMLDocument doc, HTML.Tag tag) {
        Deque<Element> q = new ArrayDeque<Element>();
        q.add(doc.getDefaultRootElement());
        while (!q.isEmpty()) {
            Element e = q.poll();
            if (e.getAttributes().getAttribute(StyleConstants.NameAttribute) == tag) return e;
            for (int i = 0; i < e.getElementCount(); i++) q.add(e.getElement(i));
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());

        // document first, then the package-private class, then the view:
        // this exact order keeps the unlayout size pool uninitialized
        HTMLDocument doc = load("<html><body><table border='1'>"
                + "<tr><td>a1</td><td>b1</td></tr><tr><td>a2</td><td>b2</td></tr>"
                + "</table></body></html>");
        Element table = findByTag(doc, HTML.Tag.TABLE);
        Element tr = findByTag(doc, HTML.Tag.TR);
        Element td = findByTag(doc, HTML.Tag.TD);
        check(table != null, "table element found");
        check(tr != null && tr.getParentElement() == table, "row element found under table");
        check(td != null && td.getParentElement() == tr, "cell element found under row");
        check(table.getElementCount() == 2 && tr.getElementCount() == 2,
              "element tree encodes a 2x2 grid");

        Class<?> c = Class.forName("javax.swing.text.html.TableView");
        check(c.getModifiers() == 0, "package-private class");
        check(c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass BoxView");
        boolean vf = false;
        for (Class<?> i : c.getInterfaces()) if (i == ViewFactory.class) vf = true;
        check(vf, "implements ViewFactory");

        Constructor<?> ctor = c.getConstructor(Element.class);
        ctor.setAccessible(true);
        View tv = (View) ctor.newInstance(table);
        check(tv.getElement() == table, "element identity");

        tv.setParent(null);
        Method rowCount = c.getMethod("getRowCount");
        rowCount.setAccessible(true);
        check(((Integer) rowCount.invoke(tv)) == 0, "getRowCount zero before layout");

        Method colCount = c.getMethod("getColumnCount");
        colCount.setAccessible(true);
        try {
            colCount.invoke(tv);
            check(false, "getColumnCount before layout should fail");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof NullPointerException,
                  "getColumnCount before layout NPE");
        }

        Method colSpan = c.getMethod("getColumnSpan", int.class);
        colSpan.setAccessible(true);
        try {
            colSpan.invoke(tv, 0);
            check(false, "getColumnSpan before layout should fail");
        } catch (InvocationTargetException e) {
            check(e.getCause() instanceof NullPointerException,
                  "getColumnSpan before layout NPE");
        }

        Method rowSpan = c.getMethod("getRowSpan", int.class);
        rowSpan.setAccessible(true);
        check(((Integer) rowSpan.invoke(tv, 0)) == 0, "getRowSpan(0) zero before layout");
        check(((Integer) rowSpan.invoke(tv, 9)) == 0, "getRowSpan(9) zero out of range");
        Method multi = c.getMethod("getMultiRowSpan", int.class, int.class);
        multi.setAccessible(true);
        check(((Integer) multi.invoke(tv, 0, 1)) == 0, "getMultiRowSpan zero before layout");

        // factory: rows become RowView, cells become CellView
        ViewFactory fac = (ViewFactory) tv;
        View cellView = fac.create(td);
        check(cellView != null && cellView.getClass().getName().endsWith("CellView"),
              "create(td) yields CellView");
        View rowView = fac.create(tr);
        check(rowView != null && rowView.getClass().getName().endsWith("RowView"),
              "create(tr) yields RowView");

        // detached from a container the resize weight is the neutral zero
        check(tv.getResizeWeight(View.X_AXIS) == 0, "getResizeWeight zero after detach");

        check(Class.forName("javax.swing.text.html.TableView$CellView") != null, "nested CellView exists");
        check(Class.forName("javax.swing.text.html.TableView$RowView") != null, "nested RowView exists");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
