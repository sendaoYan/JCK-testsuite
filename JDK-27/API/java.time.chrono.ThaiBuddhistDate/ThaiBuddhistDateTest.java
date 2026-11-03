/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistDate conformance: white-box structure + black-box behavior.
 * @run main ThaiBuddhistDateTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.chrono.ThaiBuddhistEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class ThaiBuddhistDateTest {
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
        Class<?> c = Class.forName("java.time.chrono.ThaiBuddhistDate");
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
        ThaiBuddhistDate td = ThaiBuddhistDate.of(2563, 1, 1);
        check(td.getEra() == ThaiBuddhistEra.BE, "getEra BE");
        check(td.getChronology() == ThaiBuddhistChronology.INSTANCE, "getChronology");
        check(td.getLong(ChronoField.YEAR) == 2563, "YEAR (proleptic)");
        check(td.getLong(ChronoField.MONTH_OF_YEAR) == 1, "MONTH_OF_YEAR");
        check(td.getLong(ChronoField.DAY_OF_MONTH) == 1, "DAY_OF_MONTH");
        check(td.lengthOfMonth() == 31, "lengthOfMonth January");
        check(td.lengthOfYear() == 366, "lengthOfYear 2563 (ISO 2020 leap) = 366");
        check(td.toEpochDay() == LocalDate.of(2020, 1, 1).toEpochDay(), "toEpochDay == ISO 2020-01-01");

        check(ThaiBuddhistDate.of(2563, 2, 1).lengthOfMonth() == 29, "Feb 2563 leap = 29");

        check(td.equals(ThaiBuddhistDate.from(LocalDate.of(2020, 1, 1))), "from LocalDate");
        check(ThaiBuddhistChronology.INSTANCE.dateEpochDay(td.toEpochDay()).equals(td), "round-trip dateEpochDay");

        // before-BE boundary: proleptic year 0 == ISO -543 == BEFORE_BE
        ThaiBuddhistDate before = ThaiBuddhistDate.of(0, 1, 1);
        check(before.getEra() == ThaiBuddhistEra.BEFORE_BE, "year 0 -> BEFORE_BE");

        check(td.plus(1, ChronoUnit.DAYS).toEpochDay() == td.toEpochDay() + 1, "plus one day");
        check(td.minus(1, ChronoUnit.DAYS).toEpochDay() == td.toEpochDay() - 1, "minus one day");
        check(td.atTime(LocalTime.NOON) != null, "atTime non-null");

        expectThrows(java.time.DateTimeException.class, () -> ThaiBuddhistDate.of(2563, 2, 30), "Feb 30 invalid");

        check(ThaiBuddhistDate.now() != null, "now non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
