/*
 * @test
 * @summary java.time.temporal.TemporalUnit conformance: via ChronoUnit.DAYS.
 * @run main TemporalUnitTest
 */

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class TemporalUnitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalUnit() {
        TemporalUnit days = ChronoUnit.DAYS;
        check(days.isDateBased(), "DAYS isDateBased");
        check(!days.isTimeBased(), "DAYS not isTimeBased");
        check(days.isDurationEstimated(), "DAYS isDurationEstimated");
        check(days.getDuration().equals(Duration.ofDays(1)), "DAYS duration");
        check(days.isSupportedBy(LocalDate.now()), "DAYS supported by LocalDate");
        check(LocalDate.of(2020, 1, 1).plus(1, days).equals(LocalDate.of(2020, 1, 2)), "DAYS plus");

        TemporalUnit hours = ChronoUnit.HOURS;
        check(hours.isTimeBased(), "HOURS isTimeBased");
        check(!hours.isDurationEstimated(), "HOURS not isDurationEstimated");
        check(hours.getDuration().equals(Duration.ofHours(1)), "HOURS duration");
    }

    public static void main(String[] args) {
        testTemporalUnit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
