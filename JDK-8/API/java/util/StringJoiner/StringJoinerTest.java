/*
 * @test
 * @summary java.util.StringJoiner API conformance (Java 8 surface): complete contract coverage
 *          of constructors (null delimiter/prefix/suffix NPE), add (null -> "null", chainable),
 *          setEmptyValue (null NPE + override), merge (null NPE + empty/non-empty semantics),
 *          length (prefix/suffix counted, empty value length), and toString (empty/prefix+suffix).
 * @run main StringJoinerTest
 */

import java.util.StringJoiner;

public class StringJoinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testDelimiter() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("a").add("b").add("c");
        check("a,b,c".equals(sj.toString()), "delimiter");
    }

    static void testPrefixSuffix() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        sj.add("x").add("y");
        check("[x,y]".equals(sj.toString()), "prefix/suffix");
        check(sj.length() == 5, "length");
    }

    static void testEmptyValue() {
        check("empty".equals(new StringJoiner(",").setEmptyValue("empty").toString()),
              "setEmptyValue");
    }

    static void testConstructorNull() {
        expectThrows(NullPointerException.class, () -> new StringJoiner(null),
                     "StringJoiner((CharSequence)null) NPE");
        expectThrows(NullPointerException.class, () -> new StringJoiner(",", null, "]"),
                     "StringJoiner(null prefix) NPE");
        expectThrows(NullPointerException.class, () -> new StringJoiner(null, "[", "]"),
                     "StringJoiner(null delimiter) NPE");
        expectThrows(NullPointerException.class, () -> new StringJoiner(",", "[", null),
                     "StringJoiner(null suffix) NPE");
    }

    static void testAddNullAndEmpty() {
        // add(null) appends the literal string "null" (not NPE).
        StringJoiner sj = new StringJoiner(",");
        sj.add(null);
        check("null".equals(sj.toString()), "add(null) -> \"null\"");
        check(sj.add("x") == sj, "add returns this");

        // empty element still counts as an element (so emptyValue is not used).
        StringJoiner sj2 = new StringJoiner(",");
        sj2.setEmptyValue("EMPTY");
        sj2.add("");
        check("".equals(sj2.toString()), "add(\"\") makes it non-empty with empty element");
    }

    static void testSetEmptyValueNull() {
        expectThrows(NullPointerException.class,
                     () -> new StringJoiner(",").setEmptyValue(null),
                     "setEmptyValue(null) NPE");
    }

    static void testMerge() {
        expectThrows(NullPointerException.class,
                     () -> new StringJoiner(",").merge(null),
                     "merge(null) NPE");

        StringJoiner a = new StringJoiner(",");
        a.add("1");
        StringJoiner b = new StringJoiner(",");
        b.add("2").add("3");
        check(a.merge(b) == a, "merge returns this");
        check("1,2,3".equals(a.toString()), "merge non-empty other");

        // merging an empty other has no effect, and emptyValue semantics preserved.
        StringJoiner c = new StringJoiner(",");
        c.setEmptyValue("none");
        StringJoiner d = new StringJoiner(",");
        c.merge(d);
        check("none".equals(c.toString()), "merge empty other keeps emptyValue");
    }

    static void testLengthAndEmpty() {
        // empty joiner with prefix/suffix: length == prefix.length + suffix.length.
        StringJoiner empty = new StringJoiner(",", "[", "]");
        check("[]".equals(empty.toString()), "empty toString == prefix+suffix");
        check(empty.length() == 2, "empty length == prefix+suffix length");

        // empty joiner with custom emptyValue: length == emptyValue.length.
        StringJoiner ev = new StringJoiner(",", "[", "]");
        ev.setEmptyValue("<none>");
        check(ev.length() == "<none>".length(), "length == emptyValue.length");
    }

    public static void main(String[] args) {
        testDelimiter();
        testPrefixSuffix();
        testEmptyValue();
        testConstructorNull();
        testAddNullAndEmpty();
        testSetEmptyValueNull();
        testMerge();
        testLengthAndEmpty();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
