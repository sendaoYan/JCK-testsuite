/*
 * @test
 * @summary java.time.chrono.JapaneseDate conformance: era/chronology/fields/from.
 * @run main JapaneseDateTest
 */

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.temporal.ChronoField;
import java.time.DateTimeException;
import java.time.chrono.Era;
import java.time.chrono.MinguoDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

public class JapaneseDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseDate() {
        JapaneseDate jd = JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8);
        check(jd.getEra() == JapaneseEra.HEISEI, "era");
        check(jd.getChronology() == JapaneseChronology.INSTANCE, "chronology");
        check(jd.getLong(ChronoField.YEAR_OF_ERA) == 1, "yearOfEra");
        check(jd.getLong(ChronoField.MONTH_OF_YEAR) == 1, "month");
        check(jd.getLong(ChronoField.DAY_OF_MONTH) == 8, "day");
        check(jd.equals(JapaneseDate.from(LocalDate.of(1989, 1, 8))), "from LocalDate");
        check(jd.lengthOfYear() > 0, "lengthOfYear");
        check(jd.atTime(LocalTime.NOON) != null, "atTime");
    }

    public static void main(String[] args) {
        testJapaneseDate();
        testDeepJapaneseDate();
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


    static void testDeepJapaneseDate() {
        JapaneseDate jd = JapaneseDate.of(2024, 6, 15);
        xeq(jd.toString(), "Japanese Reiwa 6-06-15", "toString");
        check(jd.get(java.time.temporal.ChronoField.YEAR_OF_ERA) == 6, "year of era 6");
        check(jd.getLong(java.time.temporal.ChronoField.ERA) == 3, "era field 3");
        check(jd.lengthOfYear() == 366, "lengthOfYear 366");
        check(jd.isLeapYear(), "leap");
        xeq(JapaneseDate.of(2019, 5, 1).toString(), "Japanese Reiwa 1-05-01", "reiwa first day");
        xeq(JapaneseDate.of(JapaneseEra.SHOWA, 1, 12, 25).toString(), "Japanese Showa 1-12-25", "showa first day");
        xeq(JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 8).toString(), "Japanese Heisei 1-01-08", "heisei factory");
        xeq(JapaneseDate.from(java.time.LocalDate.of(2024, 6, 15)).toString(), "Japanese Reiwa 6-06-15", "from LocalDate");
        xeq(JapaneseChronology.INSTANCE.dateEpochDay(19889L).toString(), "Japanese Reiwa 6-06-15", "dateEpochDay");
        xeq(jd.minus(10, java.time.temporal.ChronoUnit.YEARS).toString(), "Japanese Heisei 26-06-15", "minus 10y crosses era");
        xeq(jd.plus(1, java.time.temporal.ChronoUnit.DAYS).toString(), "Japanese Reiwa 6-06-16", "plus 1 day");
        check(jd.until(java.time.LocalDate.of(2024, 6, 20), java.time.temporal.ChronoUnit.DAYS) == 5, "until cross-chrono days");
        check(jd.until(MinguoDate.of(113, 6, 15), java.time.temporal.ChronoUnit.DAYS) == 0, "until same timeline 0");
        xeq(JapaneseDate.from(java.time.LocalDate.of(1873, 1, 1)).toString(), "Japanese Meiji 6-01-01", "minimum supported");
        check(!jd.equals(java.time.LocalDate.of(2024, 6, 15)), "not equals LocalDate");
        check(jd.compareTo(java.time.LocalDate.of(2024, 6, 14)) == 1, "compareTo iso-1d");
        check(jd.isAfter(java.time.LocalDate.of(2024, 6, 14)) && jd.isBefore(java.time.LocalDate.of(2024, 6, 16)), "isAfter isBefore");
        check(jd.isEqual(java.time.LocalDate.of(2024, 6, 15)), "isEqual");
        xeq(jd.format(java.time.format.DateTimeFormatter.ofPattern("G y-MM-d", java.util.Locale.ENGLISH)),
            "Reiwa 6-06-15", "format era pattern");
        xeq(jd.atTime(java.time.LocalTime.NOON).toString(), "Japanese Reiwa 6-06-15T12:00", "atTime");
        xeq(jd.getEra().toString(), "Reiwa", "getEra");
        xeq(jd.query(java.time.temporal.TemporalQueries.localDate()).toString(), "2024-06-15", "query localDate");
        xact(() -> JapaneseDate.of(JapaneseEra.SHOWA, 64, 1, 8), java.time.DateTimeException.class,
            "year, month, and day not valid for Era", "showa 64 overflow");
        xact(() -> JapaneseDate.of(JapaneseEra.MEIJI, 45, 8, 1), java.time.DateTimeException.class,
            "year, month, and day not valid for Era", "meiji 45 overflow");
        xact(() -> JapaneseDate.of(1872, 1, 1), java.time.DateTimeException.class,
            "JapaneseDate before Meiji 6 is not supported", "below minimum");
    }

}
