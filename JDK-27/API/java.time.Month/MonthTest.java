/*
 * @test
 * @summary java.time.Month enum conformance: getValue, of, length (leap/non-leap), and plus.
 * @run main MonthTest
 */

import java.time.Month;

public class MonthTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonth() {
        check(Month.JANUARY.getValue() == 1 && Month.DECEMBER.getValue() == 12, "getValue");
        check(Month.of(1) == Month.JANUARY && Month.of(12) == Month.DECEMBER, "of");
        check(Month.FEBRUARY.length(false) == 28 && Month.FEBRUARY.length(true) == 29, "length");
        check(Month.JANUARY.length(false) == 31, "JANUARY length");
        check(Month.JANUARY.plus(1) == Month.FEBRUARY, "plus");
    }

    public static void main(String[] args) {
        testMonth();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
