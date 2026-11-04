/*
 * @test
 * @summary javax.swing.text.html.InlineView conformance: css attribute
 *          exposure for styled and plain runs, break weight contract and
 *          element identity (headless-safe, model layer only).
 * @run main InlineViewTest
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
import javax.swing.text.html.InlineView;

public class InlineViewTest {
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
        Class<?> c = Class.forName("javax.swing.text.html.InlineView", false, InlineViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.LabelView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("insertUpdate"), "method insertUpdate");
        check(names.contains("removeUpdate"), "method removeUpdate");
        check(names.contains("changedUpdate"), "method changedUpdate");
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("getBreakWeight"), "method getBreakWeight");
        check(names.contains("breakView"), "method breakView");
    }

    // ---- Part 2: model-layer behavior ----
    static void testModelLayer() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        HTMLDocument bdoc = load("<html><body><p><b>bold text</b> plain</p></body></html>");
        Element pEl = bdoc.getDefaultRootElement().getElement(1).getElement(0);
        Element boldRun = pEl.getElement(0);
        check("content".equals(boldRun.getName()), "bold run is a content element");

        InlineView iv = new InlineView(boldRun);
        check(iv.getElement() == boldRun, "element identity");
        check(iv.getStartOffset() == boldRun.getStartOffset(), "start offset");
        check(iv.getEndOffset() == boldRun.getEndOffset(), "end offset");
        AttributeSet attrs = iv.getAttributes();
        check(attrs != null, "attributes non-null");
        check(iv.getAttributes() == attrs, "attributes instance stable");
        check(attrs.getAttributeCount() == 3, "bold run carries three attributes");
        check(attrs.isDefined(CSS.Attribute.FONT_WEIGHT), "font-weight defined for <b>");
        check(String.valueOf(attrs.getAttribute(CSS.Attribute.FONT_WEIGHT)).contains("bold"),
              "font-weight value bold");

        HTMLDocument ndoc = load("<html><body><p>plain</p></body></html>");
        Element plainRun = ndoc.getDefaultRootElement().getElement(1).getElement(0).getElement(0);
        InlineView iv2 = new InlineView(plainRun);
        check(!iv2.getAttributes().isDefined(CSS.Attribute.FONT_WEIGHT),
              "no font-weight for unstyled text");

        // narrow widths are bad break locations
        check(iv.getBreakWeight(View.X_AXIS, 0f, 3f) == View.BadBreakWeight,
              "break weight bad for 3px request");
        check(iv.getBreakWeight(View.X_AXIS, 0f, 0f) == View.BadBreakWeight,
              "break weight bad for 0px request");

        // breakView yields a fragment view anchored at the requested offset
        View frag = iv.breakView(View.X_AXIS, 0, iv.getStartOffset(), 3f);
        check(frag != null, "breakView returns a fragment");
        check(frag.getEndOffset() >= frag.getStartOffset(), "fragment range ordered");
        check(frag.getEndOffset() - frag.getStartOffset() <= iv.getEndOffset() - iv.getStartOffset(),
              "fragment no longer than the original run");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testModelLayer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
