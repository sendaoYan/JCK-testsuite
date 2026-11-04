/*
 * @test
 * @summary java.time.chrono.ChronoPeriod conformance: white-box structure + black-box behavior.
 * @run main ChronoPeriodTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
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

    static void expectThrows(Class<? extends Throwable> t, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + t.getSimpleName() + " got " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError(msg + ": expected " + t.getSimpleName() + " but nothing thrown");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.chrono.ChronoPeriod");
        check(c.isInterface(), "ChronoPeriod is an interface");
        check(TemporalAmount.class.isAssignableFrom(c), "extends TemporalAmount");
        for (String m : new String[]{"between", "get", "getUnits", "getChronology", "isZero",
                "isNegative", "plus", "minus", "multipliedBy", "negated", "normalized", "addTo",
                "subtractFrom", "equals", "hashCode", "toString"}) {
            check(hasMethod(c, m), "ChronoPeriod has method " + m);
        }
    }

    static void testBlackBox() {
        ChronoPeriod p = Period.of(1, 2, 3);
        check(p.getChronology() == IsoChronology.INSTANCE, "getChronology");
        check(p.get(ChronoUnit.YEARS) == 1, "years");
        check(p.get(ChronoUnit.MONTHS) == 2, "months");
        check(p.get(ChronoUnit.DAYS) == 3, "days");

        // getUnits returns YEARS, MONTHS, DAYS.
        List<TemporalUnit> units = p.getUnits();
        check(units.size() == 3, "three units");
        check(units.contains(ChronoUnit.YEARS) && units.contains(ChronoUnit.MONTHS)
              && units.contains(ChronoUnit.DAYS), "units are Y/M/D");

        // zero / negative.
        check(!p.isZero(), "not zero");
        check(!p.isNegative(), "not negative");
        check(Period.ZERO.isZero(), "ZERO is zero");
        check(Period.of(0, 0, 0).isZero(), "of(0,0,0) is zero");
        check(Period.of(-1, 0, 0).isNegative(), "negative year");

        // negated / multipliedBy.
        ChronoPeriod neg = p.negated();
        check(neg.get(ChronoUnit.YEARS) == -1 && neg.get(ChronoUnit.MONTHS) == -2
              && neg.get(ChronoUnit.DAYS) == -3, "negated values");
        check(neg.isNegative(), "negated is negative");
        check(p.multipliedBy(2).get(ChronoUnit.YEARS) == 2
              && p.multipliedBy(2).get(ChronoUnit.DAYS) == 6, "multipliedBy 2");
        check(p.multipliedBy(-1).equals(neg), "multipliedBy -1 == negated");

        // normalized (months -> years).
        ChronoPeriod norm = Period.of(0, 13, 0).normalized();
        check(norm.get(ChronoUnit.YEARS) == 1 && norm.get(ChronoUnit.MONTHS) == 1, "normalized 13m -> 1y1m");
        check(p.normalized().equals(Period.of(1, 2, 3)), "normalized no-op");

        // plus / minus.
        ChronoPeriod sum = p.plus(Period.of(1, 1, 1));
        check(sum.get(ChronoUnit.YEARS) == 2 && sum.get(ChronoUnit.MONTHS) == 3
              && sum.get(ChronoUnit.DAYS) == 4, "plus");
        ChronoPeriod diff = p.minus(Period.of(0, 1, 0));
        check(diff.get(ChronoUnit.MONTHS) == 1, "minus");

        // between (static factory).
        ChronoPeriod between = ChronoPeriod.between(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 2, 3));
        check(between.get(ChronoUnit.YEARS) == 1 && between.get(ChronoUnit.MONTHS) == 1
              && between.get(ChronoUnit.DAYS) == 2, "between 2020-01-01..2021-02-03");

        // addTo / subtractFrom (TemporalAmount contract).
        check(p.addTo(LocalDate.of(2020, 1, 1)).equals(LocalDate.of(2021, 3, 4)), "addTo");
        check(p.subtractFrom(LocalDate.of(2021, 3, 4)).equals(LocalDate.of(2020, 1, 1)), "subtractFrom");

        // toString is stable.
        check("P1Y2M3D".equals(p.toString()), "toString P1Y2M3D");

        // addTo with unsupported target throws UnsupportedTemporalTypeException.
        expectThrows(java.time.temporal.UnsupportedTemporalTypeException.class,
              () -> p.addTo(java.time.LocalTime.NOON),
              "addTo LocalTime throws UnsupportedTemporalTypeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
