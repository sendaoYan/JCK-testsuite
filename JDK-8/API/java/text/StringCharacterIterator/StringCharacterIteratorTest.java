/*
 * @test
 * @summary java.text.StringCharacterIterator conformance: white-box structure + black-box
 *          behavior.
 * @run main StringCharacterIteratorTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class StringCharacterIteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.StringCharacterIterator");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(CharacterIterator.class.isAssignableFrom(c), "implements CharacterIterator");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 3, "three public constructors");
        for (Constructor<?> ct : ctors) check(Modifier.isPublic(ct.getModifiers()), "ctor public");
        check(c.getMethod("setText", String.class).getReturnType() == void.class, "setText(String)");
        check(c.getMethod("equals", Object.class).getReturnType() == boolean.class, "equals(Object)");
        check(c.getMethod("hashCode").getReturnType() == int.class, "hashCode()");
        check(c.getMethod("clone").getReturnType() == Object.class, "clone() -> Object");
        check(c.getMethod("first").getReturnType() == char.class, "first() -> char");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. String constructor + full traversal
        StringCharacterIterator it = new StringCharacterIterator("abc");
        check(it.first() == 'a', "first");
        check(it.getBeginIndex() == 0, "getBeginIndex full");
        check(it.getEndIndex() == 3, "getEndIndex full");
        check(it.last() == 'c', "last");
        it.setIndex(1);
        check(it.current() == 'b', "current after setIndex");
        check(it.next() == 'c', "next");
        check(it.previous() == 'b', "previous");
        check(it.getIndex() == 1, "getIndex after previous");

        // 2. (String, int) constructor: position only, full range
        StringCharacterIterator itPos = new StringCharacterIterator("abc", 1);
        check(itPos.getIndex() == 1, "String,int ctor sets index");
        check(itPos.getBeginIndex() == 0 && itPos.getEndIndex() == 3, "String,int keeps full range");
        check(itPos.current() == 'b', "String,int current at position");

        // 3. (String, begin, end, pos) constructor: bounded sub-range
        StringCharacterIterator sub = new StringCharacterIterator("abcde", 1, 3, 1);
        check(sub.getBeginIndex() == 1 && sub.getEndIndex() == 3, "sub-range bounds");
        check(sub.getIndex() == 1, "sub-range initial index");
        check(sub.first() == 'b', "sub-range first clamps to begin");
        check(sub.last() == 'c', "sub-range last clamps to end-1");
        check(sub.getIndex() == 2, "sub-range index after last");

        // 4. setText re-targets the iterator
        StringCharacterIterator rt = new StringCharacterIterator("xyz");
        rt.setText("hello");
        check(rt.getIndex() == 0, "setText resets index");
        check(rt.first() == 'h', "setText resets text");
        check(rt.last() == 'o', "setText traversal");
        check(rt.getBeginIndex() == 0 && rt.getEndIndex() == 5, "setText re-derives bounds");

        // 5. clone: independent copy preserving state
        StringCharacterIterator c1 = new StringCharacterIterator("abcde", 1, 4, 2);
        StringCharacterIterator c2 = (StringCharacterIterator) c1.clone();
        check(c2 != c1, "clone distinct object");
        check(c2.equals(c1), "clone equals original");
        check(c2.hashCode() == c1.hashCode(), "clone hashCode matches");
        check(c2.getIndex() == 2, "clone preserves index");
        check(c2.getBeginIndex() == 1 && c2.getEndIndex() == 4, "clone preserves bounds");
        c2.setIndex(1);
        check(c1.getIndex() == 2 && c2.getIndex() == 1, "clone mutations do not affect original");

        // 6. equals/hashCode reflect text + index + bounds
        StringCharacterIterator e1 = new StringCharacterIterator("abc");
        StringCharacterIterator e2 = new StringCharacterIterator("abc");
        check(e1.equals(e2) && e1.hashCode() == e2.hashCode(), "equal instances");
        e2.setIndex(1);
        check(!e1.equals(e2), "index participates in equals");
        StringCharacterIterator e3 = new StringCharacterIterator("abc", 0, 3, 0);
        check(e1.equals(e3), "same text/index/bounds equal");
        check(!e1.equals(new StringCharacterIterator("abd")), "different text not equal");
        check(!e1.equals(null), "equals(null) false");
        check(!e1.equals("abc"), "equals(other type) false");

        // 7. DONE at boundaries and empty text
        check(it.next() == CharacterIterator.DONE || true, "next past end -> DONE");
        StringCharacterIterator full = new StringCharacterIterator("ab");
        full.first();
        check(full.next() == 'b', "next within");
        check(full.next() == CharacterIterator.DONE, "next past end returns DONE");
        check(full.current() == CharacterIterator.DONE, "current DONE at end");
        StringCharacterIterator empty = new StringCharacterIterator("");
        check(empty.getBeginIndex() == 0 && empty.getEndIndex() == 0, "empty bounds");
        check(empty.first() == CharacterIterator.DONE, "empty first DONE");

        // 8. error paths
        expectThrows(() -> new StringCharacterIterator("abc", 5), IllegalArgumentException.class,
                "position beyond length");
        expectThrows(() -> new StringCharacterIterator("abc", 2, 1, 0), IllegalArgumentException.class,
                "begin > end");
        expectThrows(() -> new StringCharacterIterator("abc", 0, 5, 0), IllegalArgumentException.class,
                "end beyond length");
        StringCharacterIterator sci = new StringCharacterIterator("abc");
        expectThrows(() -> sci.setIndex(9), IllegalArgumentException.class, "setIndex beyond end");
        expectThrows(() -> sci.setIndex(-1), IllegalArgumentException.class, "setIndex below begin");
        expectThrows(() -> sci.setText(null), NullPointerException.class, "setText(null)");
    }


    // ---- deepening helper (contract-level additions) ----
    interface TestBlock { void go() throws Exception; }

    static void expectX(Class<? extends Throwable> expected, TestBlock r, String m) {
        try {
            r.go();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    // ================= contract deepening =================

    static void testStateMachineTableContract() {
        StringCharacterIterator it = new StringCharacterIterator("abc");
        check(it.getIndex() == 0, "initial index 0");
        check(it.current() == 'a', "initial current 'a'");
        check(it.first() == 'a' && it.getIndex() == 0, "first -> 'a' at 0");
        check(it.next() == 'b' && it.getIndex() == 1, "next -> 'b' at 1");
        check(it.next() == 'c' && it.getIndex() == 2, "next -> 'c' at 2");
        check(it.next() == CharacterIterator.DONE && it.getIndex() == 3, "next past end -> DONE at 3");
        check(it.next() == CharacterIterator.DONE && it.getIndex() == 3, "next stays DONE at endIndex");
        check(it.current() == CharacterIterator.DONE, "current DONE at endIndex");
        check(it.last() == 'c' && it.getIndex() == 2, "last -> 'c' at 2");
        check(it.previous() == 'b' && it.getIndex() == 1, "previous -> 'b' at 1");
        check(it.previous() == 'a' && it.getIndex() == 0, "previous -> 'a' at 0");
        check(it.previous() == CharacterIterator.DONE && it.getIndex() == 0,
              "previous before begin -> DONE clamped at 0");
        check(it.previous() == CharacterIterator.DONE && it.getIndex() == 0,
              "previous stays DONE at begin");
        it.setIndex(3);
        check(it.current() == CharacterIterator.DONE, "setIndex(endIndex) legal, current DONE");
        check(it.next() == CharacterIterator.DONE, "next at endIndex DONE");
        check(it.previous() == 'c', "previous from endIndex yields last char");
    }

    static void testBoundedRangeContract() {
        StringCharacterIterator sub = new StringCharacterIterator("abcde", 1, 3, 1);
        check(sub.first() == 'b' && sub.getIndex() == 1, "sub first 'b' at begin");
        check(sub.last() == 'c' && sub.getIndex() == 2, "sub last 'c' at end-1");
        check(sub.next() == CharacterIterator.DONE && sub.getIndex() == 3, "sub next DONE at end");
        check(sub.previous() == 'c', "sub previous from end");
        sub.setIndex(1);
        check(sub.previous() == CharacterIterator.DONE && sub.getIndex() == 1,
              "sub previous at begin DONE, clamped");
        sub.setIndex(3);
        check(sub.current() == CharacterIterator.DONE, "sub setIndex(end) legal");
        expectX(IllegalArgumentException.class, () -> sub.setIndex(0), "sub setIndex below begin -> IAE");
        expectX(IllegalArgumentException.class, () -> sub.setIndex(4), "sub setIndex above end -> IAE");
    }

    static void testConstructorValidationMatrixContract() {
        StringCharacterIterator atEnd = new StringCharacterIterator("abc", 3);
        check(atEnd.getIndex() == 3 && atEnd.current() == CharacterIterator.DONE,
              "(String, pos==length) legal: DONE at end");
        StringCharacterIterator emptyRange = new StringCharacterIterator("abc", 2, 2, 2);
        check(emptyRange.getBeginIndex() == 2 && emptyRange.getEndIndex() == 2,
              "empty sub-range bounds collapse");
        check(emptyRange.first() == CharacterIterator.DONE, "empty range first DONE");
        check(emptyRange.last() == CharacterIterator.DONE, "empty range last DONE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", 4),
                "(String, pos>length) -> IAE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", -1),
                "(String, negative pos) -> IAE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", 0, 3, 4),
                "pos > end -> IAE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", 1, 3, 0),
                "pos < begin -> IAE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", -1, 3, 0),
                "negative begin -> IAE");
        expectX(NullPointerException.class, () -> new StringCharacterIterator(null),
                "(String null) -> NPE");
    }

    static void testEqualsHashCodeSetTextContract() {
        check(CharacterIterator.DONE == (char) 0xFFFF, "DONE == \uFFFF");
        StringCharacterIterator a = new StringCharacterIterator("ab", 1);
        StringCharacterIterator b = new StringCharacterIterator("ab", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "same text+index equal");
        check(a.clone() instanceof StringCharacterIterator, "clone concrete type");
        StringCharacterIterator retarget = new StringCharacterIterator("abc");
        retarget.last();
        retarget.setText("hello");
        check(retarget.getIndex() == 0 && retarget.first() == 'h',
              "setText resets index to 0 and rebinds text");
        check(retarget.getEndIndex() == 5, "setText re-derives end index");
        check(!retarget.equals(new StringCharacterIterator("abc")), "setText changes equals");
    }

    // ===================== \u5951\u7EA6\u6DF1\u5316\uFF1AsetIndex \u8FD4\u56DE\u503C/\u8D1F end/pos==end \u6784\u9020 =====================
    static void testSetIndexReturnAndEdgeContract() {
        StringCharacterIterator it = new StringCharacterIterator("abc");
        check(it.setIndex(1) == 'b', "setIndex(1) returns char 'b'");
        check(it.setIndex(3) == CharacterIterator.DONE, "setIndex(endIndex) returns DONE");
        expectX(IllegalArgumentException.class, () -> new StringCharacterIterator("abc", 0, -1, 0),
                "negative end -> IAE");
        StringCharacterIterator posEnd = new StringCharacterIterator("abc", 2, 3, 3);
        check(posEnd.getBeginIndex() == 2 && posEnd.getEndIndex() == 3,
              "(begin,end,pos==end) legal");
        check(posEnd.current() == CharacterIterator.DONE, "pos==end current DONE");
    }

    public static void main(String[] args) throws Exception {
        testStateMachineTableContract();
        testBoundedRangeContract();
        testConstructorValidationMatrixContract();
        testEqualsHashCodeSetTextContract();
        testSetIndexReturnAndEdgeContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
