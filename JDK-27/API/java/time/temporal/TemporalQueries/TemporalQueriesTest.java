/*
 * @test
 * @summary java.time.temporal.TemporalQueries conformance: white-box structure + black-box behavior.
 * @run main TemporalQueriesTest
 */

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

public class TemporalQueriesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.TemporalQueries");
        check(Modifier.isFinal(c.getModifiers()), "is final utility class");
        check(c.getConstructors().length == 0, "no public constructor");
        check(TemporalQueries.zoneId() != null, "zoneId() non-null");
        check(TemporalQueries.chronology() != null, "chronology() non-null");
        check(TemporalQueries.precision() != null, "precision() non-null");
        check(TemporalQueries.zone() != null, "zone() non-null");
        check(TemporalQueries.offset() != null, "offset() non-null");
        check(TemporalQueries.localDate() != null, "localDate() non-null");
        check(TemporalQueries.localTime() != null, "localTime() non-null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // localDate / localTime
        check(TemporalQueries.localDate().queryFrom(LocalDate.of(2024, 3, 1)).equals(LocalDate.of(2024, 3, 1)), "localDate from LocalDate");
        check(TemporalQueries.localDate().queryFrom(LocalDateTime.of(2024, 3, 1, 12, 0)).equals(LocalDate.of(2024, 3, 1)), "localDate from LocalDateTime");
        check(TemporalQueries.localTime().queryFrom(LocalTime.of(10, 30)).equals(LocalTime.of(10, 30)), "localTime from LocalTime");
        check(TemporalQueries.localTime().queryFrom(LocalDateTime.of(2024, 3, 1, 10, 30)).equals(LocalTime.of(10, 30)), "localTime from LocalDateTime");

        // chronology
        check(TemporalQueries.chronology().queryFrom(LocalDate.of(2024, 3, 1)) == IsoChronology.INSTANCE, "chronology == ISO");

        // zoneId / zone
        ZoneId utc = ZoneId.of("UTC");
        ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 1, 10, 0, 0, 0, utc);
        check(TemporalQueries.zoneId().queryFrom(zdt).equals(utc), "zoneId from ZonedDateTime");
        check(TemporalQueries.zone().queryFrom(zdt).equals(utc), "zone from ZonedDateTime");

        // offset
        OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 0, 0, 0, ZoneOffset.ofHours(2));
        check(TemporalQueries.offset().queryFrom(odt).equals(ZoneOffset.ofHours(2)), "offset from OffsetDateTime");
        check(TemporalQueries.offset().queryFrom(ZoneOffset.ofHours(2)).equals(ZoneOffset.ofHours(2)), "offset from ZoneOffset");

        // precision
        check(TemporalQueries.precision().queryFrom(LocalDate.of(2024, 3, 1)) == ChronoUnit.DAYS, "precision LocalDate DAYS");
        check(TemporalQueries.precision().queryFrom(LocalTime.of(10, 30)) == ChronoUnit.NANOS, "precision LocalTime NANOS");
        check(TemporalQueries.precision().queryFrom(Instant.ofEpochSecond(0)) == ChronoUnit.NANOS, "precision Instant NANOS");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
