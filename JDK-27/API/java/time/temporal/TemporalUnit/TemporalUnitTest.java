/*
 * @test
 * @summary java.time.temporal.TemporalUnit conformance: white-box structure + black-box behavior.
 * @run main TemporalUnitTest
 */

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

public class TemporalUnitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalUnit");
        check(c.isInterface(), "is an interface");
        check(Modifier.isInterface(c.getModifiers()), "modifier is interface");
        check(c.getMethod("getDuration").getReturnType() == Duration.class, "getDuration returns Duration");
        check(c.getMethod("isDurationEstimated").getReturnType() == boolean.class, "isDurationEstimated returns boolean");
        check(c.getMethod("addTo", Temporal.class, long.class).getReturnType() == Temporal.class, "addTo returns Temporal");
        check(c.getMethod("between", Temporal.class, Temporal.class).getReturnType() == long.class, "between returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // DAYS via the interface
        TemporalUnit days = ChronoUnit.DAYS;
        check(days.isDateBased(), "DAYS isDateBased");
        check(!days.isTimeBased(), "DAYS not isTimeBased");
        check(days.isDurationEstimated(), "DAYS isDurationEstimated");
        check(days.getDuration().equals(Duration.ofDays(1)), "DAYS duration");
        check(days.isSupportedBy(LocalDate.of(2020, 1, 1)), "DAYS supported by LocalDate");
        check(LocalDate.of(2020, 1, 1).plus(1, days).equals(LocalDate.of(2020, 1, 2)), "DAYS plus");
        check(days.between(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 5)) == 4, "DAYS between");

        // HOURS via the interface
        TemporalUnit hours = ChronoUnit.HOURS;
        check(hours.isTimeBased(), "HOURS isTimeBased");
        check(!hours.isDurationEstimated(), "HOURS not isDurationEstimated");
        check(hours.getDuration().equals(Duration.ofHours(1)), "HOURS duration");
        check(hours.isSupportedBy(LocalTime.of(10, 0)), "HOURS supported by LocalTime");
        check(hours.addTo(LocalTime.of(10, 0), 2).equals(LocalTime.of(12, 0)), "HOURS addTo");
        check(hours.between(LocalTime.of(10, 0), LocalTime.of(13, 0)) == 3, "HOURS between");

        // NANOS and addTo of a date-based unit via interface
        TemporalUnit months = ChronoUnit.MONTHS;
        check(months.isDateBased(), "MONTHS isDateBased");
        check(months.addTo(LocalDate.of(2024, 1, 31), 1).equals(LocalDate.of(2024, 2, 29)), "MONTHS addTo");
        check(months.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 1)) == 3, "MONTHS between");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
