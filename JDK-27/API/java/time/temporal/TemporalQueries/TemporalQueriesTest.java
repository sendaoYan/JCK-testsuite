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
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.temporal.TemporalQuery;

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
        testDeepTemporalQueries();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepTemporalQueries() {
        check(TemporalQueries.localDate().queryFrom(LocalDate.of(2024, 2, 29)).toString().equals("2024-02-29"), "localDate q");
        check(TemporalQueries.localDate().queryFrom(LocalTime.NOON) == null, "localDate null on time");
        check(TemporalQueries.localTime().queryFrom(LocalTime.NOON).toString().equals("12:00"), "localTime q");
        check(TemporalQueries.localTime().queryFrom(LocalDate.of(2024, 1, 1)) == null, "localTime null on date");
        check(TemporalQueries.localDate().queryFrom(LocalDateTime.of(2024, 2, 29, 10, 15)).toString().equals("2024-02-29"), "localDate on LDT");
        check(TemporalQueries.precision().queryFrom(LocalDate.of(2024, 1, 1)) == ChronoUnit.DAYS, "precision days");
        check(TemporalQueries.precision().queryFrom(LocalTime.NOON) == ChronoUnit.NANOS, "precision nanos");
        check(TemporalQueries.precision().queryFrom(Year.of(2024)) == ChronoUnit.YEARS, "precision years");
        check(TemporalQueries.precision().queryFrom(YearMonth.of(2024, 2)) == ChronoUnit.MONTHS, "precision months");
        ZoneId ber = ZoneId.of("Europe/Berlin");
        ZonedDateTime zdt = ZonedDateTime.of(2024, 2, 29, 10, 15, 30, 0, ber);
        check(TemporalQueries.zoneId().queryFrom(zdt) == ber, "zoneId q");
        check(TemporalQueries.zone().queryFrom(zdt) == ber, "zone q zdt");
        check(TemporalQueries.zone().queryFrom(OffsetTime.of(LocalTime.NOON, ZoneOffset.ofHours(1))).equals(ZoneOffset.ofHours(1)), "zone q offset");
        check(TemporalQueries.zone().queryFrom(LocalDate.of(2024, 1, 1)) == null, "zone null on date");
        check(TemporalQueries.offset().queryFrom(zdt).equals(ZoneOffset.ofHours(1)), "offset q zdt");
        check(TemporalQueries.offset().queryFrom(LocalDate.of(2024, 1, 1)) == null, "offset null on date");
        check(TemporalQueries.chronology().queryFrom(LocalDate.of(2024, 1, 1)) == IsoChronology.INSTANCE, "chronology q");
        check(TemporalQueries.chronology().queryFrom(MinguoDate.of(113, 2, 29)).equals(MinguoChronology.INSTANCE), "chronology minguo");
        check(TemporalQueries.chronology().queryFrom(LocalTime.NOON) == null, "chronology null");
        TemporalQuery<Integer> custom = temporal -> 42;
        check((Object) TemporalQueries.localDate() != (Object) TemporalQueries.localTime(), "distinct instances");
        check(custom.queryFrom(LocalDate.of(2024, 1, 1)) == 42, "custom query");
        check(LocalDate.of(2024, 1, 1).query(custom) == 42, "accessor.query default");
        check(LocalTime.NOON.query(TemporalQueries.localDate()) == null, "default query dispatch");
        xact(() -> TemporalQueries.localDate().queryFrom(null), NullPointerException.class, null, "queryFrom null");
    }

}
