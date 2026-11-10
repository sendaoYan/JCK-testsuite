/*
 * @test
 * @summary java.text.AttributedString conformance: white-box structure + black-box behavior.
 * @run main AttributedStringTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AttributedStringTest {
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

    static final AttributedCharacterIterator.Attribute LANG =
            AttributedCharacterIterator.Attribute.LANGUAGE;
    static final AttributedCharacterIterator.Attribute READ =
            AttributedCharacterIterator.Attribute.READING;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.text.AttributedString");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, non-final class");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        boolean hasStringCtor = false, hasMapCtor = false, hasIterCtor = false,
                hasIterRangeCtor = false, hasIterRangeAttrCtor = false;
        for (Constructor<?> ct : ctors) {
            Class<?>[] p = ct.getParameterTypes();
            if (p.length == 1 && p[0] == String.class) hasStringCtor = true;
            if (p.length == 2 && p[0] == String.class && Map.class.isAssignableFrom(p[1]))
                hasMapCtor = true;
            if (p.length == 1 && AttributedCharacterIterator.class.isAssignableFrom(p[0]))
                hasIterCtor = true;
            if (p.length == 3 && AttributedCharacterIterator.class.isAssignableFrom(p[0])
                    && p[1] == int.class && p[2] == int.class)
                hasIterRangeCtor = true;
            if (p.length == 4 && AttributedCharacterIterator.class.isAssignableFrom(p[0])
                    && p[1] == int.class && p[2] == int.class
                    && p[3] == AttributedCharacterIterator.Attribute[].class)
                hasIterRangeAttrCtor = true;
        }
        check(hasStringCtor, "AttributedString(String)");
        check(hasMapCtor, "AttributedString(String, Map)");
        check(hasIterCtor, "AttributedString(AttributedCharacterIterator)");
        check(hasIterRangeCtor, "AttributedString(AttributedCharacterIterator, int, int)");
        check(hasIterRangeAttrCtor,
                "AttributedString(AttributedCharacterIterator, int, int, Attribute[])");

        check(c.getMethod("addAttribute",
                AttributedCharacterIterator.Attribute.class, Object.class) != null,
                "addAttribute(Attribute, Object)");
        check(c.getMethod("addAttribute",
                AttributedCharacterIterator.Attribute.class, Object.class, int.class, int.class) != null,
                "addAttribute(Attribute, Object, int, int)");
        check(c.getMethod("addAttributes", Map.class, int.class, int.class) != null,
                "addAttributes(Map, int, int)");
        check(c.getMethod("getIterator").getReturnType() == AttributedCharacterIterator.class,
                "getIterator() -> AttributedCharacterIterator");
        check(c.getMethod("getIterator", AttributedCharacterIterator.Attribute[].class)
                .getReturnType() == AttributedCharacterIterator.class,
                "getIterator(Attribute[]) -> AttributedCharacterIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // 1. String constructor: whole-text single run, no attributes
        AttributedString as = new AttributedString("abc");
        AttributedCharacterIterator it = as.getIterator();
        check(it.getBeginIndex() == 0 && it.getEndIndex() == 3, "String ctor iterator spans text");
        check(it.getAllAttributeKeys().isEmpty(), "String ctor has no attributes");
        StringBuilder sb = new StringBuilder();
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) sb.append(c);
        check("abc".contentEquals(sb), "String ctor text readable");

        // 2. Map constructor: attribute applied to the entire string
        Map<AttributedCharacterIterator.Attribute, Object> m = new HashMap<>();
        m.put(LANG, "en");
        AttributedString as2 = new AttributedString("abc", m);
        check("en".equals(as2.getIterator().getAttribute(LANG)), "Map ctor applies attribute");
        check(as2.getIterator().getRunStart() == 0 && as2.getIterator().getRunLimit() == 3,
                "Map ctor single full run");

        // 3. addAttribute whole + ranged
        AttributedString as3 = new AttributedString("hello");
        as3.addAttribute(READ, "r");
        check("r".equals(as3.getIterator().getAttribute(READ)), "addAttribute whole string");
        as3.addAttribute(LANG, "en", 1, 3);
        AttributedCharacterIterator it3 = as3.getIterator();
        it3.setIndex(1);
        check("en".equals(it3.getAttribute(LANG)), "ranged attribute present in [1,3)");
        check(it3.getRunStart(LANG) == 1 && it3.getRunLimit(LANG) == 3, "ranged run bounds");
        it3.setIndex(4);
        check(it3.getAttribute(LANG) == null, "attribute absent outside range");
        check("r".equals(it3.getAttribute(READ)), "whole-string attribute still present");

        // 4. addAttributes(Map, begin, end)
        AttributedString as4 = new AttributedString("abcdef");
        as4.addAttributes(m, 2, 5);
        AttributedCharacterIterator it4 = as4.getIterator();
        it4.setIndex(3);
        check("en".equals(it4.getAttribute(LANG)), "addAttributes range applied");
        check(it4.getRunStart(LANG) == 2 && it4.getRunLimit(LANG) == 5, "addAttributes run bounds");
        it4.setIndex(0);
        check(it4.getAttribute(LANG) == null, "addAttributes absent outside range");

        // 5. getIterator(Attribute[]) yields a working iterator over the whole string
        AttributedString as5 = new AttributedString("xy");
        as5.addAttribute(LANG, "en");
        as5.addAttribute(READ, "r");
        AttributedCharacterIterator viaAttr = as5.getIterator(new AttributedCharacterIterator.Attribute[] {LANG});
        check("en".equals(viaAttr.getAttribute(LANG)), "attribute-array iterator keeps text");
        check(viaAttr.getBeginIndex() == 0 && viaAttr.getEndIndex() == 2,
                "attribute-array iterator spans whole string");
        StringBuilder as5Text = new StringBuilder();
        for (char c = viaAttr.first(); c != CharacterIterator.DONE; c = viaAttr.next())
            as5Text.append(c);
        check("xy".contentEquals(as5Text), "attribute-array iterator still reads the text");
        AttributedCharacterIterator emptyKeys = as5.getIterator(new AttributedCharacterIterator.Attribute[0]);
        check(emptyKeys.getBeginIndex() == 0 && emptyKeys.getEndIndex() == 2,
                "empty attribute-array iterator still spans the whole string");

        // 6. getIterator(Attribute[], begin, end): restricted substring window
        AttributedString as6 = new AttributedString("0123456789");
        as6.addAttribute(LANG, "en", 0, 10);
        AttributedCharacterIterator win = as6.getIterator(
                new AttributedCharacterIterator.Attribute[] {LANG}, 2, 8);
        check(win.getBeginIndex() == 2 && win.getEndIndex() == 8, "sub-window iterator bounds");
        check("en".equals(win.getAttribute(LANG)), "sub-window still carries attributes");

        // 7. AttributedCharacterIterator copy constructor preserves text and attributes
        AttributedString src = new AttributedString("world");
        src.addAttribute(LANG, "en", 1, 4);
        AttributedString copy = new AttributedString(src.getIterator());
        AttributedCharacterIterator cit = copy.getIterator();
        cit.setIndex(2);
        check("en".equals(cit.getAttribute(LANG)), "iterator ctor preserves attribute");
        check(copy.getIterator().getEndIndex() == 5, "iterator ctor preserves text length");
        // ranged copy constructor (begin,end)
        AttributedString sub = new AttributedString(src.getIterator(), 1, 3);
        AttributedCharacterIterator sit = sub.getIterator();
        check(sit.getBeginIndex() == 0 && sit.getEndIndex() == 2, "range copy normalizes indexes");
        check("en".equals(sit.getAttribute(LANG)), "range copy keeps overlapping attribute");

        // 8. error paths
        expectThrows(() -> new AttributedString((String) null), NullPointerException.class,
                "null text");
        expectThrows(() -> new AttributedString("ab").addAttribute(LANG, "en", 2, 1),
                IllegalArgumentException.class, "invalid range (start > end)");
        expectThrows(() -> new AttributedString("ab").addAttribute(LANG, "en", -1, 1),
                IllegalArgumentException.class, "negative start");
        expectThrows(() -> new AttributedString("ab").addAttribute(LANG, "en", 0, 5),
                IllegalArgumentException.class, "end beyond text length");
        expectThrows(() -> new AttributedString("ab").addAttribute(null, "en"),
                NullPointerException.class, "null attribute key");
        // empty string is valid, single empty run
        AttributedString empty = new AttributedString("");
        check(empty.getIterator().getBeginIndex() == 0 && empty.getIterator().getEndIndex() == 0,
                "empty string yields empty run");
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

    static void testConstructorMatrixContract() {
        expectX(NullPointerException.class, () -> new AttributedString("hi",
                (Map<AttributedCharacterIterator.Attribute, Object>) null),
                "(String, null Map) -> NPE");
        Map<AttributedCharacterIterator.Attribute, Object> m = new HashMap<>();
        m.put(LANG, java.util.Locale.ENGLISH);
        m.put(READ, "q");
        AttributedString as = new AttributedString("abcd", m);
        AttributedCharacterIterator it = as.getIterator();
        it.setIndex(1);
        check(java.util.Locale.ENGLISH.equals(it.getAttribute(LANG)) && "q".equals(it.getAttribute(READ)),
              "Map constructor applies every entry whole-range");
        check(it.getRunLimit() == 4, "Map-ctor attributes span the entire text");
        AttributedString fromIterator = new AttributedString(as.getIterator());
        AttributedCharacterIterator it2 = fromIterator.getIterator();
        check(java.util.Locale.ENGLISH.equals(it2.getAttribute(LANG)),
              "(AttributedCharacterIterator) constructor preserves attributes");
    }

    static void testAddAttributeRangeMatrixContract() {
        AttributedString base = new AttributedString("hello");
        expectX(IllegalArgumentException.class, () -> base.addAttribute(LANG, "en", 3, 1),
                "start > end -> IAE");
        expectX(IllegalArgumentException.class, () -> base.addAttribute(LANG, "en", 0, 99),
                "end > length -> IAE");
        expectX(IllegalArgumentException.class, () -> base.addAttribute(LANG, "en", 2, 2),
                "empty range -> IAE");
        AttributedString nullValue = new AttributedString("hi");
        nullValue.addAttribute(LANG, null);
        check(nullValue.getIterator().getAttribute(LANG) == null,
              "addAttribute(attr, null) stores null (removes visible value)");
        expectX(NullPointerException.class, () -> new AttributedString("hi").addAttribute(null, "en"),
                "addAttribute(null attr, ...) -> NPE");
        AttributedString sliced = new AttributedString("abcdef");
        sliced.addAttribute(LANG, "en", 1, 4);
        AttributedCharacterIterator si = sliced.getIterator();
        si.setIndex(2);
        check("en".equals(si.getAttribute(LANG)) && si.getRunStart() == 1 && si.getRunLimit() == 4,
              "partial-range addAttribute bounds the run");
        si.setIndex(5);
        check(si.getAttribute(LANG) == null, "outside the added range value is null");
    }

    static void testGetIteratorMatrixContract() {
        AttributedString as = new AttributedString("hello world");
        as.addAttribute(LANG, java.util.Locale.FRENCH);
        as.addAttribute(READ, "r", 0, 5);
        AttributedCharacterIterator all = as.getIterator(null, 0, 5);
        check(all.getBeginIndex() == 0 && all.getEndIndex() == 5,
              "getIterator(null attrs, range): null means all attributes");
        check("r".equals(all.getAttribute(READ)), "null filter keeps attributes");
        expectX(IllegalArgumentException.class,
                () -> as.getIterator(new AttributedCharacterIterator.Attribute[] {LANG}, 0, 99),
                "getIterator(attrs, 0, 99) -> IAE");
        expectX(IllegalArgumentException.class,
                () -> as.getIterator(new AttributedCharacterIterator.Attribute[] {LANG}, 3, 1),
                "getIterator(attrs, start>end) -> IAE");
        AttributedCharacterIterator tail = as.getIterator(new AttributedCharacterIterator.Attribute[] {LANG}, 6, 11);
        check(tail.getBeginIndex() == 6 && tail.first() == 'w',
              "filtered range iterator positions at 6");
        check(tail.getAttribute(READ) == null, "filtered range hides unlisted attribute");
    }

    static void testTextReconstructionContract() {
        AttributedString as = new AttributedString("0123456789");
        as.addAttribute(LANG, "en", 0, 4);
        as.addAttribute(LANG, "fr", 4, 10);
        as.addAttribute(READ, "r1", 2, 8);
        AttributedCharacterIterator it = as.getIterator();
        StringBuilder sb = new StringBuilder();
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) sb.append(ch);
        check("0123456789".contentEquals(sb), "iterator reconstructs full text");
        it.setIndex(4);
        check("fr".equals(it.getAttribute(LANG)) && it.getRunStart() == 4,
              "adjacent same-attribute ranges split runs exactly at 4");
    }

    // ===================== 契约深化：addAttributes null/范围 null 值移除/run 边界/null 迭代器 =====================
    static void testNullAndRunBoundaryContract() {
        AttributedString base = new AttributedString("hello");
        expectX(NullPointerException.class, () -> base.addAttributes(null, 0, 1),
                "addAttributes(null Map, ...) -> NPE");
        // 范围 addAttribute 传 null 值：仅移除该范围内的属性值。
        AttributedString as = new AttributedString("abcdef");
        as.addAttribute(LANG, "en", 0, 6);
        as.addAttribute(LANG, null, 1, 3);
        AttributedCharacterIterator it = as.getIterator();
        it.setIndex(0);
        check("en".equals(it.getAttribute(LANG)), "range null removal leaves value outside");
        it.setIndex(1);
        check(it.getAttribute(LANG) == null, "range null value removes attribute in [1,3)");
        it.setIndex(3);
        check("en".equals(it.getAttribute(LANG)), "value restored after removed range");
        // null 属性数组迭代器 = 全文本全属性
        AttributedString whole = new AttributedString("01234");
        whole.addAttribute(LANG, "en");
        AttributedCharacterIterator nf = whole.getIterator((AttributedCharacterIterator.Attribute[]) null);
        check(nf.getBeginIndex() == 0 && nf.getEndIndex() == 5, "getIterator(null attrs) spans whole text");
        check("en".equals(nf.getAttribute(LANG)), "getIterator(null attrs) keeps attributes");
        // getAttribute(null) 不抛异常、返回 null
        check(whole.getIterator().getAttribute(null) == null, "getAttribute(null) returns null");
        // 重叠 run 的边界
        AttributedString ov = new AttributedString("0123456789");
        ov.addAttribute(LANG, "en", 0, 4);
        ov.addAttribute(LANG, "fr", 4, 10);
        ov.addAttribute(READ, "r", 2, 8);
        AttributedCharacterIterator oi = ov.getIterator();
        oi.setIndex(0);
        check(oi.getRunStart() == 0 && oi.getRunLimit() == 2, "run boundary at 0..2");
        oi.setIndex(2);
        check(oi.getRunStart() == 2 && oi.getRunLimit() == 4, "run boundary at 2..4");
        oi.setIndex(4);
        check(oi.getRunStart() == 4 && oi.getRunLimit() == 8, "run boundary at 4..8");
    }

    public static void main(String[] args) throws Exception {
        testConstructorMatrixContract();
        testAddAttributeRangeMatrixContract();
        testGetIteratorMatrixContract();
        testTextReconstructionContract();
        testNullAndRunBoundaryContract();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
