/*
 * @test
 * @summary java.time.chrono.ChronoPeriod conformance: via Period.
 * @run main ChronoPeriodTest
 */

import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.chrono.JapaneseChronology;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoChronology;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.chrono.ThaiBuddhistDate;

public class ChronoPeriodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testChronoPeriod() {
        ChronoPeriod p = Period.of(1, 2, 3);
        check(p.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(p.get(ChronoUnit.YEARS) == 1, "years");
        check(p.get(ChronoUnit.MONTHS) == 2, "months");
        check(p.get(ChronoUnit.DAYS) == 3, "days");
        check(p.getUnits().size() == 3, "three units");
        check(!p.isZero(), "not zero");
        check(!p.isNegative(), "not negative");
        check(Period.ZERO.isZero(), "ZERO is zero");
        check(p.negated().isNegative(), "negated is negative");
        check(p.multipliedBy(2).get(ChronoUnit.YEARS) == 2, "multipliedBy");
        check(p.normalized() != null, "normalized");
    }

    public static void main(String[] args) {
        testChronoPeriod();
        testDeepChronoPeriod();
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


    static void testDeepChronoPeriod() {
        ChronoPeriod jp = JapaneseChronology.INSTANCE.period(1, 2, 3);
        xeq(jp.toString(), "Japanese P1Y2M3D", "jp period toString");
        check(jp.getChronology().equals(JapaneseChronology.INSTANCE), "jp period chronology");
        xeq(jp.negated().toString(), "Japanese P-1Y-2M-3D", "jp negated");
        xeq(jp.multipliedBy(2).toString(), "Japanese P2Y4M6D", "jp multipliedBy");
        xeq(MinguoChronology.INSTANCE.period(1, 2, 3).plus(MinguoChronology.INSTANCE.period(0, 1, 0)).toString(),
            "Minguo P1Y3M3D", "mg plus");
        xeq(ThaiBuddhistChronology.INSTANCE.period(4, 5, 6).minus(ThaiBuddhistChronology.INSTANCE.period(1, 1, 1)).toString(),
            "ThaiBuddhist P3Y4M5D", "tb minus");
        check(jp.get(java.time.temporal.ChronoUnit.YEARS) == 1, "get YEARS");
        check(jp.get(java.time.temporal.ChronoUnit.MONTHS) == 2, "get MONTHS");
        check(jp.get(java.time.temporal.ChronoUnit.DAYS) == 3, "get DAYS");
        check(jp.getUnits().size() == 3, "three units");
        check(jp.isZero() == false && jp.isNegative() == false, "not zero not negative");
        check(JapaneseChronology.INSTANCE.period(0, 0, 0).isZero(), "zero period");
        check(JapaneseChronology.INSTANCE.period(-1, 0, 0).isNegative(), "negative period");
        xeq(ChronoPeriod.between(java.time.LocalDate.of(2023, 6, 15), java.time.LocalDate.of(2024, 6, 15)).toString(),
            "P1Y", "iso between full year");
        xeq(ChronoPeriod.between(JapaneseDate.of(2024, 6, 15), JapaneseDate.of(2024, 6, 20)).toString(),
            "Japanese P5D", "jp between days");
        xeq(ChronoPeriod.between(MinguoDate.of(113, 1, 31), MinguoDate.of(113, 3, 1)).toString(),
            "Minguo P1M1D", "mg between month-end clamping");
        xeq(ChronoPeriod.between(ThaiBuddhistDate.of(2567, 1, 1), ThaiBuddhistDate.of(2568, 1, 1)).toString(),
            "ThaiBuddhist P1Y", "tb between year");
        check(ChronoPeriod.between(java.time.LocalDate.of(2024, 1, 1), JapaneseDate.of(2024, 6, 15)) != null,
            "cross-chrono between allowed");
        xact(() -> ChronoPeriod.between(null, java.time.LocalDate.of(2024, 1, 1)), NullPointerException.class,
            "startDateInclusive", "between null start");
        xact(() -> ChronoPeriod.between(java.time.LocalDate.of(2024, 1, 1), null), NullPointerException.class,
            "endDateExclusive", "between null end");
    }

}
