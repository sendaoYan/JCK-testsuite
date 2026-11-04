/*
 * @test
 * @summary java.nio.charset.CodingErrorAction conformance: white-box structure + black-box constants.
 * @run main CodingErrorActionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class CodingErrorActionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.charset.CodingErrorAction");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "superclass is Object");

        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        check(Modifier.isPrivate(ctors[0].getModifiers()), "constructor is private");

        int constants = 0;
        for (Field f : c.getFields()) {
            if (f.getType() == CodingErrorAction.class && Modifier.isStatic(f.getModifiers())) {
                constants++;
            }
        }
        check(constants == 3, "exactly three public constants");
    }

    static void testBlackBox() {
        check("IGNORE".equals(CodingErrorAction.IGNORE.toString()), "IGNORE");
        check("REPLACE".equals(CodingErrorAction.REPLACE.toString()), "REPLACE");
        check("REPORT".equals(CodingErrorAction.REPORT.toString()), "REPORT");
        check(CodingErrorAction.IGNORE != CodingErrorAction.REPLACE, "IGNORE != REPLACE");
        check(CodingErrorAction.REPLACE != CodingErrorAction.REPORT, "REPLACE != REPORT");
        check(CodingErrorAction.IGNORE != CodingErrorAction.REPORT, "IGNORE != REPORT");
        check(CodingErrorAction.IGNORE.equals(CodingErrorAction.IGNORE), "constant identity/equality");
    }

    // ================ A3 batch appended: non-enum structure, constant identity,
    // ================ toString distinctness, decoder/encoder configuration round-trips ================

    static void testNotAnEnumA3() throws Exception {
        Class<?> c = CodingErrorAction.class;
        check(!c.isEnum(), "A3 CodingErrorAction is not an enum");
        check(!Enum.class.isAssignableFrom(c), "A3 not assignable to java.lang.Enum");
        check(!Enum.class.isInstance(CodingErrorAction.REPORT), "A3 REPORT not an Enum instance");
        // A typesafe enum without the enum machinery: no values()/valueOf(String).
        try {
            c.getMethod("values");
            check(false, "A3 values() must not exist");
        } catch (NoSuchMethodException e) {
            check(true, "A3 no values() method");
        }
        try {
            c.getMethod("valueOf", String.class);
            check(false, "A3 valueOf(String) must not exist");
        } catch (NoSuchMethodException e) {
            check(true, "A3 no valueOf(String) method");
        }
    }

    static void testConstantsA3() {
        CodingErrorAction[] all = {CodingErrorAction.REPORT, CodingErrorAction.REPLACE, CodingErrorAction.IGNORE};
        for (CodingErrorAction a : all) {
            check(a.equals(a) && a.hashCode() == a.hashCode(), "A3 reflexive equals/hashCode " + a);
            check(!a.equals(null), "A3 not equal to null " + a);
            check(!a.equals(a.toString()), "A3 not equal to foreign type " + a);
        }
        check(CodingErrorAction.REPORT == CodingErrorAction.REPORT
              && CodingErrorAction.REPLACE == CodingErrorAction.REPLACE
              && CodingErrorAction.IGNORE == CodingErrorAction.IGNORE, "A3 constant identity");
        check(!CodingErrorAction.REPORT.toString().equals(CodingErrorAction.REPLACE.toString()),
              "A3 toString distinct REPORT/REPLACE");
        check(!CodingErrorAction.REPLACE.toString().equals(CodingErrorAction.IGNORE.toString()),
              "A3 toString distinct REPLACE/IGNORE");
        check(!CodingErrorAction.REPORT.toString().equals(CodingErrorAction.IGNORE.toString()),
              "A3 toString distinct REPORT/IGNORE");
        // The three constants are mutually non-equal objects.
        check(!CodingErrorAction.REPORT.equals(CodingErrorAction.REPLACE), "A3 REPORT != REPLACE (equals)");
        check(!CodingErrorAction.REPLACE.equals(CodingErrorAction.IGNORE), "A3 REPLACE != IGNORE (equals)");
    }

    static void testCoderRoundTripA3() {
        CodingErrorAction[] acts = {CodingErrorAction.REPORT, CodingErrorAction.REPLACE, CodingErrorAction.IGNORE};
        for (CodingErrorAction a : acts) {
            CharsetDecoder d = StandardCharsets.UTF_8.newDecoder();
            check(d.onMalformedInput(a) == d, "A3 decoder onMalformedInput returns this " + a);
            check(d.malformedInputAction() == a, "A3 decoder malformed getter " + a);
            CharsetEncoder e = StandardCharsets.UTF_8.newEncoder();
            check(e.onUnmappableCharacter(a) == e, "A3 encoder onUnmappableCharacter returns this " + a);
            check(e.unmappableCharacterAction() == a, "A3 encoder unmappable getter " + a);
        }
        // Actions can be switched on a live coder.
        CharsetDecoder d = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE);
        check(d.malformedInputAction() == CodingErrorAction.IGNORE, "A3 decoder action IGNORE set");
        d.onMalformedInput(CodingErrorAction.REPORT);
        check(d.malformedInputAction() == CodingErrorAction.REPORT, "A3 decoder action switched to REPORT");
        CharsetEncoder e = StandardCharsets.US_ASCII.newEncoder()
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        check(e.unmappableCharacterAction() == CodingErrorAction.REPLACE, "A3 encoder action REPLACE set");
        e.onUnmappableCharacter(CodingErrorAction.IGNORE);
        check(e.unmappableCharacterAction() == CodingErrorAction.IGNORE, "A3 encoder action switched to IGNORE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNotAnEnumA3();
        testConstantsA3();
        testCoderRoundTripA3();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
