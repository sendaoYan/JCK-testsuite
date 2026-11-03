/*
 * @test
 * @summary java.math.RoundingMode conformance: white-box structure + black-box behavior
 *          (8 enum constants UP..UNNECESSARY, values/valueOf(String)/valueOf(int) legacy mapping,
 *           name/ordinal, IllegalArgumentException on unknown name/out-of-range int,
 *           per-mode rounding semantics via BigDecimal.round(MathContext)).
 * @run main RoundingModeTest
 */

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class RoundingModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.math.RoundingMode");

        check(c.isEnum(), "RoundingMode is an enum");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()),
              "RoundingMode is public final");
        check(c.getSuperclass() == Enum.class, "RoundingMode extends Enum");

        String[] names = {"UP", "DOWN", "CEILING", "FLOOR",
                          "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY"};
        RoundingMode[] expected = {RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                                   RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                                   RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY};
        for (int i = 0; i < names.length; i++) {
            check(c.getField(names[i]).getType() == RoundingMode.class
                  && c.getField(names[i]).get(null) == expected[i],
                  "constant " + names[i]);
        }
        // methods
        check(c.getMethod("values").getReturnType() == RoundingMode[].class, "values() returns array");
        check(c.getMethod("valueOf", String.class) != null, "valueOf(String)");
        check(c.getMethod("valueOf", int.class) != null, "valueOf(int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        constants();
        valueOfString();
        valueOfIntLegacy();
        semanticsViaBigDecimal();
        exceptions();
    }

    static void constants() {
        RoundingMode[] all = RoundingMode.values();
        check(all.length == 8, "8 constants");
        check(all[0] == RoundingMode.UP && all[1] == RoundingMode.DOWN, "ordinal UP/DOWN");
        check(all[2] == RoundingMode.CEILING && all[3] == RoundingMode.FLOOR, "ordinal CEILING/FLOOR");
        check(all[4] == RoundingMode.HALF_UP && all[5] == RoundingMode.HALF_DOWN
              && all[6] == RoundingMode.HALF_EVEN, "ordinal HALF_*");
        check(all[7] == RoundingMode.UNNECESSARY, "ordinal UNNECESSARY");
        check(RoundingMode.UP.name().equals("UP"), "name UP");
        check(RoundingMode.HALF_EVEN.name().equals("HALF_EVEN"), "name HALF_EVEN");
        check(RoundingMode.UP.ordinal() == 0 && RoundingMode.UNNECESSARY.ordinal() == 7, "ordinal range");
    }

    static void valueOfString() {
        check(RoundingMode.valueOf("UP") == RoundingMode.UP, "valueOf(UP)");
        check(RoundingMode.valueOf("DOWN") == RoundingMode.DOWN, "valueOf(DOWN)");
        check(RoundingMode.valueOf("CEILING") == RoundingMode.CEILING, "valueOf(CEILING)");
        check(RoundingMode.valueOf("FLOOR") == RoundingMode.FLOOR, "valueOf(FLOOR)");
        check(RoundingMode.valueOf("HALF_UP") == RoundingMode.HALF_UP, "valueOf(HALF_UP)");
        check(RoundingMode.valueOf("HALF_DOWN") == RoundingMode.HALF_DOWN, "valueOf(HALF_DOWN)");
        check(RoundingMode.valueOf("HALF_EVEN") == RoundingMode.HALF_EVEN, "valueOf(HALF_EVEN)");
        check(RoundingMode.valueOf("UNNECESSARY") == RoundingMode.UNNECESSARY, "valueOf(UNNECESSARY)");
    }

    static void valueOfIntLegacy() {
        // Legacy BigDecimal ROUND_* constants: UP=0 DOWN=1 CEILING=2 FLOOR=3
        // HALF_UP=4 HALF_DOWN=5 HALF_EVEN=6 UNNECESSARY=7
        check(RoundingMode.valueOf(0) == RoundingMode.UP, "valueOf(0)==UP");
        check(RoundingMode.valueOf(1) == RoundingMode.DOWN, "valueOf(1)==DOWN");
        check(RoundingMode.valueOf(2) == RoundingMode.CEILING, "valueOf(2)==CEILING");
        check(RoundingMode.valueOf(3) == RoundingMode.FLOOR, "valueOf(3)==FLOOR");
        check(RoundingMode.valueOf(4) == RoundingMode.HALF_UP, "valueOf(4)==HALF_UP");
        check(RoundingMode.valueOf(5) == RoundingMode.HALF_DOWN, "valueOf(5)==HALF_DOWN");
        check(RoundingMode.valueOf(6) == RoundingMode.HALF_EVEN, "valueOf(6)==HALF_EVEN");
        check(RoundingMode.valueOf(7) == RoundingMode.UNNECESSARY, "valueOf(7)==UNNECESSARY");
    }

    static void semanticsViaBigDecimal() {
        // Each mode's documented rounding table, applied to one-digit precision.
        MathContext up = new MathContext(1, RoundingMode.UP);
        MathContext down = new MathContext(1, RoundingMode.DOWN);
        MathContext ceiling = new MathContext(1, RoundingMode.CEILING);
        MathContext floor = new MathContext(1, RoundingMode.FLOOR);
        MathContext halfUp = new MathContext(1, RoundingMode.HALF_UP);
        MathContext halfDown = new MathContext(1, RoundingMode.HALF_DOWN);
        MathContext halfEven = new MathContext(1, RoundingMode.HALF_EVEN);

        // 5.5 -> 6 / 5 / 6 / 5 / 6 / 5 / 6
        check(new BigDecimal("5.5").round(up).equals(new BigDecimal("6")), "UP 5.5");
        check(new BigDecimal("5.5").round(down).equals(new BigDecimal("5")), "DOWN 5.5");
        check(new BigDecimal("5.5").round(ceiling).equals(new BigDecimal("6")), "CEILING 5.5");
        check(new BigDecimal("5.5").round(floor).equals(new BigDecimal("5")), "FLOOR 5.5");
        check(new BigDecimal("5.5").round(halfUp).equals(new BigDecimal("6")), "HALF_UP 5.5");
        check(new BigDecimal("5.5").round(halfDown).equals(new BigDecimal("5")), "HALF_DOWN 5.5");
        check(new BigDecimal("5.5").round(halfEven).equals(new BigDecimal("6")), "HALF_EVEN 5.5");

        // 2.5 -> 3 / 2 / 3 / 2 / 3 / 2 / 2 (HALF_EVEN ties to even)
        check(new BigDecimal("2.5").round(halfUp).equals(new BigDecimal("3")), "HALF_UP 2.5");
        check(new BigDecimal("2.5").round(halfDown).equals(new BigDecimal("2")), "HALF_DOWN 2.5");
        check(new BigDecimal("2.5").round(halfEven).equals(new BigDecimal("2")), "HALF_EVEN 2.5 -> 2");
        check(new BigDecimal("3.5").round(halfEven).equals(new BigDecimal("4")), "HALF_EVEN 3.5 -> 4");

        // negative values
        check(new BigDecimal("-1.1").round(ceiling).equals(new BigDecimal("-1")), "CEILING -1.1");
        check(new BigDecimal("-1.1").round(floor).equals(new BigDecimal("-2")), "FLOOR -1.1");
        check(new BigDecimal("-1.6").round(up).equals(new BigDecimal("-2")), "UP -1.6");
        check(new BigDecimal("-1.6").round(down).equals(new BigDecimal("-1")), "DOWN -1.6");
        check(new BigDecimal("-2.5").round(halfEven).equals(new BigDecimal("-2")), "HALF_EVEN -2.5 -> -2");

        // UNNECESSARY is exact
        check(new BigDecimal("1.0").round(new MathContext(1, RoundingMode.UNNECESSARY))
              .equals(new BigDecimal("1")), "UNNECESSARY exact");
    }

    static void exceptions() {
        expectIllegalArgument(() -> RoundingMode.valueOf("BOGUS"), "valueOf unknown name");
        expectIllegalArgument(() -> RoundingMode.valueOf("up"), "valueOf lowercase name");
        expectIllegalArgument(() -> RoundingMode.valueOf(-1), "valueOf negative int");
        expectIllegalArgument(() -> RoundingMode.valueOf(8), "valueOf int above 7");
        expectArithmetic(() -> new BigDecimal("1.1").round(new MathContext(1, RoundingMode.UNNECESSARY)),
              "UNNECESSARY on inexact value");
    }

    static void expectIllegalArgument(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected IllegalArgumentException: " + msg);
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void expectArithmetic(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected ArithmeticException: " + msg);
        } catch (ArithmeticException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
