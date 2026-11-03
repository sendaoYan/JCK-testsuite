/*
 * @test
 * @summary java.time.chrono.MinguoDate conformance: white-box structure + black-box behavior.
 * @run main MinguoDateTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.MinguoEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class MinguoDateTest {
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
        Class<?> c = Class.forName("java.time.chrono.MinguoDate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check("java.time.chrono.ChronoLocalDateImpl".equals(c.getSuperclass().getName()), "extends ChronoLocalDateImpl");
        check(ChronoLocalDate.class.isAssignableFrom(c), "implements ChronoLocalDate");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        checkMethod(c, "of", int.class, int.class, int.class);
        checkMethod(c, "from", java.time.temporal.TemporalAccessor.class);
        checkMethod(c, "now");
        checkMethod(c, "getEra");
        checkMethod(c, "getChronology");
        checkMethod(c, "toEpochDay");
    }

    static void testBlackBox() {
        MinguoDate md = MinguoDate.of(109, 1, 1);
        check(md.getEra() == MinguoEra.ROC, "getEra ROC");
        check(md.getChronology() == MinguoChronology.INSTANCE, "getChronology");
        check(md.getLong(ChronoField.YEAR) == 109, "YEAR (proleptic)");
        check(md.getLong(ChronoField.MONTH_OF_YEAR) == 1, "MONTH_OF_YEAR");
        check(md.getLong(ChronoField.DAY_OF_MONTH) == 1, "DAY_OF_MONTH");
        check(md.lengthOfMonth() == 31, "lengthOfMonth January");
        check(md.toEpochDay() == LocalDate.of(2020, 1, 1).toEpochDay(), "toEpochDay == ISO 2020-01-01");

        // Feb of a Minguo leap year (109 == ISO 2020) has 29 days
        check(MinguoDate.of(109, 2, 1).lengthOfMonth() == 29, "lengthOfMonth Feb 2020 leap = 29");

        check(md.equals(MinguoDate.from(LocalDate.of(2020, 1, 1))), "from LocalDate");
        check(MinguoChronology.INSTANCE.dateEpochDay(md.toEpochDay()).equals(md), "round-trip dateEpochDay");

        // before-ROC boundary: proleptic year 0 == ISO 1911 == BEFORE_ROC
        MinguoDate before = MinguoDate.of(0, 1, 1);
        check(before.getEra() == MinguoEra.BEFORE_ROC, "year 0 -> BEFORE_ROC");
        check(before.toEpochDay() == LocalDate.of(1911, 1, 1).toEpochDay(), "year 0 == ISO 1911-01-01");

        check(md.plus(1, ChronoUnit.DAYS).toEpochDay() == md.toEpochDay() + 1, "plus one day");
        check(md.minus(1, ChronoUnit.DAYS).toEpochDay() == md.toEpochDay() - 1, "minus one day");
        check(md.atTime(LocalTime.NOON) != null, "atTime non-null");

        expectThrows(java.time.DateTimeException.class, () -> MinguoDate.of(109, 2, 30), "Feb 30 invalid");

        check(MinguoDate.now() != null, "now non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
