/*
 * @test
 * @summary javax.swing.text.Style conformance: white-box structure + black-box behavior.
 * @run main StyleTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.ChangeListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class StyleTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.Style");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.MutableAttributeSet"), "interface 0");
        check(c.getDeclaredMethods().length == 3, "three declared methods");
    }

    static void testBlackBox() {
        StyleContext ctx = new StyleContext();

        // A named style reports its name and behaves as a MutableAttributeSet.
        Style s = ctx.addStyle("mystyle", null);
        check("mystyle".equals(s.getName()), "named style name");
        check(s.getAttributeCount() == 1, "style holds its name attribute");
        s.addAttribute(StyleConstants.Bold, Boolean.TRUE);
        check(Boolean.TRUE.equals(s.getAttribute(StyleConstants.Bold)), "style attribute add/read");

        // Change listeners fire on mutation and can be removed.
        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        s.addChangeListener(l);
        s.addAttribute(StyleConstants.Italic, Boolean.TRUE);
        check(fired[0] == 1, "listener fired on add");
        s.removeChangeListener(l);
        s.addAttribute(StyleConstants.Underline, Boolean.TRUE);
        check(fired[0] == 1, "listener not fired after remove");

        // An unnamed style has a null name.
        Style unnamed = ctx.addStyle(null, null);
        check(unnamed.getName() == null, "unnamed style null name");

        // Parent/child attribute resolution through Style.
        Style parent = ctx.addStyle("parent", null);
        parent.addAttribute("pattr", "pv");
        Style child = ctx.addStyle("child", parent);
        check(child.getResolveParent() == parent, "child resolve parent");
        check("pv".equals(child.getAttribute("pattr")), "child inherits parent attribute");
        check(!child.isDefined("pattr"), "inherited attribute not local");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
