/*
 * @test
 * @summary java.time.MonthDay API conformance: month/day accessors and isValidYear.
 * @run main MonthDayTest
 */

import java.time.Month;
import java.time.MonthDay;

public class MonthDayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonthDay() {
        MonthDay md = MonthDay.of(Month.MAY, 17);
        check(md.getMonth() == Month.MAY, "month enum");
        check(md.getDayOfMonth() == 17, "day of month");
        MonthDay feb29 = MonthDay.of(Month.FEBRUARY, 29);
        check(feb29.isValidYear(2020), "Feb 29 valid in leap year");
        check(!feb29.isValidYear(2019), "Feb 29 invalid in non-leap year");
    }

    public static void main(String[] args) {
        testMonthDay();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
