/*
 * @test
 * @summary java.time.Period API conformance: of/getters, isZero/isNegative, plus, and
 *          toTotalMonths.
 * @run main PeriodTest
 */

import java.time.Period;

public class PeriodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPeriod() {
        Period p = Period.of(1, 2, 3);
        check(p.getYears() == 1 && p.getMonths() == 2 && p.getDays() == 3, "of/getters");
        check(Period.ZERO.isZero() && !p.isZero(), "isZero");
        check(Period.ofMonths(-1).isNegative(), "isNegative");
        check(p.plus(Period.ofDays(1)).getDays() == 4, "plus");
        check(Period.ofYears(1).toTotalMonths() == 12, "toTotalMonths");
    }

    public static void main(String[] args) {
        testPeriod();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
