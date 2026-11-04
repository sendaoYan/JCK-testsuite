/*
 * @test
 * @summary java.time.MonthDay API conformance: of/getters, isValidYear, and atYear.
 * @run main MonthDayTest
 */

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;

public class MonthDayTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonthDay() {
        MonthDay md = MonthDay.of(Month.FEBRUARY, 29);
        check(md.getMonth() == Month.FEBRUARY && md.getDayOfMonth() == 29, "of/getters");
        check(md.isValidYear(2024) && !md.isValidYear(2023), "isValidYear");
        check(md.atYear(2024).equals(LocalDate.of(2024, 2, 29)), "atYear");
    }

    public static void main(String[] args) {
        testMonthDay();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
