/*
 * @test
 * @summary java.time.DayOfWeek enum conformance: getValue, of, and plus (with wrap-around).
 * @run main DayOfWeekTest
 */

import java.time.DayOfWeek;

public class DayOfWeekTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDayOfWeek() {
        check(DayOfWeek.MONDAY.getValue() == 1 && DayOfWeek.SUNDAY.getValue() == 7, "getValue");
        check(DayOfWeek.of(1) == DayOfWeek.MONDAY, "of");
        check(DayOfWeek.MONDAY.plus(1) == DayOfWeek.TUESDAY, "plus");
        check(DayOfWeek.MONDAY.plus(7) == DayOfWeek.MONDAY, "plus wraps");
    }

    public static void main(String[] args) {
        testDayOfWeek();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
