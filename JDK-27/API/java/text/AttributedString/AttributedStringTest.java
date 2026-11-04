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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
