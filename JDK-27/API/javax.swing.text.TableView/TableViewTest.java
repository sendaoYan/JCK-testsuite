/*
 * @test
 * @summary javax.swing.text.TableView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TableViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.TableView;
import javax.swing.text.View;

public class TableViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class TestTableView extends TableView {
        TestTableView(Element e) { super(e); }
        TableView.TableRow makeRow() { return createTableRow(getElement()); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TableView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element root = ta.getDocument().getDefaultRootElement();
            TestTableView tv = new TestTableView(root);

            check(tv.getAxis() == View.Y_AXIS, "table tiles rows vertically");
            check(tv.getStartOffset() == 0 && tv.getEndOffset() == root.getEndOffset(), "offsets from element");

            TableView.TableRow row = tv.makeRow();
            check(row != null, "createTableRow returns a TableRow");
            check(row.getAxis() == View.X_AXIS, "table row tiles cells horizontally");

            check(tv.getViewCount() == 0, "no rows before replace");
            tv.replace(0, 0, new View[] { row });
            check(tv.getViewCount() == 1, "replace adds the row");
            check(tv.getView(0) == row, "getView returns the row");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(TableViewTest::testWhiteBox);
        runOnEDT(TableViewTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
