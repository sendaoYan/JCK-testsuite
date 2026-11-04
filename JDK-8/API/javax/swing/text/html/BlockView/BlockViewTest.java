/*
 * @test
 * @summary javax.swing.text.html.BlockView conformance: axis contract,
 *          stylesheet attributes, span behavior before layout and invalid
 *          axis rejection (headless-safe, model layer only).
 * @run main BlockViewTest
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
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class BlockViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static boolean throwsIAE(Runnable r) {
        try { r.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
    }

    static HTMLEditorKit kit = new HTMLEditorKit();

    static HTMLDocument load(String html) throws Exception {
        HTMLDocument d = (HTMLDocument) kit.createDefaultDocument();
        kit.read(new StringReader(html), d, 0);
        return d;
    }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.BlockView", false, BlockViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPreferredSpan"), "method getPreferredSpan");
        check(names.contains("getMinimumSpan"), "method getMinimumSpan");
        check(names.contains("getMaximumSpan"), "method getMaximumSpan");
        check(names.contains("getResizeWeight"), "method getResizeWeight");
        check(names.contains("getAlignment"), "method getAlignment");
        check(names.contains("getAxis"), "method getAxis");
        check(names.contains("paint"), "method paint");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument doc = load("<html><body><div id='d' align='center'><p>Para one</p></div>"
                + "<p>Para two</p></body></html>");
        Element div = doc.getElement("d");
        check(div != null, "div element found");

        BlockView bv = new BlockView(div, View.Y_AXIS);
        check(bv.getElement() == div, "element identity");
        check(bv.getStartOffset() == div.getStartOffset(), "start offset");
        check(bv.getEndOffset() == div.getEndOffset(), "end offset");
        check(bv.getAxis() == View.Y_AXIS, "Y_AXIS stored");
        check(bv.getViewCount() == 0, "children not loaded before parenting");

        BlockView bx = new BlockView(div, View.X_AXIS);
        check(bx.getAxis() == View.X_AXIS, "X_AXIS stored");

        check(bv.getResizeWeight(View.X_AXIS) == 1, "resizeWeight X == 1");
        check(bv.getResizeWeight(View.Y_AXIS) == 0, "resizeWeight Y == 0");
        check(throwsIAE(() -> bv.getResizeWeight(5)), "resizeWeight(5) IAE");
        check(throwsIAE(() -> bv.getResizeWeight(-1)), "resizeWeight(-1) IAE");

        AttributeSet attrs = bv.getAttributes();
        check(attrs != null, "attributes non-null");
        check(bv.getAttributes() == attrs, "attributes instance stable");
        check(attrs.getAttributeCount() == 1, "one attribute (text-align) for align=center");
        check(attrs.isDefined(CSS.Attribute.TEXT_ALIGN), "text-align defined");
        check(String.valueOf(attrs.getAttribute(CSS.Attribute.TEXT_ALIGN)).contains("center"),
              "text-align value center");
        check(attrs.getAttribute(StyleConstants.NameAttribute) == null, "name attribute not exposed");

        // before layout: spans are zero, minimum does not exceed preferred
        bv.setSize(100f, 100f);
        check(bv.getPreferredSpan(View.X_AXIS) == 0.0f, "preferred X zero before layout");
        check(bv.getPreferredSpan(View.Y_AXIS) == 0.0f, "preferred Y zero before layout");
        check(bv.getMinimumSpan(View.Y_AXIS) <= bv.getPreferredSpan(View.Y_AXIS), "min <= pref");
        check(bv.getMaximumSpan(View.X_AXIS) >= bv.getMinimumSpan(View.X_AXIS), "max >= min");

        // a plain div has no text-align entry
        HTMLDocument plain = load("<html><body><div id='p'>x</div></body></html>");
        BlockView bp = new BlockView(plain.getElement("p"), View.Y_AXIS);
        check(bp.getAttributes().getAttributeCount() == 0, "plain div carries no css attributes");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
