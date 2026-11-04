/*
 * @test
 * @summary java.time.chrono.MinguoChronology conformance: id/calendarType/era.
 * @run main MinguoChronologyTest
 */

import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoEra;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

public class MinguoChronologyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMinguoChronology() {
        MinguoChronology mc = MinguoChronology.INSTANCE;
        check("Minguo".equals(mc.getId()), "id");
        check("roc".equals(mc.getCalendarType()), "calendarType");
        check(mc.eras().size() == 2, "two eras");
        check(mc.eraOf(1) == MinguoEra.ROC, "eraOf ROC");
        check(mc.date(1, 1, 1) != null, "date ROC 1");
    }

    public static void main(String[] args) {
        testMinguoChronology();
        testDeepMinguoChronology();
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


    static void testDeepMinguoChronology() {
        xeq(MinguoChronology.INSTANCE.getId() + "|" + MinguoChronology.INSTANCE.getCalendarType(), "Minguo|roc", "ids");
        check(MinguoChronology.INSTANCE.isLeapYear(113), "mg 113 leap");
        check(!MinguoChronology.INSTANCE.isLeapYear(112), "mg 112 not leap");
        xeq(MinguoChronology.INSTANCE.period(1, 2, 3).toString(), "Minguo P1Y2M3D", "period toString");
        xeq(MinguoChronology.INSTANCE.dateEpochDay(19889L).toString(), "Minguo ROC 113-06-15", "dateEpochDay");
        xeq(MinguoChronology.INSTANCE.dateYearDay(113, 167).toString(), "Minguo ROC 113-06-15", "dateYearDay");
        xeq(MinguoChronology.INSTANCE.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
            "Minguo Calendar", "display name");
        check(MinguoChronology.INSTANCE.eraOf(1).toString().equals("ROC"), "eraOf ROC");
        check(MinguoChronology.INSTANCE.eraOf(0).toString().equals("BEFORE_ROC"), "eraOf BEFORE_ROC");
        xact(() -> MinguoChronology.INSTANCE.eraOf(2), java.time.DateTimeException.class, "Invalid era: 2", "eraOf 2");
        check(MinguoChronology.INSTANCE.eras().size() == 2, "two eras");
        java.time.Clock fixed = java.time.Clock.fixed(java.time.Instant.parse("2024-06-15T10:15:30Z"), java.time.ZoneOffset.UTC);
        xeq(MinguoChronology.INSTANCE.dateNow(fixed).toString(), "Minguo ROC 113-06-15", "dateNow fixed clock");
        check(MinguoDate.of(113, 6, 15).get(java.time.temporal.ChronoField.YEAR) == 113, "proleptic year passthrough");
        check(MinguoChronology.INSTANCE.compareTo(ThaiBuddhistChronology.INSTANCE) < 0, "id ordering");
    }

}
