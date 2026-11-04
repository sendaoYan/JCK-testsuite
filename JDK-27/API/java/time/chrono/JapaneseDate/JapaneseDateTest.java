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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
