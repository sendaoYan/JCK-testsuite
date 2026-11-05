/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.CollapsedStringAdapter conformance: xs:token collapse semantics.
 * @run main CollapsedStringAdapterTest
 */

import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CollapsedStringAdapterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.bind.annotation.adapters.CollapsedStringAdapter a = new javax.xml.bind.annotation.adapters.CollapsedStringAdapter();
        check("a b".equals(a.unmarshal("  a   b  ")), "unmarshal collapses whitespace");
        check("a b".equals(a.marshal("a b")), "marshal");
        testClassContract();
        testUnmarshal();
        testMarshal();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<CollapsedStringAdapter> c = CollapsedStringAdapter.class;
        check("javax.xml.bind.annotation.adapters.CollapsedStringAdapter".equals(c.getName()), "FQCN exact");
        check("CollapsedStringAdapter".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation.adapters".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        // non-breaking space (U+00A0) is NOT XML whitespace: untouched by collapse
        CollapsedStringAdapter a = new CollapsedStringAdapter();
        check("a\u00A0b".equals(a.unmarshal("a\u00A0b")), "NBSP not treated as whitespace");
        String mixed = a.unmarshal("a\u00A0 b");
        check(mixed.length() == 4, "NBSP plus regular space length preserved");
        check(mixed.charAt(0) == 'a' && mixed.charAt(1) == '\u00A0' && mixed.charAt(2) == ' ' && mixed.charAt(3) == 'b',
                "inner regular space kept, NBSP kept");
        check("a\u00A0b".equals(a.marshal("a\u00A0b")), "marshal no-op for NBSP");
    }

    static void testClassContract() {
        Class<CollapsedStringAdapter> c = CollapsedStringAdapter.class;
        check(!Modifier.isFinal(c.getModifiers()), "class not final (subclassable)");
        check(c.getSuperclass() == XmlAdapter.class, "extends XmlAdapter<String, String>");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        XmlAdapter<String, String> gen = new CollapsedStringAdapter();
        try {
            check("a b".equals(gen.unmarshal(" a  b ")), "generic-typed unmarshal bridge works");
            check("x".equals(gen.marshal("x")), "generic-typed marshal bridge works");
        } catch (Exception e) {
            check(false, "bridge invocation failed: " + e);
        }
        // subclass inherits the collapsing behavior
        CollapsedStringAdapter sub = new CollapsedStringAdapter() {};
        check("a b".equals(sub.unmarshal("  a   b  ")), "anonymous subclass inherits collapse");
    }

    static void testUnmarshal() {
        CollapsedStringAdapter a = new CollapsedStringAdapter();
        check(a.unmarshal(null) == null, "unmarshal(null) == null");
        check("".equals(a.unmarshal("")), "unmarshal(\"\") == \"\"");
        check("".equals(a.unmarshal("   ")), "all-whitespace collapses to empty");
        check("".equals(a.unmarshal("\t")), "tab-only collapses to empty");
        check("".equals(a.unmarshal("\r\n ")), "mixed trailing whitespace collapses to empty");
        check("a b c".equals(a.unmarshal("a\tb\nc")), "tabs/newlines collapse to single spaces");
        check("x".equals(a.unmarshal("\t\n x")), "leading whitespace stripped");
        check("a b".equals(a.unmarshal("a  \t\r\n  b")), "inner whitespace run collapses to one space");
        check("a b".equals(a.unmarshal("a b ")), "trailing whitespace stripped");
        check("abc".equals(a.unmarshal("abc")), "already-clean string unchanged");
        String preserved = a.unmarshal("\u000Ba\u000Cc\r");
        check(preserved.length() == 4, "vertical tab / form feed are NOT XML whitespace (kept)");
        check(preserved.charAt(0) == '\u000B' && preserved.charAt(1) == 'a' && preserved.charAt(2) == '\u000C' && preserved.charAt(3) == 'c',
                "only trailing \\r stripped from non-XML-whitespace mix");
        check("x y\u000Bz".equals(a.unmarshal("x y\u000Bz ")), "inner \\u000B kept, trailing space stripped");
        // idempotence: collapsing twice yields the same result
        check("a b".equals(a.unmarshal(a.unmarshal("  a   b  "))), "collapse is idempotent");
    }

    static void testMarshal() {
        CollapsedStringAdapter a = new CollapsedStringAdapter();
        check(a.marshal(null) == null, "marshal(null) == null");
        check("".equals(a.marshal("")), "marshal(\"\") == \"\"");
        // marshal is documented as a no-op: no collapse-time validation is applied outbound
        check(" a  b ".equals(a.marshal(" a  b ")), "marshal leaves uncollapsed string untouched");
        check("a\tb".equals(a.marshal("a\tb")), "marshal leaves tabs untouched");
        check("  ".equals(a.marshal("  ")), "marshal leaves whitespace-only string untouched");
        check("a b".equals(a.marshal("a b")), "marshal identity on clean string");
        check(" a  b ".equals(a.marshal(a.marshal(" a  b "))), "double marshal still never collapses");
    }
}
