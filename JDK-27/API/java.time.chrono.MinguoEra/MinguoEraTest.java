/*
 * @test
 * @summary java.time.chrono.MinguoEra conformance: BEFORE_ROC/ROC values.
 * @run main MinguoEraTest
 */

import java.time.chrono.MinguoEra;

public class MinguoEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMinguoEra() {
        check(MinguoEra.BEFORE_ROC.getValue() == 0, "BEFORE_ROC value");
        check(MinguoEra.ROC.getValue() == 1, "ROC value");
        check(MinguoEra.valueOf("ROC") == MinguoEra.ROC, "valueOf ROC");
        check(MinguoEra.values().length == 2, "two values");
        check(MinguoEra.of(1) == MinguoEra.ROC, "of(1) ROC");
    }

    public static void main(String[] args) {
        testMinguoEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
