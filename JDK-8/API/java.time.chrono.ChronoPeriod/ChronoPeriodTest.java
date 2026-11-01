/*
 * @test
 * @summary java.time.chrono.ChronoPeriod conformance: via Period.
 * @run main ChronoPeriodTest
 */

import java.time.Period;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoUnit;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
