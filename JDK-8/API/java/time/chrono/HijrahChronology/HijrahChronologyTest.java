/*
 * @test
 * @summary java.time.chrono.HijrahChronology conformance: id/calendarType/era.
 * @run main HijrahChronologyTest
 */

import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahEra;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.chrono.Chronology;
import java.time.chrono.HijrahDate;
import java.time.chrono.IsoChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

public class HijrahChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHijrahChronology() {
        HijrahChronology hc = HijrahChronology.INSTANCE;
        check("Hijrah-umalqura".equals(hc.getId()), "id");
        check("islamic-umalqura".equals(hc.getCalendarType()), "calendarType");
        check(hc.eras().size() == 1, "one era");
        check(hc.eraOf(1) == HijrahEra.AH, "eraOf AH");
        check(hc.date(1437, 1, 1) != null, "date");
    }

    public static void main(String[] args) {
        testHijrahChronology();
        testDeepHijrahChronology();
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


    static void testDeepHijrahChronology() {
        xeq(HijrahChronology.INSTANCE.getId(), "Hijrah-umalqura", "id");
        xeq(HijrahChronology.INSTANCE.getCalendarType(), "islamic-umalqura", "calendarType");
        check(!HijrahChronology.INSTANCE.isLeapYear(1445), "1445 not leap");
        xeq(HijrahChronology.INSTANCE.period(1, 2, 3).toString(), "Hijrah-umalqura P1Y2M3D", "period toString");
        check(HijrahChronology.INSTANCE.period(1, 2, 3).getChronology().equals(HijrahChronology.INSTANCE), "period chronology");
        xeq(HijrahChronology.INSTANCE.eraOf(1).toString(), "AH", "eraOf 1");
        xact(() -> HijrahChronology.INSTANCE.eraOf(2), java.time.DateTimeException.class, "invalid Hijrah era", "eraOf 2");
        xeq(HijrahChronology.INSTANCE.dateEpochDay(19557L).toString(), "Hijrah-umalqura AH 1445-01-01", "dateEpochDay");
        xeq(HijrahChronology.INSTANCE.date(HijrahDate.of(1445, 1, 1)).toString(),
            "Hijrah-umalqura AH 1445-01-01", "date round-trip");
        xeq(HijrahChronology.INSTANCE.dateYearDay(1445, 345).toString(), "Hijrah-umalqura AH 1445-12-21", "dateYearDay");
        xeq(HijrahChronology.INSTANCE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
             "Islamic Umm al-Qura Calendar", "hj chronology full name");
        check(HijrahChronology.INSTANCE.compareTo(IsoChronology.INSTANCE) == -1, "compareTo Hijrah first");
        check(HijrahChronology.INSTANCE.equals(Chronology.of("Hijrah-umalqura")), "equals of");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        xeq(HijrahChronology.INSTANCE.dateNow(fixed).toString(), "Hijrah-umalqura AH 1445-12-09", "dateNow fixed clock");
        check(HijrahDate.of(1445, 1, 1).range(java.time.temporal.ChronoField.YEAR).toString().equals("1300 - 1600"),
            "year range via date");
        check(HijrahDate.of(1445, 1, 1).range(java.time.temporal.ChronoField.DAY_OF_YEAR).toString().equals("1 - 354"),
            "day-of-year range via date");
    }

}
