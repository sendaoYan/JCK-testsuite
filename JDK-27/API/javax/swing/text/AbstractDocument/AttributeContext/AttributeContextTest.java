/*
 * @test
 * @summary javax.swing.text.AbstractDocument.AttributeContext conformance: white-box structure + black-box behavior.
 * @run main AttributeContextTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;

public class AttributeContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.AbstractDocument$AttributeContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // StyleContext is the reference AttributeContext implementation.
        AbstractDocument.AttributeContext ac = new StyleContext();

        AttributeSet empty = ac.getEmptySet();
        check(empty != null, "getEmptySet non-null");
        check(empty.getAttributeCount() == 0, "empty set has no attributes");
        check(empty == ac.getEmptySet(), "getEmptySet stable instance");

        SimpleAttributeSet sas = new SimpleAttributeSet();
        sas.addAttribute("k", "v");
        sas.addAttribute("n", "1");

        AttributeSet added = ac.addAttribute(empty, "k", "v");
        check("v".equals(added.getAttribute("k")), "addAttribute adds name/value");
        check(added.getAttributeCount() == 1, "addAttribute count 1");

        AttributeSet addedAll = ac.addAttributes(empty, sas);
        check("v".equals(addedAll.getAttribute("k")) && "1".equals(addedAll.getAttribute("n")),
              "addAttributes adds set");

        AttributeSet removed = ac.removeAttribute(addedAll, "k");
        check(removed.getAttribute("k") == null, "removeAttribute removes name");
        check("1".equals(removed.getAttribute("n")), "removeAttribute keeps other");

        AttributeSet removedAll = ac.removeAttributes(addedAll, sas);
        check(removedAll.getAttribute("k") == null && removedAll.getAttribute("n") == null,
              "removeAttributes(AttributeSet) removes all named");

        Enumeration<?> names = new Vector<Object>(Arrays.asList("k", "n")).elements();
        AttributeSet removedEnum = ac.removeAttributes(addedAll, names);
        check(removedEnum.getAttribute("k") == null && removedEnum.getAttribute("n") == null,
              "removeAttributes(Enumeration) removes all enumerated");

        // reclaim is a documented no-op hook; must be callable without effect.
        ac.reclaim(addedAll);
        check(addedAll.getAttribute("k") != null, "reclaim leaves set usable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
