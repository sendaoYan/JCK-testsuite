/*
 * @test
 * @summary java.time.temporal.ChronoField API conformance: field range and
 *          date/time classification.
 * @run main ChronoFieldTest
 */

import java.time.temporal.ChronoField;

public class ChronoFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoField() {
        ChronoField dom = ChronoField.DAY_OF_MONTH;
        check(dom.range().getMinimum() == 1, "day-of-month min");
        check(dom.range().getMaximum() == 31, "day-of-month max");
        check(dom.isDateBased(), "day-of-month date-based");
        check(!dom.isTimeBased(), "day-of-month not time-based");

        ChronoField hod = ChronoField.HOUR_OF_DAY;
        check(hod.range().getMaximum() == 23, "hour-of-day max");
        check(hod.isTimeBased(), "hour-of-day time-based");
    }

    public static void main(String[] args) {
        testChronoField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
