/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAccumulator conformance: accumulate/get/reset.
 * @run main DoubleAccumulatorTest
 */

import java.util.concurrent.atomic.DoubleAccumulator;

public class DoubleAccumulatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccumulator() {
        DoubleAccumulator acc = new DoubleAccumulator((a, b) -> a + b, 0.0);
        check(acc.get() == 0.0, "identity");
        acc.accumulate(2.0);
        acc.accumulate(3.0);
        check(acc.get() == 5.0, "accumulate/get");
        acc.reset();
        check(acc.get() == 0.0, "reset to identity");
        acc.accumulate(4.0);
        check(acc.getThenReset() == 4.0, "getThenReset returns old");
        check(acc.get() == 0.0, "getThenReset resets");
        check(acc.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAccumulator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
