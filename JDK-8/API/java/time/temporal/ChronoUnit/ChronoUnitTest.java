/*
 * @test
 * @summary java.time.temporal.ChronoUnit API conformance: date/time classification and
 *          unit duration.
 * @run main ChronoUnitTest
 */

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ChronoUnitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoUnit() {
        check(ChronoUnit.DAYS.isDateBased(), "DAYS date-based");
        check(!ChronoUnit.DAYS.isTimeBased(), "DAYS not time-based");
        check(ChronoUnit.HOURS.isTimeBased(), "HOURS time-based");
        check(!ChronoUnit.HOURS.isDateBased(), "HOURS not date-based");
        check(ChronoUnit.DAYS.getDuration().equals(Duration.ofDays(1)), "DAYS duration");
        check(ChronoUnit.HOURS.getDuration().equals(Duration.ofHours(1)),
              "HOURS duration");
    }

    public static void main(String[] args) {
        testChronoUnit();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
