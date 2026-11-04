/*
 * @test
 * @summary java.time.temporal.WeekFields conformance: ISO week fields.
 * @run main WeekFieldsTest
 */

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;

public class WeekFieldsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWeekFields() {
        WeekFields wf = WeekFields.ISO;
        check(wf.getFirstDayOfWeek() == DayOfWeek.MONDAY, "firstDayOfWeek MONDAY");
        check(wf.getMinimalDaysInFirstWeek() == 4, "minimalDaysInFirstWeek 4");
        check(wf.dayOfWeek() != null, "dayOfWeek");
        check(wf.weekOfMonth() != null, "weekOfMonth");
        check(wf.weekOfYear() != null, "weekOfYear");
        check(wf.weekOfWeekBasedYear() != null, "weekOfWeekBasedYear");
        check(wf.weekBasedYear() != null, "weekBasedYear");

        WeekFields sunday = WeekFields.of(DayOfWeek.SUNDAY, 1);
        check(sunday.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "of firstDayOfWeek SUNDAY");
        check(sunday.getMinimalDaysInFirstWeek() == 1, "of minimalDaysInFirstWeek 1");
    }

    public static void main(String[] args) {
        testWeekFields();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
