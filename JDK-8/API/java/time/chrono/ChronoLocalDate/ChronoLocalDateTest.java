/*
 * @test
 * @summary java.time.chrono.ChronoLocalDate conformance: via LocalDate.
 * @run main ChronoLocalDateTest
 */

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.chrono.IsoEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;

public class ChronoLocalDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoLocalDate() {
        ChronoLocalDate d = LocalDate.of(2020, 1, 1);
        check(d.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(d.getEra() == IsoEra.CE, "getEra");
        check(d.isSupported(ChronoField.DAY_OF_MONTH), "isSupported");
        check(d.get(ChronoField.DAY_OF_MONTH) == 1, "get DAY_OF_MONTH");
        check(d.getLong(ChronoField.YEAR) == 2020, "getLong YEAR");
        check(d.lengthOfMonth() == 31, "lengthOfMonth");
        check(d.lengthOfYear() == 366, "lengthOfYear leap");
        check(d.isLeapYear(), "isLeapYear");
        check(d.toEpochDay() == 18262L, "toEpochDay");
        check(d.atTime(LocalTime.NOON) != null, "atTime");
        check(d.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2020, 1, 2)), "plus");
        check(d.until(LocalDate.of(2020, 1, 5), ChronoUnit.DAYS) == 4, "until");
    }

    public static void main(String[] args) {
        testChronoLocalDate();
        testDeepChronoLocalDate();
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


    static void testDeepChronoLocalDate() {
        ChronoLocalDate jp = JapaneseDate.of(2024, 6, 15);
        ChronoLocalDate mg = MinguoDate.of(113, 6, 15);
        ChronoLocalDate iso = java.time.LocalDate.of(2024, 6, 15);
        xeq(jp.compareTo(mg) + "", "-3", "cross-chrono compareTo by id");
        xeq(jp.compareTo(iso) + "", "1", "jp vs iso-1d compareTo");
        check(ChronoLocalDate.timeLineOrder().compare(jp, iso) == 0, "timeLine same instant equal");
        check(ChronoLocalDate.timeLineOrder().compare(jp, java.time.LocalDate.of(2024, 6, 14)) > 0, "timeLine later");
        check(ChronoLocalDate.timeLineOrder().compare(mg, java.time.LocalDate.of(2024, 6, 16)) < 0, "timeLine earlier");
        check(jp.isAfter(java.time.LocalDate.of(2024, 6, 14)), "jp isAfter iso-1d");
        check(jp.isBefore(java.time.LocalDate.of(2024, 6, 16)), "jp isBefore iso+1d");
        check(jp.isEqual(iso), "jp isEqual same timeline");
        check(!jp.equals(iso), "jp not equals iso");
        check(ChronoLocalDate.from(java.time.LocalDate.of(2024, 6, 15)).equals(iso), "from LocalDate");
        check(ChronoLocalDate.from(java.time.LocalDateTime.of(2024, 6, 15, 10, 0)).equals(iso), "from LocalDateTime");
        check(ChronoLocalDate.from(java.time.ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, java.time.ZoneOffset.UTC)).equals(iso),
            "from ZonedDateTime");
        xact(() -> ChronoLocalDate.from(null), NullPointerException.class, "temporal", "from null");
        check(jp.toEpochDay() == 19889L, "jp epochDay");
        check(mg.toEpochDay() == 19889L, "mg epochDay same");
        check(jp.format(java.time.format.DateTimeFormatter.ofPattern("G y-MM-d", java.util.Locale.ENGLISH))
            .equals("Reiwa 6-06-15"), "jp format era pattern");
        check(jp.query(java.time.temporal.TemporalQueries.localDate()).equals(java.time.LocalDate.of(2024, 6, 15)),
            "jp query localDate");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp chronology");
    }

}
