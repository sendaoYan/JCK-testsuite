/*
 * @test
 * @summary javax.swing.text.html.ParagraphView conformance: attribute merging,
 *          visibility without a parent, alignment and break weight contract
 *          (headless-safe, model layer only).
 * @run main ParagraphViewTest
 */

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ParagraphView;

public class ParagraphViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.ParagraphView", false, ParagraphViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.ParagraphView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setParent"), "method setParent");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("isVisible"), "method isVisible");
        check(names.contains("paint"), "method paint");
        check(names.contains("getPreferredSpan"), "method getPreferredSpan");
        check(names.contains("getMinimumSpan"), "method getMinimumSpan");
        check(names.contains("getMaximumSpan"), "method getMaximumSpan");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument doc = load("<html><body><div align='center'><p id='p'>Para one</p></div></body></html>");
        Element para = doc.getElement("p");
        check(para != null, "paragraph element found");

        ParagraphView pv = new ParagraphView(para);
        check(pv.getElement() == para, "element identity");
        check(pv.getViewCount() == 0, "children not loaded before parenting");
        check(pv.getAttributes().getAttributeCount() == 2,
              "paragraph attributes carry name + resolved style: " + pv.getAttributes().getAttributeCount());

        // isVisible without a parent throws NPE (layout not established)
        boolean npe = false;
        try { pv.isVisible(); } catch (NullPointerException e) { npe = true; }
        check(npe, "isVisible before parenting NPE");

        check(pv.getAlignment(View.X_AXIS) == 0.5f, "alignment X 0.5");
        check(pv.getAlignment(View.Y_AXIS) == 0.5f, "alignment Y 0.5");

        BlockView host = new BlockView(para.getParentElement(), View.Y_AXIS);
        pv.setParent(host);
        check(pv.getParent() == host, "setParent round-trip");
        check(pv.getBreakWeight(View.X_AXIS, 0f, 10f) == View.GoodBreakWeight,
              "break weight good for a wide request");
        check(pv.getBreakWeight(View.X_AXIS, 0f, 0f) == View.BadBreakWeight,
              "break weight bad for a zero-width request");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
