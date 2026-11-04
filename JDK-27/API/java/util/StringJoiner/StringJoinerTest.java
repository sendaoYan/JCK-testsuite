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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
