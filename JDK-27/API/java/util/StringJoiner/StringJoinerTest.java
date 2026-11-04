/*
 * @test
 * @summary java.util.StringJoiner conformance: white-box structure plus black-box behavior of
 *          delimiter joining, prefix/suffix, length, setEmptyValue, and merge.
 * @run main StringJoinerTest
 */

import java.util.StringJoiner;

public class StringJoinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.StringJoiner");
        check(!c.isInterface(), "StringJoiner is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "StringJoiner is final");
        c.getMethod("add", CharSequence.class);
        c.getMethod("merge", StringJoiner.class);
        c.getMethod("setEmptyValue", CharSequence.class);
        c.getMethod("length");
        c.getMethod("toString");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testDelimiter();
        testPrefixSuffix();
        testEmptyValue();
        testMerge();
    }

    static void testDelimiter() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("a").add("b").add("c");
        check("a,b,c".equals(sj.toString()), "delimiter");
        check(sj.length() == 5, "length of a,b,c");
        check("a,b,c".equals(sj.toString()), "toString repeatable");
    }

    static void testPrefixSuffix() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        sj.add("x").add("y");
        check("[x,y]".equals(sj.toString()), "prefix/suffix");
        check(sj.length() == 5, "length includes prefix/suffix");
    }

    static void testEmptyValue() {
        check("empty".equals(new StringJoiner(",").setEmptyValue("empty").toString()),
              "setEmptyValue");
        check("[]".equals(new StringJoiner(",", "[", "]").toString()),
              "empty with prefix/suffix ignores emptyValue");
    }

    static void testMerge() {
        StringJoiner a = new StringJoiner(",", "[", "]");
        a.add("a").add("b");
        StringJoiner b = new StringJoiner(",", "[", "]");
        b.add("c");
        StringJoiner merged = a.merge(b);
        check("[a,b,c]".equals(merged.toString()), "merge appends elements");
        check(merged == a, "merge returns receiver");

        // Merging into an empty joiner adopts the other's empty value.
        StringJoiner empty = new StringJoiner(",").setEmptyValue("none");
        StringJoiner other = new StringJoiner(",").setEmptyValue("also-none");
        empty.merge(other);
        check("none".equals(empty.toString()), "merging empty joiners keeps emptyValue");
    }

    // ---- 本棒新增：null 契约 ----
    static void testNullContracts() {
        assertThrows(NullPointerException.class, () -> new StringJoiner(null),
                "StringJoiner((CharSequence)null) NPE");
        assertThrows(NullPointerException.class, () -> new StringJoiner(",", null, "]"),
                "StringJoiner(null prefix) NPE");
        assertThrows(NullPointerException.class, () -> new StringJoiner(null, "[", "]"),
                "StringJoiner(null delimiter) NPE");
        assertThrows(NullPointerException.class, () -> new StringJoiner(",", "[", null),
                "StringJoiner(null suffix) NPE");
        assertThrows(NullPointerException.class, () -> new StringJoiner(",").setEmptyValue(null),
                "setEmptyValue(null) NPE");
        assertThrows(NullPointerException.class, () -> new StringJoiner(",").merge(null),
                "merge(null) NPE");

        // add(null) appends the literal "null"; add("") makes the joiner non-empty.
        StringJoiner sj = new StringJoiner(",");
        sj.add(null);
        check("null".equals(sj.toString()), "add(null) -> \"null\"");
        StringJoiner sj2 = new StringJoiner(",").setEmptyValue("EMPTY");
        sj2.add("");
        check("".equals(sj2.toString()), "add(\"\") non-empty with empty element");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
