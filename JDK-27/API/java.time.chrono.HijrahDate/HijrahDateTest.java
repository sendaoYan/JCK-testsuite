/*
 * @test
 * @summary java.time.chrono.HijrahDate conformance: white-box structure + black-box behavior.
 * @run main HijrahDateTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.chrono.HijrahEra;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class HijrahDateTest {
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
        Class<?> c = Class.forName("java.time.chrono.HijrahDate");
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
        HijrahDate hd = HijrahDate.of(1437, 1, 1);
        check(hd.getEra() == HijrahEra.AH, "getEra AH");
        check(hd.getChronology() == HijrahChronology.INSTANCE, "getChronology");
        check(hd.getLong(ChronoField.YEAR) == 1437, "YEAR");
        check(hd.getLong(ChronoField.MONTH_OF_YEAR) == 1, "MONTH_OF_YEAR");
        check(hd.getLong(ChronoField.DAY_OF_MONTH) == 1, "DAY_OF_MONTH");

        // Umm al-Qura month/year lengths are data-derived; assert ranges, not exact values
        int lom = hd.lengthOfMonth();
        check(lom >= 29 && lom <= 30, "lengthOfMonth in 29..30");
        int loy = hd.lengthOfYear();
        check(loy >= 354 && loy <= 355, "lengthOfYear in 354..355");
        check(hd.isLeapYear() == (loy == 355), "isLeapYear consistent with lengthOfYear");
        check(!HijrahChronology.INSTANCE.isLeapYear(1437) == !hd.isLeapYear(), "chronology/hijrah-date isLeapYear agree for 1437");

        // round-trip only, no hard-coded epoch-day
        check(HijrahChronology.INSTANCE.dateEpochDay(hd.toEpochDay()).equals(hd), "round-trip dateEpochDay");

        // arithmetic round-trips
        check(hd.plus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.DAYS).equals(hd), "plus/minus one day round-trip");
        check(hd.minus(1, ChronoUnit.MONTHS).toEpochDay() < hd.toEpochDay(), "minus one month earlier");

        check(hd.atTime(LocalTime.NOON) != null, "atTime non-null");

        expectThrows(java.time.DateTimeException.class, () -> HijrahDate.of(1437, 13, 1), "month 13 invalid");

        check(HijrahDate.now() != null, "now non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
