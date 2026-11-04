/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AttributeContext conformance: interface shape + interning contract exercised through StyleContext (headless-safe).
 * @run main AttributeContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;

public class AttributeContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AttributeContext", false, AttributeContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addAttribute"), "method addAttribute");
        check(names.contains("addAttributes"), "method addAttributes");
        check(names.contains("removeAttribute"), "method removeAttribute");
        check(names.contains("removeAttributes"), "method removeAttributes");
        check(names.contains("getEmptySet"), "method getEmptySet");
        check(names.contains("reclaim"), "method reclaim");
        check(hasInterface(Class.forName("javax.swing.text.StyleContext"), "javax.swing.text.AbstractDocument$AttributeContext"),
                "StyleContext implements AttributeContext");

        testInterningContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testInterningContract() {
        AbstractDocument.AttributeContext ctx = new StyleContext();
        AttributeSet empty = ctx.getEmptySet();
        check(empty.getAttributeCount() == 0, "empty set has no attributes");
        check(ctx.getEmptySet() == empty, "empty set is stable");

        // addAttribute returns an interned immutable instance; the input is untouched
        AttributeSet one = ctx.addAttribute(empty, "k", "v");
        check(one.getAttributeCount() == 1 && "v".equals(one.getAttribute("k")), "add one");
        check(ctx.addAttribute(empty, "k", "v") == one, "same addition interns to the same instance");
        check(empty.getAttributeCount() == 0, "source unchanged");

        // overwriting produces a distinct interned instance
        AttributeSet two = ctx.addAttribute(one, "k", "v2");
        check("v2".equals(two.getAttribute("k")), "overwritten value");
        check("v".equals(one.getAttribute("k")), "original immutable");

        // bulk add merges
        SimpleAttributeSet more = new SimpleAttributeSet();
        more.addAttribute("x", "1");
        more.addAttribute("k", "v3");
        AttributeSet merged = ctx.addAttributes(one, more);
        check(merged.getAttributeCount() == 2, "bulk merge count");
        check("v3".equals(merged.getAttribute("k")), "bulk merge overwrites");

        // removals walk back toward the empty set
        AttributeSet dropK = ctx.removeAttribute(merged, "k");
        check(dropK.getAttributeCount() == 1 && "1".equals(dropK.getAttribute("x")), "remove one key");
        AttributeSet dropX = ctx.removeAttribute(dropK, "x");
        check(dropX.getAttributeCount() == 0, "back to empty");
        check(ctx.removeAttributes(merged, more).getAttributeCount() == 0, "removeAttributes(AttributeSet) empties matching entries");

        // reclaim is a hint and must never throw
        ctx.reclaim(one);
        ctx.reclaim(empty);
        check(true, "reclaim tolerated");
    }
}
