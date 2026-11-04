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
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.temporal.TemporalAccessor;

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
        testDeepChronoLocalDateTime();
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


    static void testDeepChronoLocalDateTime() {
        ChronoLocalDateTime<?> c = ChronoLocalDateTime.from(java.time.LocalDateTime.of(2024, 6, 15, 10, 0));
        xeq(c.toString(), "2024-06-15T10:00", "from LocalDateTime");
        xeq(c.atZone(java.time.ZoneId.of("UTC")).toString(), "2024-06-15T10:00Z[UTC]", "atZone");
        xeq(c.toInstant(java.time.ZoneOffset.UTC).toString(), "2024-06-15T10:00:00Z", "toInstant");
        check(c.toEpochSecond(java.time.ZoneOffset.UTC) == 1718445600L, "toEpochSecond");
        check(c.toLocalDate().equals(java.time.LocalDate.of(2024, 6, 15)), "toLocalDate");
        check(c.toLocalTime().equals(java.time.LocalTime.of(10, 0)), "toLocalTime");
        ChronoLocalDateTime<?> jp = JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON);
        xeq(jp.toString(), "Japanese Reiwa 6-06-15T12:00", "jp atTime");
        check(ChronoLocalDateTime.timeLineOrder().compare(jp, c) > 0, "timeLine NOON after 10:00");
        check(ChronoLocalDateTime.timeLineOrder().compare(jp, JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON)) == 0,
            "timeLine equal");
        xact(() -> ChronoLocalDateTime.from(java.time.LocalDate.of(2024, 1, 1)), java.time.DateTimeException.class,
            "Unable to obtain LocalDateTime from TemporalAccessor: 2024-01-01 of type java.time.LocalDate", "from LocalDate");
        xact(() -> ChronoLocalDateTime.from(null), NullPointerException.class, "temporal", "from null");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp CLDT chronology");
        check(jp.isAfter(c), "isAfter cross-chrono");
        check(jp.isEqual(JapaneseDate.of(2024, 6, 15).atTime(java.time.LocalTime.NOON)), "isEqual");
    }

}
