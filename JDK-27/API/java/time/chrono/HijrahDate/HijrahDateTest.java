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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

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
        testDeepHijrahDate();
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


    static void testDeepHijrahDate() {
        HijrahDate hd = HijrahDate.of(1445, 12, 10);
        xeq(hd.toString(), "Hijrah-umalqura AH 1445-12-10", "toString");
        check(hd.lengthOfMonth() == 30, "lengthOfMonth 30");
        check(hd.lengthOfYear() == 354, "lengthOfYear 354");
        xeq(hd.range(java.time.temporal.ChronoField.DAY_OF_MONTH).toString(), "1 - 30", "dom range");
        xeq(hd.range(java.time.temporal.ChronoField.MONTH_OF_YEAR).toString(), "1 - 12", "month range");
        xeq(hd.range(java.time.temporal.ChronoField.YEAR).toString(), "1300 - 1600", "year range");
        check(hd.get(java.time.temporal.ChronoField.DAY_OF_YEAR) == 334, "day of year 334");
        xeq(java.time.LocalDate.from(hd).toString(), "2024-06-16", "to ISO date");
        xeq(HijrahDate.from(java.time.LocalDate.of(2024, 6, 15)).toString(), "Hijrah-umalqura AH 1445-12-09", "from ISO date");
        check(hd.toEpochDay() != 0 && HijrahDate.of(1300, 1, 1).toEpochDay() < hd.toEpochDay(), "epochDay ordering");
        check(HijrahChronology.INSTANCE.dateEpochDay(hd.toEpochDay()).equals(hd), "epochDay round-trip");
        xeq(hd.plus(20, java.time.temporal.ChronoUnit.DAYS).toString(), "Hijrah-umalqura AH 1445-12-30", "plus 20 days");
        xeq(hd.plus(1, java.time.temporal.ChronoUnit.MONTHS).toString(), "Hijrah-umalqura AH 1446-01-10", "plus 1 month");
        xeq(hd.with(java.time.temporal.TemporalAdjusters.firstDayOfNextMonth()).toString(),
            "Hijrah-umalqura AH 1446-01-01", "firstDayOfNextMonth");
        xeq(HijrahDate.of(1300, 1, 1).toString(), "Hijrah-umalqura AH 1300-01-01", "minimum date");
        xeq(HijrahDate.of(1600, 12, 30).toString(), "Hijrah-umalqura AH 1600-12-30", "maximum date");
        check(hd.equals(HijrahDate.of(1445, 12, 10)) && hd.hashCode() == HijrahDate.of(1445, 12, 10).hashCode(), "equals hashCode");
        check(!hd.equals(HijrahDate.of(1445, 12, 11)), "not equals");
        check(hd.compareTo(HijrahDate.of(1445, 12, 9)) > 0, "compareTo");
        check(!hd.isLeapYear(), "isLeapYear 1445 false");
        xact(() -> HijrahDate.of(1445, 13, 10), java.time.DateTimeException.class, "Invalid Hijrah month: 13", "month 13");
        xact(() -> HijrahDate.of(1445, 12, 31), java.time.DateTimeException.class, "Invalid Hijrah day of month: 31", "day 31");
        xact(() -> HijrahDate.of(1299, 1, 1), java.time.DateTimeException.class,
            "Invalid Hijrah date, year: 1299, month: 1", "below range");
        xact(() -> HijrahDate.of(1600, 12, 31), java.time.DateTimeException.class,
            "Invalid Hijrah day of month: 31", "above range");
        xact(() -> HijrahDate.of(1700, 1, 1), java.time.DateTimeException.class,
            "Invalid Hijrah date, year: 1700, month: 1", "far future");
        xeq(hd.format(java.time.format.DateTimeFormatter.ofPattern("G uuuu-MM-d", java.util.Locale.ENGLISH)),
            "AH 1445-12-10", "format era pattern");
        check(hd.atTime(java.time.LocalTime.NOON).toString().equals("Hijrah-umalqura AH 1445-12-10T12:00"), "atTime");
    }

}
