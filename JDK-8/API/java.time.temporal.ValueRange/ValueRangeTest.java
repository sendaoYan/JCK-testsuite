/*
 * @test
 * @summary java.time.temporal.ValueRange API conformance: min/max and validity checks.
 * @run main ValueRangeTest
 */

import java.time.temporal.ValueRange;

public class ValueRangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testValueRange() {
        ValueRange vr = ValueRange.of(1, 12);
        check(vr.getMinimum() == 1, "minimum");
        check(vr.getMaximum() == 12, "maximum");
        check(vr.isValidValue(1), "1 valid");
        check(vr.isValidValue(12), "12 valid");
        check(!vr.isValidValue(0), "0 invalid");
        check(!vr.isValidValue(13), "13 invalid");

        ValueRange vr2 = ValueRange.of(1, 28, 31);
        check(vr2.getLargestMinimum() == 1, "largest minimum");
        check(vr2.getSmallestMaximum() == 28, "smallest maximum");
        check(vr2.isValidValue(29), "29 valid");
        check(vr2.isValidIntValue(31), "31 valid int");
    }

    public static void main(String[] args) {
        testValueRange();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
