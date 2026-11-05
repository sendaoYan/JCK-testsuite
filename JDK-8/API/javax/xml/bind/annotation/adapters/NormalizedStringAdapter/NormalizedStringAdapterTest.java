/*
 * @test
 * @summary javax.xml.bind.annotation.adapters.NormalizedStringAdapter conformance: xs:normalizedString replace semantics.
 * @run main NormalizedStringAdapterTest
 */

import java.lang.reflect.Modifier;

import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class NormalizedStringAdapterTest {
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
        javax.xml.bind.annotation.adapters.NormalizedStringAdapter a = new javax.xml.bind.annotation.adapters.NormalizedStringAdapter();
        check("a b".equals(a.unmarshal("a	b")), "unmarshal normalizes whitespace");
        check("a b".equals(a.marshal("a b")), "marshal");
        testClassContract();
        testUnmarshal();
        testMarshal();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() {
        Class<NormalizedStringAdapter> c = NormalizedStringAdapter.class;
        check("javax.xml.bind.annotation.adapters.NormalizedStringAdapter".equals(c.getName()), "FQCN exact");
        check("NormalizedStringAdapter".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation.adapters".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        NormalizedStringAdapter a = new NormalizedStringAdapter();
        check(" a  b c".equals(a.unmarshal("\ta\r b\nc")), "interleaved replace vector exact");
        check(a.unmarshal("\ta\r b\nc").length() == 7, "replace preserves length");
        check("a\u00A0b".equals(a.unmarshal("a\u00A0b")), "NBSP not treated as whitespace");
        check(new NormalizedStringAdapter() != a, "two adapter instances independent");
    }

    static void testClassContract() {
        Class<NormalizedStringAdapter> c = NormalizedStringAdapter.class;
        check(Modifier.isFinal(c.getModifiers()), "class is final (unlike CollapsedStringAdapter)");
        check(c.getSuperclass() == XmlAdapter.class, "extends XmlAdapter<String, String>");
        check(c.getConstructors().length == 1, "single public no-arg constructor");
        XmlAdapter<String, String> gen = new NormalizedStringAdapter();
        try {
            check("a b".equals(gen.unmarshal("a\tb")), "generic-typed unmarshal bridge works");
            check("a\tb".equals(gen.marshal("a\tb")), "generic-typed marshal bridge works");
        } catch (Exception e) {
            check(false, "bridge invocation failed: " + e);
        }
    }

    static void testUnmarshal() {
        NormalizedStringAdapter a = new NormalizedStringAdapter();
        check(a.unmarshal(null) == null, "unmarshal(null) == null");
        check("".equals(a.unmarshal("")), "unmarshal(\"\") == \"\"");
        check("a b".equals(a.unmarshal("a\tb")), "tab replaced by single space");
        check("a b".equals(a.unmarshal("a\rb")), "carriage return replaced");
        check("a b".equals(a.unmarshal("a\nb")), "newline replaced");
        check("   ".equals(a.unmarshal("\t\r\n")), "each whitespace char becomes one space (no collapsing)");
        check("a  b".equals(a.unmarshal("a  b")), "existing spaces untouched (no collapsing)");
        check("a     b".equals(a.unmarshal("a \t\r\n b")), "mixed run: replace only, run length preserved");
        String preserved = a.unmarshal("\u000Ba\u000Cc\r");
        check(preserved.length() == 5, "vertical tab / form feed are NOT XML whitespace (kept)");
        check(preserved.charAt(0) == '\u000B' && preserved.charAt(1) == 'a' && preserved.charAt(2) == '\u000C'
                && preserved.charAt(3) == 'c' && preserved.charAt(4) == ' ', "only \\r replaced by space");
        check("abc".equals(a.unmarshal("abc")), "clean string unchanged");
        check(" a ".equals(a.unmarshal("\ta\n")), "leading and trailing whitespace also replaced (not stripped)");
        check("a  b".equals(a.unmarshal("a\t\rb")), "tab+CR run becomes two spaces (no collapsing)");
        check(a.unmarshal(a.unmarshal("a\t\rb")).equals(a.unmarshal("a\t\rb")), "replace is idempotent");
    }

    static void testMarshal() {
        NormalizedStringAdapter a = new NormalizedStringAdapter();
        check(a.marshal(null) == null, "marshal(null) == null");
        check("".equals(a.marshal("")), "marshal(\"\") == \"\"");
        // marshal is documented as a no-op: no normalization outbound
        check("a\tb".equals(a.marshal("a\tb")), "marshal leaves tab untouched");
        check("a\r\nb".equals(a.marshal("a\r\nb")), "marshal leaves CR/LF untouched");
        check("  ".equals(a.marshal("  ")), "marshal leaves spaces untouched");
        check("a b".equals(a.marshal("a b")), "marshal identity on clean string");
        check("a\tb".equals(a.marshal(a.marshal("a\tb"))), "double marshal still never normalizes");
    }
}
