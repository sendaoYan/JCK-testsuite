/*
 * @test
 * @summary java.time.temporal.TemporalAdjusters conformance: white-box structure + black-box behavior.
 * @run main TemporalAdjustersTest
 */

import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

public class TemporalAdjustersTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalAdjusters");
        check(Modifier.isFinal(c.getModifiers()), "is final utility class");
        check(c.getConstructors().length == 0, "no public constructor");
        for (String n : new String[] {
                "ofDateAdjuster", "firstDayOfMonth", "lastDayOfMonth", "firstDayOfNextMonth",
                "firstDayOfYear", "lastDayOfYear", "firstDayOfNextYear", "firstInMonth",
                "lastInMonth", "dayOfWeekInMonth", "next", "nextOrSame", "previous", "previousOrSame"}) {
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)), "method " + n);
        }
        check(TemporalAdjusters.firstDayOfMonth() instanceof TemporalAdjuster, "firstDayOfMonth returns TemporalAdjuster");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LocalDate d = LocalDate.of(2024, 6, 15); // a Saturday
        check(d.getDayOfWeek() == DayOfWeek.SATURDAY, "2024-06-15 is Saturday");

        check(d.with(TemporalAdjusters.firstDayOfMonth()).equals(LocalDate.of(2024, 6, 1)), "firstDayOfMonth");
        check(d.with(TemporalAdjusters.lastDayOfMonth()).equals(LocalDate.of(2024, 6, 30)), "lastDayOfMonth");
        check(d.with(TemporalAdjusters.firstDayOfNextMonth()).equals(LocalDate.of(2024, 7, 1)), "firstDayOfNextMonth");
        check(d.with(TemporalAdjusters.firstDayOfYear()).equals(LocalDate.of(2024, 1, 1)), "firstDayOfYear");
        check(d.with(TemporalAdjusters.lastDayOfYear()).equals(LocalDate.of(2024, 12, 31)), "lastDayOfYear");
        check(d.with(TemporalAdjusters.firstDayOfNextYear()).equals(LocalDate.of(2025, 1, 1)), "firstDayOfNextYear");

        check(d.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).equals(LocalDate.of(2024, 6, 17)), "next Monday");
        check(d.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).equals(LocalDate.of(2024, 6, 10)), "previous Monday");
        check(d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)).equals(LocalDate.of(2024, 6, 15)), "nextOrSame Saturday");
        check(d.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).equals(LocalDate.of(2024, 6, 15)), "previousOrSame Saturday");
        check(d.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).equals(LocalDate.of(2024, 6, 16)), "nextOrSame Sunday");

        check(d.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)).equals(LocalDate.of(2024, 6, 3)), "firstInMonth Monday");
        check(d.with(TemporalAdjusters.lastInMonth(DayOfWeek.SUNDAY)).equals(LocalDate.of(2024, 6, 30)), "lastInMonth Sunday");
        check(d.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.WEDNESDAY)).equals(LocalDate.of(2024, 6, 12)), "dayOfWeekInMonth 2nd Wednesday");

        // ofDateAdjuster
        check(d.with(TemporalAdjusters.ofDateAdjuster(x -> x.plusDays(2))).equals(LocalDate.of(2024, 6, 17)), "ofDateAdjuster +2 days");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
