/*
 * @test
 * @summary java.math.RoundingMode enum conformance: 8 constants, valueOf, and ordinal.
 * @run main RoundingModeTest
 */

import java.math.RoundingMode;

public class RoundingModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRoundingMode() {
        check(RoundingMode.values().length == 8, "8 constants");
        check(RoundingMode.valueOf("HALF_UP") == RoundingMode.HALF_UP, "valueOf");
        check(RoundingMode.HALF_UP.ordinal() >= 0, "ordinal non-negative");
    }

    public static void main(String[] args) {
        testRoundingMode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
