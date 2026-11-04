/*
 * @test
 * @summary java.time.chrono.JapaneseDate conformance: white-box structure + black-box behavior.
 * @run main JapaneseDateTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.JapaneseEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.DateTimeException;
import java.time.chrono.Era;
import java.time.chrono.MinguoDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;

public class JapaneseDateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); passed++; }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name, e); }
    }

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try { r.run(); } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.JapaneseDate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check("java.time.chrono.ChronoLocalDateImpl".equals(c.getSuperclass().getName()), "extends ChronoLocalDateImpl");
        check(ChronoLocalDate.class.isAssignableFrom(c), "implements ChronoLocalDate");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        checkMethod(c, "of", JapaneseEra.class, int.class, int.class, int.class);
        checkMethod(c, "of", int.class, int.class, int.class);
        checkMethod(c, "from", java.time.temporal.TemporalAccessor.class);
        checkMethod(c, "now");
        checkMethod(c, "getEra");
        checkMethod(c, "getChronology");
        checkMethod(c, "toEpochDay");
    }

    static void testBlackBox() {
        JapaneseDate jd = JapaneseDate.of(JapaneseEra.HEISEI, 6, 1, 1);
        check(jd.getEra() == JapaneseEra.HEISEI, "getEra HEISEI");
        check(jd.getChronology() == JapaneseChronology.INSTANCE, "getChronology");
        check(jd.getLong(ChronoField.YEAR_OF_ERA) == 6, "YEAR_OF_ERA");
        check(jd.getLong(ChronoField.MONTH_OF_YEAR) == 1, "MONTH_OF_YEAR");
        check(jd.getLong(ChronoField.DAY_OF_MONTH) == 1, "DAY_OF_MONTH");
        check(jd.lengthOfMonth() == 31, "lengthOfMonth January");
        check(jd.lengthOfYear() > 0, "lengthOfYear > 0");

        // toEpochDay matches the ISO proleptic date (Heisei 6 == 1994, full non-leap year)
        check(jd.toEpochDay() == LocalDate.of(1994, 1, 1).toEpochDay(), "toEpochDay == ISO 1994-01-01");

        // from() and proleptic of()
        check(jd.equals(JapaneseDate.from(LocalDate.of(1994, 1, 1))), "from LocalDate");
        check(JapaneseDate.of(1994, 1, 1).equals(jd), "of(1994,1,1) equals of(HEISEI,6,1,1)");

        // round-trip through the chronology
        check(JapaneseChronology.INSTANCE.dateEpochDay(jd.toEpochDay()).equals(jd), "round-trip dateEpochDay");

        // plus/minus arithmetic
        check(jd.plus(1, ChronoUnit.DAYS).toEpochDay() == jd.toEpochDay() + 1, "plus one day");
        check(jd.minus(1, ChronoUnit.DAYS).toEpochDay() == jd.toEpochDay() - 1, "minus one day");

        // with()
        JapaneseDate jd7 = jd.with(ChronoField.YEAR_OF_ERA, 7);
        check(jd7.getLong(ChronoField.YEAR_OF_ERA) == 7, "with YEAR_OF_ERA=7");
        check(jd7.getEra() == JapaneseEra.HEISEI, "with keeps era");

        check(jd.atTime(LocalTime.NOON) != null, "atTime non-null");
        check(jd.equals(jd) && jd.hashCode() == jd.hashCode(), "equals/hashCode self");

        // invalid era-year combination rejected (Heisei 1 began on Jan 8)
        expectThrows(java.time.DateTimeException.class,
            () -> JapaneseDate.of(JapaneseEra.HEISEI, 1, 1, 1), "HEISEI 1/1/1 invalid");

        check(JapaneseDate.now() != null, "now non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
