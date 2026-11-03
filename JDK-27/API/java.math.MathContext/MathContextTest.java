/*
 * @test
 * @summary java.math.MathContext conformance: white-box structure + black-box behavior
 *          (precision/rounding-mode getters, DECIMAL32/64/128/UNLIMITED constants,
 *           (int)/(int,RoundingMode)/(String) constructors, equals/hashCode/toString round-trip,
 *           IllegalArgumentException/NullPointerException on invalid inputs).
 * @run main MathContextTest
 */

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

public class MathContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.math.MathContext");

        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()),
              "MathContext is public final");
        check(!c.isInterface() && !c.isEnum(), "MathContext is a concrete class");
        check(Arrays.asList(c.getInterfaces()).contains(Serializable.class),
              "MathContext implements Serializable");

        for (String f : new String[]{"UNLIMITED", "DECIMAL32", "DECIMAL64", "DECIMAL128"}) {
            Field field = c.getField(f);
            check(Modifier.isPublic(field.getModifiers())
                  && Modifier.isStatic(field.getModifiers())
                  && Modifier.isFinal(field.getModifiers())
                  && field.getType() == MathContext.class,
                  "static final MathContext " + f);
        }

        String[][] methods = {
            {"getPrecision"},
            {"getRoundingMode"},
            {"equals", "java.lang.Object"},
            {"hashCode"},
            {"toString"},
        };
        for (String[] m : methods) {
            Class<?>[] params = (m.length == 1) ? new Class<?>[0] : new Class<?>[]{Class.forName(m[1])};
            Method method = c.getMethod(m[0], params);
            check(Modifier.isPublic(method.getModifiers()), "public method " + m[0]);
        }
        // constructors
        check(c.getConstructor(int.class) != null, "constructor (int)");
        check(c.getConstructor(int.class, RoundingMode.class) != null, "constructor (int, RoundingMode)");
        check(c.getConstructor(String.class) != null, "constructor (String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        constants();
        constructors();
        getters();
        equalityAndString();
        exceptions();
    }

    static void constants() {
        check(MathContext.DECIMAL32.getPrecision() == 7, "DECIMAL32 precision 7");
        check(MathContext.DECIMAL64.getPrecision() == 16, "DECIMAL64 precision 16");
        check(MathContext.DECIMAL128.getPrecision() == 34, "DECIMAL128 precision 34");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED precision 0");
        check(MathContext.DECIMAL32.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL32 HALF_EVEN");
        check(MathContext.DECIMAL64.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL64 HALF_EVEN");
        check(MathContext.DECIMAL128.getRoundingMode() == RoundingMode.HALF_EVEN, "DECIMAL128 HALF_EVEN");
        check(MathContext.UNLIMITED.getRoundingMode() == RoundingMode.HALF_UP, "UNLIMITED HALF_UP");
    }

    static void constructors() {
        check(new MathContext(10).getPrecision() == 10, "(int) ctor precision");
        check(new MathContext(10).getRoundingMode() == RoundingMode.HALF_UP,
              "(int) ctor default rounding HALF_UP");
        check(new MathContext(0).getPrecision() == 0, "(int) ctor zero precision allowed");
        check(new MathContext(10, RoundingMode.HALF_EVEN).getPrecision() == 10, "(int, mode) precision");
        check(new MathContext(10, RoundingMode.HALF_EVEN).getRoundingMode() == RoundingMode.HALF_EVEN,
              "(int, mode) rounding mode");
        // String constructor parses toString() output
        MathContext parsed = new MathContext("precision=9 roundingMode=HALF_UP");
        check(parsed.getPrecision() == 9 && parsed.getRoundingMode() == RoundingMode.HALF_UP,
              "String ctor round-trip");
        check(new MathContext("precision=0 roundingMode=HALF_UP").getPrecision() == 0,
              "String ctor zero precision");
    }

    static void getters() {
        MathContext mc = new MathContext(12, RoundingMode.CEILING);
        check(mc.getPrecision() == 12, "getPrecision");
        check(mc.getRoundingMode() == RoundingMode.CEILING, "getRoundingMode");
        check(MathContext.UNLIMITED.getPrecision() == 0, "UNLIMITED getPrecision");
    }

    static void equalityAndString() {
        check(new MathContext(10, RoundingMode.HALF_UP).equals(new MathContext(10, RoundingMode.HALF_UP)),
              "equals same settings");
        check(!new MathContext(10, RoundingMode.HALF_UP).equals(new MathContext(10, RoundingMode.HALF_EVEN)),
              "equals different rounding");
        check(!new MathContext(10).equals(new MathContext(11)), "equals different precision");
        check(!new MathContext(10).equals("10"), "equals against String false");
        check(!new MathContext(10).equals(null), "equals null false");
        check(new MathContext(10, RoundingMode.HALF_UP).hashCode()
              == new MathContext(10, RoundingMode.HALF_UP).hashCode(), "equal objects equal hashCode");
        check(MathContext.DECIMAL32.toString().equals("precision=7 roundingMode=HALF_EVEN"),
              "DECIMAL32 toString");
        check(MathContext.UNLIMITED.toString().equals("precision=0 roundingMode=HALF_UP"),
              "UNLIMITED toString");
        check(new MathContext(9, RoundingMode.HALF_UP).toString()
              .equals("precision=9 roundingMode=HALF_UP"), "toString format");
        // round-trip: parse toString() of arbitrary context
        MathContext orig = new MathContext(5, RoundingMode.DOWN);
        MathContext copy = new MathContext(orig.toString());
        check(copy.equals(orig), "toString/parse round-trip");
    }

    static void exceptions() {
        expectIllegalArgument(() -> new MathContext(-1), "negative precision");
        expectIllegalArgument(() -> new MathContext(-1, RoundingMode.HALF_UP), "negative precision with mode");
        expectNullPointer(() -> new MathContext(10, (RoundingMode) null), "null rounding mode");
        expectNullPointer(() -> new MathContext((String) null), "null String");
        expectIllegalArgument(() -> new MathContext("bad"), "bad String format");
        expectIllegalArgument(() -> new MathContext("precision=-1 roundingMode=HALF_UP"),
              "String with negative precision");
        expectIllegalArgument(() -> new MathContext("precision=abc roundingMode=HALF_UP"),
              "String with non-numeric precision");
        expectIllegalArgument(() -> new MathContext("precision=5 roundingMode=BOGUS"),
              "String with unknown rounding mode");
    }

    static void expectIllegalArgument(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + msg);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void expectNullPointer(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected NullPointerException: " + msg);
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
