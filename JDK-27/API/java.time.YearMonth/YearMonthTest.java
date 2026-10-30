/*
 * @test
 * @summary java.time.YearMonth API conformance: of/getters, lengthOfMonth, isLeapYear, and
 *          atDay.
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
        check(ym.getYear() == 2024 && ym.getMonth() == Month.FEBRUARY, "of/getters");
        check(ym.lengthOfMonth() == 29, "lengthOfMonth leap");
        check(ym.isLeapYear(), "isLeapYear");
        check(YearMonth.of(2023, 2).lengthOfMonth() == 28, "lengthOfMonth non-leap");
        check(ym.atDay(15).equals(LocalDate.of(2024, 2, 15)), "atDay");
    }

    public static void main(String[] args) {
        testYearMonth();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
