/*
 * @test
 * @summary java.util.concurrent.atomic.LongAccumulator conformance: accumulate/get/reset.
 * @run main LongAccumulatorTest
 */

import java.util.concurrent.atomic.LongAccumulator;

public class LongAccumulatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAccumulator() {
        LongAccumulator acc = new LongAccumulator((a, b) -> a * b, 1L);
        check(acc.get() == 1L, "identity");
        acc.accumulate(2L);
        acc.accumulate(3L);
        check(acc.get() == 6L, "accumulate/get");
        acc.reset();
        check(acc.get() == 1L, "reset to identity");
        acc.accumulate(4L);
        check(acc.getThenReset() == 4L, "getThenReset returns old");
        check(acc.get() == 1L, "getThenReset resets");
        check(acc.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAccumulator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
