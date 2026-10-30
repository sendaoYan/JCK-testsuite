/*
 * @test
 * @summary java.time.temporal.TemporalAmount API conformance: Period as a temporal amount.
 * @run main TemporalAmountTest
 */

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

public class TemporalAmountTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAmount() {
        TemporalAmount ta = Period.ofDays(3);
        check(ta instanceof Period, "Period is a TemporalAmount");
        check(ta.getUnits().contains(ChronoUnit.DAYS), "unit days present");
        LocalDate start = LocalDate.of(2024, 1, 1);
        check(start.plus(ta).equals(LocalDate.of(2024, 1, 4)), "plus period");
    }

    public static void main(String[] args) {
        testTemporalAmount();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
