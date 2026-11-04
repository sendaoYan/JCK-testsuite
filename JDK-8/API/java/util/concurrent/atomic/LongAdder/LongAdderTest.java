/*
 * @test
 * @summary java.util.concurrent.atomic.LongAdder conformance: add/increment/decrement/sum/reset.
 * @run main LongAdderTest
 */

import java.util.concurrent.atomic.LongAdder;

public class LongAdderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAdder() {
        LongAdder adder = new LongAdder();
        check(adder.sum() == 0L, "initial sum 0");
        adder.add(1L);
        adder.add(2L);
        check(adder.sum() == 3L, "add/sum");
        adder.increment();
        check(adder.sum() == 4L, "increment");
        adder.decrement();
        check(adder.sum() == 3L, "decrement");
        adder.reset();
        check(adder.sum() == 0L, "reset");
        adder.add(5L);
        check(adder.sumThenReset() == 5L, "sumThenReset returns old");
        check(adder.sum() == 0L, "sumThenReset resets");
        check(adder.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAdder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
