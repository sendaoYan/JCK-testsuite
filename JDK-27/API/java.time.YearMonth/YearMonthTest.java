/*
 * @test
 * @summary java.time.YearMonth API conformance: year/month accessors, length, and atDay.
 * @run main YearMonthTest
 */

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class YearMonthTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testYearMonth() {
        YearMonth ym = YearMonth.of(2024, 2);
        check(ym.getYear() == 2024, "year");
        check(ym.getMonth() == Month.FEBRUARY, "month enum");
        check(ym.getMonthValue() == 2, "month value");
        check(ym.isLeapYear(), "isLeapYear 2024");
        check(ym.lengthOfMonth() == 29, "lengthOfMonth leap");
        check(ym.atDay(1).equals(LocalDate.of(2024, 2, 1)), "atDay");
    }

    public static void main(String[] args) {
        testYearMonth();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
