/*
 * @test
 * @summary java.time.LocalDate API conformance: of/getters, isLeapYear, lengthOfMonth,
 *          getDayOfWeek, plusDays/minusDays, isAfter/isBefore, and parse.
 * @run main LocalDateTest
 */

import java.time.DayOfWeek;
import java.time.LocalDate;

public class LocalDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLocalDate() {
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.getYear() == 2024 && d.getMonthValue() == 2 && d.getDayOfMonth() == 29,
              "of/getters");
        check(d.isLeapYear(), "isLeapYear");
        check(d.lengthOfMonth() == 29, "lengthOfMonth");
        check(d.getDayOfWeek() == DayOfWeek.THURSDAY, "getDayOfWeek");
        check(d.plusDays(1).equals(LocalDate.of(2024, 3, 1)), "plusDays");
        check(d.minusDays(1).equals(LocalDate.of(2024, 2, 28)), "minusDays");
        check(d.isAfter(LocalDate.of(2024, 2, 28)) && d.isBefore(LocalDate.of(2024, 3, 1)),
              "isAfter/isBefore");
        check(LocalDate.parse("2024-02-29").equals(d), "parse");
        check(LocalDate.of(2023, 2, 28).lengthOfMonth() == 28, "lengthOfMonth non-leap");
    }

    public static void main(String[] args) {
        testLocalDate();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
