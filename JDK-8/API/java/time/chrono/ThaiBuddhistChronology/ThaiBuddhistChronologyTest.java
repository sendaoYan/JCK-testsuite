/*
 * @test
 * @summary java.time.chrono.ThaiBuddhistChronology conformance: id/calendarType/era.
 * @run main ThaiBuddhistChronologyTest
 */

import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistEra;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.chrono.MinguoChronology;
import java.time.chrono.ThaiBuddhistDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

public class ThaiBuddhistChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThaiBuddhistChronology() {
        ThaiBuddhistChronology tc = ThaiBuddhistChronology.INSTANCE;
        check("ThaiBuddhist".equals(tc.getId()), "id");
        check("buddhist".equals(tc.getCalendarType()), "calendarType");
        check(tc.eras().size() == 2, "two eras");
        check(tc.eraOf(1) == ThaiBuddhistEra.BE, "eraOf BE");
        check(tc.date(2563, 1, 1) != null, "date 2563");
    }

    public static void main(String[] args) {
        testThaiBuddhistChronology();
        testDeepThaiBuddhistChronology();
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


    static void testDeepThaiBuddhistChronology() {
        xeq(ThaiBuddhistChronology.INSTANCE.getId() + "|" + ThaiBuddhistChronology.INSTANCE.getCalendarType(),
            "ThaiBuddhist|buddhist", "ids");
        check(ThaiBuddhistChronology.INSTANCE.isLeapYear(2567), "2567 leap");
        check(!ThaiBuddhistChronology.INSTANCE.isLeapYear(2566), "2566 not leap");
        xeq(ThaiBuddhistChronology.INSTANCE.period(1, 2, 3).toString(), "ThaiBuddhist P1Y2M3D", "period toString");
        xeq(ThaiBuddhistChronology.INSTANCE.dateEpochDay(19889L).toString(), "ThaiBuddhist BE 2567-06-15", "dateEpochDay");
        xeq(ThaiBuddhistChronology.INSTANCE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Buddhist Calendar", "display name");
        check(ThaiBuddhistChronology.INSTANCE.eraOf(1).toString().equals("BE"), "eraOf BE");
        check(ThaiBuddhistChronology.INSTANCE.eraOf(0).toString().equals("BEFORE_BE"), "eraOf BEFORE_BE");
        check(ThaiBuddhistChronology.INSTANCE.eras().size() == 2, "two eras");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        xeq(ThaiBuddhistChronology.INSTANCE.dateNow(fixed).toString(), "ThaiBuddhist BE 2567-06-15", "dateNow fixed clock");
        check(ThaiBuddhistDate.of(2567, 6, 15).get(java.time.temporal.ChronoField.YEAR) == 2567, "proleptic year");
        check(ThaiBuddhistChronology.INSTANCE.compareTo(MinguoChronology.INSTANCE) > 0, "id ordering");
    }

}
