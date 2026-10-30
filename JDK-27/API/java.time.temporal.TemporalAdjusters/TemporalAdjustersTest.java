/*
 * @test
 * @summary java.time.temporal.TemporalAdjusters API conformance: first/last day and next
 *          day-of-week.
 * @run main TemporalAdjustersTest
 */

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class TemporalAdjustersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAdjusters() {
        LocalDate d = LocalDate.of(2024, 6, 15); // a Saturday
        check(d.with(TemporalAdjusters.firstDayOfMonth())
                  .equals(LocalDate.of(2024, 6, 1)),
              "firstDayOfMonth");
        check(d.with(TemporalAdjusters.lastDayOfMonth())
                  .equals(LocalDate.of(2024, 6, 30)),
              "lastDayOfMonth");
        check(d.with(TemporalAdjusters.firstDayOfNextMonth())
                  .equals(LocalDate.of(2024, 7, 1)),
              "firstDayOfNextMonth");
        check(d.with(TemporalAdjusters.firstDayOfYear())
                  .equals(LocalDate.of(2024, 1, 1)),
              "firstDayOfYear");
        check(d.with(TemporalAdjusters.lastDayOfYear())
                  .equals(LocalDate.of(2024, 12, 31)),
              "lastDayOfYear");
        check(d.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                  .equals(LocalDate.of(2024, 6, 17)),
              "next Monday");
    }

    public static void main(String[] args) {
        testTemporalAdjusters();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
