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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
