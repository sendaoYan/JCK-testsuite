/*
 * @test
 * @summary java.time.chrono.ChronoLocalDateTime conformance: via LocalDateTime.
 * @run main ChronoLocalDateTimeTest
 */

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class ChronoLocalDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoLocalDateTime() {
        ChronoLocalDateTime<?> dt = LocalDateTime.of(2020, 1, 1, 12, 0);
        check(dt.toLocalDate().equals(LocalDate.of(2020, 1, 1)), "toLocalDate");
        check(dt.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        check(dt.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(dt.isSupported(ChronoField.HOUR_OF_DAY), "isSupported");
        check(dt.get(ChronoField.HOUR_OF_DAY) == 12, "get HOUR_OF_DAY");
        check(dt.plus(1, ChronoUnit.HOURS).equals(LocalDateTime.of(2020, 1, 1, 13, 0)),
              "plus hour");
        check(dt.toInstant(ZoneOffset.UTC).equals(Instant.parse("2020-01-01T12:00:00Z")),
              "toInstant");
    }

    public static void main(String[] args) {
        testChronoLocalDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
