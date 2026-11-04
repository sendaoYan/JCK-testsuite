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
import java.time.DateTimeException;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.temporal.TemporalAccessor;

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
        testDeepChronoZonedDateTime();
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


    static void testDeepChronoZonedDateTime() {
        ChronoZonedDateTime<?> c = ChronoZonedDateTime.from(
            java.time.ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, java.time.ZoneId.of("UTC")));
        xeq(c.toString(), "2024-06-15T10:00Z[UTC]", "from ZonedDateTime");
        xeq(c.toInstant().toString(), "2024-06-15T10:00:00Z", "toInstant");
        check(c.toEpochSecond() == 1718445600L, "toEpochSecond");
        check(c.toLocalDate().equals(java.time.LocalDate.of(2024, 6, 15)), "toLocalDate");
        check(c.getOffset().equals(java.time.ZoneOffset.UTC), "getOffset");
        check(c.getZone().equals(java.time.ZoneId.of("UTC")), "getZone");
        ChronoZonedDateTime<?> jp = JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON).atZone(java.time.ZoneId.of("UTC"));
        xeq(jp.toString(), "Japanese Reiwa 6-06-15T12:00Z[UTC]", "jp atZone");
        check(ChronoZonedDateTime.timeLineOrder() != null, "timeLineOrder comparator exists");
        check(ChronoZonedDateTime.timeLineOrder().compare(jp, c) > 0, "timeLine NOON after 10:00");
        check(jp.compareTo(c) > 0, "compareTo instant order");
        check(jp.isAfter(c), "isAfter");
        xact(() -> ChronoZonedDateTime.from(java.time.LocalDate.of(2024, 1, 1)), java.time.DateTimeException.class,
            "Unable to obtain ZonedDateTime from TemporalAccessor: 2024-01-01 of type java.time.LocalDate", "from LocalDate");
        xact(() -> ChronoZonedDateTime.from(java.time.LocalDateTime.of(2024, 1, 1, 10, 0)), java.time.DateTimeException.class,
            "Unable to obtain ZonedDateTime from TemporalAccessor: 2024-01-01T10:00 of type java.time.LocalDateTime",
            "from LocalDateTime");
        check(jp.withZoneSameLocal(java.time.ZoneId.of("UTC")).toLocalTime().equals(java.time.LocalTime.NOON),
            "withZoneSameLocal keeps wall time");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp CZDT chronology");
    }

}
