/*
 * @test
 * @summary java.time.temporal.ChronoUnit conformance: white-box structure + black-box behavior.
 * @run main ChronoUnitTest
 */

import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ChronoUnitTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.ChronoUnit");
        check(c.isEnum(), "is an enum");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        boolean implementsTemporalUnit = false;
        for (Class<?> i : c.getInterfaces()) if (i == TemporalUnit.class) implementsTemporalUnit = true;
        check(implementsTemporalUnit, "implements TemporalUnit");
        check(c.getEnumConstants().length == 16, "16 constants");
        check(ChronoUnit.valueOf("DAYS") == ChronoUnit.DAYS, "valueOf DAYS round-trip");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // durations
        check(ChronoUnit.DAYS.getDuration().equals(Duration.ofSeconds(86400)), "DAYS == 86400s");
        check(ChronoUnit.HOURS.getDuration().equals(Duration.ofSeconds(3600)), "HOURS == 3600s");
        check(ChronoUnit.MINUTES.getDuration().equals(Duration.ofSeconds(60)), "MINUTES == 60s");

        // duration estimation (DST makes days estimated; hours are exact)
        check(ChronoUnit.DAYS.isDurationEstimated(), "DAYS.isDurationEstimated() is true");
        check(!ChronoUnit.HOURS.isDurationEstimated(), "HOURS.isDurationEstimated() is false");

        // date/time classification
        check(ChronoUnit.DAYS.isDateBased() && !ChronoUnit.DAYS.isTimeBased(), "DAYS date-based only");
        check(ChronoUnit.HOURS.isTimeBased() && !ChronoUnit.HOURS.isDateBased(), "HOURS time-based only");
        check(!ChronoUnit.FOREVER.isDateBased() && !ChronoUnit.FOREVER.isTimeBased(), "FOREVER neither");

        // arithmetic
        check(ChronoUnit.MONTHS.addTo(LocalDate.of(2024, 1, 31), 1).equals(LocalDate.of(2024, 2, 29)), "MONTHS.addTo Jan31 +1 -> Feb29");
        check(ChronoUnit.DAYS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 11)) == 10, "DAYS.between == 10");
        check(ChronoUnit.WEEKS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 8)) == 1, "WEEKS.between == 1");

        // support
        check(ChronoUnit.DAYS.isSupportedBy(LocalDate.of(2024, 1, 1)), "DAYS supported by LocalDate");
        check(!ChronoUnit.HOURS.isSupportedBy(LocalDate.of(2024, 1, 1)), "HOURS not supported by LocalDate");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
