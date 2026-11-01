/*
 * @test
 * @summary java.time.chrono.ChronoZonedDateTime conformance: via ZonedDateTime.
 * @run main ChronoZonedDateTimeTest
 */

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;

public class ChronoZonedDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoZonedDateTime() {
        ChronoZonedDateTime<?> zdt = ZonedDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneId.of("UTC"));
        check(zdt.toLocalDateTime().equals(LocalDateTime.of(2020, 1, 1, 12, 0)), "toLocalDateTime");
        check(zdt.toLocalDate().equals(LocalDate.of(2020, 1, 1)), "toLocalDate");
        check(zdt.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        check(zdt.getZone().getId().equals("UTC"), "getZone");
        check(zdt.getOffset().equals(ZoneOffset.UTC), "getOffset");
        check(zdt.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(zdt.plus(1, ChronoUnit.DAYS).toLocalDate().equals(LocalDate.of(2020, 1, 2)), "plus day");
        check(zdt.toInstant().equals(Instant.parse("2020-01-01T12:00:00Z")), "toInstant");
    }

    public static void main(String[] args) {
        testChronoZonedDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
