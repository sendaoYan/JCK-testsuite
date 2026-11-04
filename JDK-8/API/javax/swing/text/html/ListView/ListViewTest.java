/*
 * @test
 * @summary javax.swing.text.html.ListView conformance: list stylesheet
 *          attributes, marker type mapping and axis contract (headless-safe,
 *          model layer only).
 * @run main ListViewTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ListView;

public class ListViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.ListView", false, ListViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.BlockView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAlignment"), "method getAlignment");
        check(names.contains("paint"), "method paint");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument doc = load("<html><body><ul id='ul'><li>item one</li><li>item two</li></ul></body></html>");
        Element ul = doc.getElement("ul");
        check(ul != null, "ul element found");

        ListView lv = new ListView(ul);
        check(lv.getElement() == ul, "element identity");
        check(lv.getViewCount() == 0, "children not loaded before parenting");
        AttributeSet attrs = lv.getAttributes();
        check(attrs != null, "attributes non-null");
        check(attrs.getAttributeCount() == 6, "ul view carries six attributes (name + 5 css)");
        check(attrs.isDefined(CSS.Attribute.LIST_STYLE_TYPE), "list-style-type defined");
        check(String.valueOf(attrs.getAttribute(CSS.Attribute.LIST_STYLE_TYPE)).contains("disc"),
              "default marker is disc");
        check(attrs.isDefined(CSS.Attribute.MARGIN_TOP), "margin-top defined");
        check(attrs.isDefined(CSS.Attribute.MARGIN_BOTTOM), "margin-bottom defined");
        check(hasAttrName(attrs, "margin-left-ltr"), "margin-left-ltr defined");
        check(hasAttrName(attrs, "margin-right-rtl"), "margin-right-rtl defined");
        check(String.valueOf(attrs.getAttribute(CSS.Attribute.MARGIN_TOP)).contains("10"),
              "default list margins are 10");

        check(lv.getAlignment(View.Y_AXIS) == 0.5f, "alignment Y 0.5");
        check(lv.getResizeWeight(View.X_AXIS) == 1, "resizeWeight X == 1");
        check(lv.getResizeWeight(View.Y_AXIS) == 0, "resizeWeight Y == 0");

        // type attribute maps to list-style-type
        HTMLDocument sdoc = load("<html><body><ul type='square'><li>one</li></ul></body></html>");
        Element sul = sdoc.getDefaultRootElement().getElement(1).getElement(0);
        ListView slv = new ListView(sul);
        check(String.valueOf(slv.getAttributes().getAttribute(CSS.Attribute.LIST_STYLE_TYPE)).contains("square"),
              "type=square maps to list-style-type square");

        // ordered lists use decimal markers
        HTMLDocument odoc = load("<html><body><ol><li>one</li></ol></body></html>");
        Element ol = odoc.getDefaultRootElement().getElement(1).getElement(0);
        ListView olv = new ListView(ol);
        check(String.valueOf(olv.getAttributes().getAttribute(CSS.Attribute.LIST_STYLE_TYPE)).contains("decimal"),
              "ol default marker is decimal");
    }

    static boolean hasAttrName(AttributeSet s, String name) {
        for (java.util.Enumeration<?> e = s.getAttributeNames(); e.hasMoreElements(); ) {
            if (name.equals(String.valueOf(e.nextElement()))) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
