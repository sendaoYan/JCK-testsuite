/*
 * @test
 * @summary java.math.RoundingMode enum conformance: full contract coverage.
 *          8 constants with stable ordinals, values() order, valueOf(int)
 *          mapping for every mode, valueOf(String), name()/toString, and exact
 *          exception matrix (IllegalArgumentException / NPE).
 * @run main RoundingModeTest
 */

import java.math.RoundingMode;

public class RoundingModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testConstants() {
        check(RoundingMode.values().length == 8, "8 constants");
        // documented ordinal order
        RoundingMode[] expected = { RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
                RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
                RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY };
        RoundingMode[] actual = RoundingMode.values();
        for (int i = 0; i < expected.length; i++) {
            check(actual[i] == expected[i], "ordinal " + i + " == " + expected[i]);
            check(actual[i].ordinal() == i, "ordinal() of " + expected[i]);
        }
    }

    static void testValueOfInt() {
        check(RoundingMode.valueOf(0) == RoundingMode.UP, "valueOf(0)");
        check(RoundingMode.valueOf(1) == RoundingMode.DOWN, "valueOf(1)");
        check(RoundingMode.valueOf(2) == RoundingMode.CEILING, "valueOf(2)");
        check(RoundingMode.valueOf(3) == RoundingMode.FLOOR, "valueOf(3)");
        check(RoundingMode.valueOf(4) == RoundingMode.HALF_UP, "valueOf(4)");
        check(RoundingMode.valueOf(5) == RoundingMode.HALF_DOWN, "valueOf(5)");
        check(RoundingMode.valueOf(6) == RoundingMode.HALF_EVEN, "valueOf(6)");
        check(RoundingMode.valueOf(7) == RoundingMode.UNNECESSARY, "valueOf(7)");
        check(RoundingMode.UP.ordinal() == 0 && RoundingMode.UNNECESSARY.ordinal() == 7,
                "ordinal bounds");
    }

    static void testValueOfString() {
        for (RoundingMode m : RoundingMode.values()) {
            check(RoundingMode.valueOf(m.name()) == m, "valueOf(String) " + m);
        }
        check(RoundingMode.valueOf("HALF_UP").toString().equals("HALF_UP"), "name/toString");
        check(RoundingMode.HALF_UP.name().equals("HALF_UP"), "name");
    }

    static void testErrors() {
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf(-1), "valueOf(-1)");
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf(8), "valueOf(8)");
        checkThrows(IllegalArgumentException.class, () -> RoundingMode.valueOf("FOO"),
                "valueOf bad name");
        checkThrows(NullPointerException.class, () -> RoundingMode.valueOf((String) null),
                "valueOf null String");
    }

    public static void main(String[] args) {
        testConstants();
        testValueOfInt();
        testValueOfString();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
