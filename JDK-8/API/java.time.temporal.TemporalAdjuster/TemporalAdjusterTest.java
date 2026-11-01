/*
 * @test
 * @summary java.time.temporal.TemporalAdjuster conformance: adjustInto.
 * @run main TemporalAdjusterTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;

public class TemporalAdjusterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAdjuster() {
        TemporalAdjuster adjuster = temporal -> temporal.plus(1, ChronoUnit.DAYS);
        LocalDate result = (LocalDate) adjuster.adjustInto(LocalDate.of(2020, 1, 1));
        check(result.equals(LocalDate.of(2020, 1, 2)), "adjustInto adds one day");
    }

    public static void main(String[] args) {
        testTemporalAdjuster();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
