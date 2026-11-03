/*
 * @test
 * @summary javax.swing.text.StyleContext.NamedStyle conformance: white-box structure + black-box behavior.
 * @run main NamedStyleTest
 */

import java.lang.reflect.Modifier;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleContext;

public class NamedStyleTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.StyleContext$NamedStyle");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.Style"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    static void testBlackBox() {
        StyleContext ctx = new StyleContext();

        // Unnamed default constructor.
        StyleContext.NamedStyle ns = ctx.new NamedStyle();
        check(ns.getName() == null, "unnamed style null name");
        check(ns.getAttributeCount() == 0, "unnamed style empty");
        check(ns.getResolveParent() == null, "unnamed style no parent");
        check(ns.toString().startsWith("NamedStyle:"), "toString prefix");

        // setName / getName.
        ns.setName("s1");
        check("s1".equals(ns.getName()), "setName/getName round trip");
        ns.setName(null);
        check("s1".equals(ns.getName()), "setName(null) is a no-op");

        // Name + parent constructor.
        StyleContext.NamedStyle parent = ctx.new NamedStyle();
        parent.addAttribute("p", "pv");
        StyleContext.NamedStyle child = ctx.new NamedStyle("child", parent);
        check("child".equals(child.getName()), "child name");
        check(child.getResolveParent() == parent, "child resolve parent");
        check("pv".equals(child.getAttribute("p")), "child inherits parent attribute");
        check(!child.isDefined("p"), "inherited attribute is not local");

        // Change listeners fire on mutation and can be removed.
        final int[] fired = {0};
        ChangeListener l = e -> fired[0]++;
        ns.addChangeListener(l);
        ns.addAttribute("k", "v");
        check(fired[0] == 1, "listener fired on add");
        check(ns.getChangeListeners().length == 1, "one change listener");
        ns.removeChangeListener(l);
        ns.addAttribute("k2", "v2");
        check(fired[0] == 1, "listener not fired after remove");
        check(ns.getChangeListeners().length == 0, "no change listeners after remove");

        // Attribute read path through the immutable backing set.
        check("v".equals(ns.getAttribute("k")), "attribute read back");
        check(ns.isDefined("k"), "attribute defined");
        ns.removeAttribute("k");
        check(!ns.isDefined("k"), "attribute removed");

        // copyAttributes returns an independent equivalent set.
        AttributeSet copy = ns.copyAttributes();
        check(copy != ns, "copyAttributes distinct instance");
        check(copy.isEqual(ns), "copyAttributes equivalent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
