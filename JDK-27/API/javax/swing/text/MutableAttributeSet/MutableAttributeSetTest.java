/*
 * @test
 * @summary javax.swing.text.MutableAttributeSet conformance: white-box structure + black-box behavior.
 * @run main MutableAttributeSetTest
 */

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;

public class MutableAttributeSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.MutableAttributeSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.text.AttributeSet"), "interface 0");
    }

    static void testBlackBox() {
        MutableAttributeSet m = new SimpleAttributeSet();
        check(m.getAttributeCount() == 0, "initial count 0");
        check(m.getResolveParent() == null, "initial resolve parent null");

        m.addAttribute("k1", "v1");
        check(m.getAttributeCount() == 1, "count 1 after add");
        check("v1".equals(m.getAttribute("k1")), "getAttribute reads added value");
        check(m.isDefined("k1"), "isDefined true");
        check(m.containsAttribute("k1", "v1"), "containsAttribute true");

        // Overwriting an existing key does not change the count.
        m.addAttribute("k1", "v1-new");
        check(m.getAttributeCount() == 1, "count unchanged after overwrite");
        check("v1-new".equals(m.getAttribute("k1")), "overwrite value");

        // addAttributes imports all name/value pairs.
        SimpleAttributeSet src = new SimpleAttributeSet();
        src.addAttribute("k2", "v2");
        src.addAttribute("k3", "v3");
        m.addAttributes(src);
        check(m.getAttributeCount() == 3, "count 3 after addAttributes");
        check("v2".equals(m.getAttribute("k2")), "k2 imported");
        check("v3".equals(m.getAttribute("k3")), "k3 imported");

        // removeAttribute.
        m.removeAttribute("k1");
        check(!m.isDefined("k1"), "k1 removed");
        check(m.getAttributeCount() == 2, "count 2 after remove");

        // removeAttributes(AttributeSet) removes only matching name/value pairs.
        SimpleAttributeSet toRemove = new SimpleAttributeSet();
        toRemove.addAttribute("k2", "v2");
        toRemove.addAttribute("k3", "WRONG");
        m.removeAttributes(toRemove);
        check(!m.isDefined("k2"), "k2 removed by matching pair");
        check(m.isDefined("k3"), "k3 retained (value mismatch)");

        // removeAttributes(Enumeration).
        Vector<String> names = new Vector<>();
        names.add("k3");
        m.removeAttributes(names.elements());
        check(!m.isDefined("k3"), "k3 removed via enumeration");
        check(m.getAttributeCount() == 0, "count 0 after removals");

        // Resolve parent: lookup falls through, isDefined stays local.
        MutableAttributeSet child = new SimpleAttributeSet();
        SimpleAttributeSet parent = new SimpleAttributeSet();
        parent.addAttribute("inherit", "pv");
        child.setResolveParent(parent);
        check(child.getResolveParent() == parent, "resolve parent set");
        check("pv".equals(child.getAttribute("inherit")), "lookup falls through to parent");
        check(!child.isDefined("inherit"), "isDefined is local only");
        child.addAttribute("own", "ov");
        check("ov".equals(child.getAttribute("own")), "own attribute wins");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
