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

    // ============ contract deepening (appended) ============

    // merge(): the other's joined content (its own delimiter, WITHOUT its prefix/suffix)
    // is appended to this joiner as the next single element; the other is not modified.
    static void testMergeDifferentDelimiters() {
        StringJoiner a = new StringJoiner(",", "[", "]");
        a.add("0");
        StringJoiner b = new StringJoiner(";", "<", ">");
        b.add("1").add("2");
        check(a.merge(b) == a, "merge returns this");
        check("[0,1;2]".equals(a.toString()),
              "merge keeps other's internal delimiter, drops other's prefix/suffix");
        check(a.length() == 7, "length reflects merged content");
        check("<1;2>".equals(b.toString()), "merge does not modify the other joiner");

        // empty this + non-empty other: this becomes non-empty with THIS's prefix/suffix
        StringJoiner intoEmpty = new StringJoiner(",", "[", "]");
        intoEmpty.setEmptyValue("E");
        StringJoiner other = new StringJoiner(";", "<", ">");
        other.add("a");
        intoEmpty.merge(other);
        check("[a]".equals(intoEmpty.toString()),
              "merge into empty adopts content, emptyValue no longer applies");
        check(intoEmpty.length() == 3, "length after merge into empty");

        // other's emptyValue is irrelevant: only whether it currently holds elements matters
        StringJoiner withEmptyValue = new StringJoiner(";");
        withEmptyValue.setEmptyValue("OTHEREMPTY");
        StringJoiner target = new StringJoiner(",");
        target.add("x");
        target.merge(withEmptyValue);
        check("x".equals(target.toString()), "other with emptyValue but no elements is empty for merge");

        // merging twice accumulates
        StringJoiner acc = new StringJoiner(",");
        acc.add("1");
        StringJoiner two = new StringJoiner(";");
        two.add("a").add("b");
        acc.merge(two);
        acc.merge(two);
        check("1,a;b,a;b".equals(acc.toString()), "merge accumulates across calls");
    }

    // length() always equals toString().length() in every state.
    static void testLengthAlwaysConsistent() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        check(sj.length() == sj.toString().length() && sj.toString().equals("[]"),
              "length consistent while empty");
        sj.setEmptyValue("<none>");
        check(sj.length() == sj.toString().length() && sj.length() == 6,
              "length consistent with emptyValue");
        sj.add("abc");
        check(sj.length() == sj.toString().length() && "[abc]".equals(sj.toString()),
              "length consistent with one element");
        sj.add(null); // appends "null"
        check(sj.length() == sj.toString().length() && "[abc,null]".equals(sj.toString()),
              "length consistent after add(null)");
        // default no-prefix/suffix joiner
        StringJoiner bare = new StringJoiner(",");
        check(bare.toString().isEmpty() && bare.length() == 0, "bare joiner empty is \"\" length 0");
        bare.add("");
        check(bare.length() == 0 && "".equals(bare.toString()), "single empty element renders empty string");
    }

    // setEmptyValue after elements were added has no effect (only the empty state consults it).
    static void testSetEmptyValueAfterElements() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("x").add("y");
        sj.setEmptyValue("LATE");
        check("x,y".equals(sj.toString()), "setEmptyValue after elements ignored for toString");
        check(sj.length() == 3, "setEmptyValue after elements ignored for length");
    }

    // add() contracts: chaining, null rendering, empty strings, single element.
    static void testAddContracts() {
        StringJoiner sj = new StringJoiner(",");
        check(sj.add("a") == sj && sj.add(null) == sj && sj.add("b") == sj,
              "add chains regardless of content");
        check("a,null,b".equals(sj.toString()), "null renders as \"null\" between elements");
        StringJoiner single = new StringJoiner(",");
        single.add("only");
        check("only".equals(single.toString()), "single element: no delimiter emitted");
        StringJoiner noPS = new StringJoiner(",", "", "");
        noPS.add("a").add("b");
        check("a,b".equals(noPS.toString()), "empty prefix/suffix render nothing");
        StringJoiner longChain = new StringJoiner("-");
        StringBuilder expect = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longChain.add("e" + i);
            if (i > 0) expect.append('-');
            expect.append('e').append(i);
        }
        check(expect.toString().equals(longChain.toString()), "100-element chain exact");
    }

    // toString() is deterministic and idempotent; repeated calls agree.
    static void testToStringIdempotent() {
        StringJoiner sj = new StringJoiner(",", "<", ">");
        sj.add("x");
        String first = sj.toString();
        check(first.equals(sj.toString()) && first.equals(sj.toString()),
              "toString idempotent after add");
        check(sj.length() == first.length(), "length agrees with toString");
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
        testMergeDifferentDelimiters();
        testLengthAlwaysConsistent();
        testSetEmptyValueAfterElements();
        testAddContracts();
        testToStringIdempotent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
