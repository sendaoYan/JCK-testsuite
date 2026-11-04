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

    // ---- 契约深化（追加） ----

    // merge()：对方的已拼接内容（其内部分隔符、去掉其 prefix/suffix）作为 this 的下一个单一元素；
    // 对方不被修改。
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

        // 空 this + 非空 other：this 变为非空并采用 THIS 的 prefix/suffix，emptyValue 不再生效
        StringJoiner intoEmpty = new StringJoiner(",", "[", "]");
        intoEmpty.setEmptyValue("E");
        StringJoiner other = new StringJoiner(";", "<", ">");
        other.add("a");
        intoEmpty.merge(other);
        check("[a]".equals(intoEmpty.toString()),
              "merge into empty adopts content, emptyValue no longer applies");
        check(intoEmpty.length() == 3, "length after merge into empty");

        // other 的 emptyValue 无关紧要：只看它当前是否有元素
        StringJoiner withEmptyValue = new StringJoiner(";");
        withEmptyValue.setEmptyValue("OTHEREMPTY");
        StringJoiner target = new StringJoiner(",");
        target.add("x");
        target.merge(withEmptyValue);
        check("x".equals(target.toString()), "other with emptyValue but no elements is empty for merge");

        // 连续 merge 累加
        StringJoiner acc = new StringJoiner(",");
        acc.add("1");
        StringJoiner two = new StringJoiner(";");
        two.add("a").add("b");
        acc.merge(two);
        acc.merge(two);
        check("1,a;b,a;b".equals(acc.toString()), "merge accumulates across calls");
    }

    // length() 在任何状态都等于 toString().length()。
    static void testLengthAlwaysConsistent() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        check(sj.length() == sj.toString().length() && "[]".equals(sj.toString()),
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
        StringJoiner bare = new StringJoiner(",");
        check(bare.toString().isEmpty() && bare.length() == 0, "bare joiner empty is \"\" length 0");
        bare.add("");
        check(bare.length() == 0 && "".equals(bare.toString()), "single empty element renders empty string");
    }

    // 已有元素后 setEmptyValue 不影响 toString/length（只在空态被咨询）。
    static void testSetEmptyValueAfterElements() {
        StringJoiner sj = new StringJoiner(",");
        sj.add("x").add("y");
        sj.setEmptyValue("LATE");
        check("x,y".equals(sj.toString()), "setEmptyValue after elements ignored for toString");
        check(sj.length() == 3, "setEmptyValue after elements ignored for length");
    }

    // add() 契约：链式、null 渲染、空串、单元素无分隔符。
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

    // toString() 确定性且幂等。
    static void testToStringIdempotent() {
        StringJoiner sj = new StringJoiner(",", "<", ">");
        sj.add("x");
        String first = sj.toString();
        check(first.equals(sj.toString()) && first.equals(sj.toString()),
              "toString idempotent after add");
        check(sj.length() == first.length(), "length agrees with toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullContracts();
        testMergeDifferentDelimiters();
        testLengthAlwaysConsistent();
        testSetEmptyValueAfterElements();
        testAddContracts();
        testToStringIdempotent();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
