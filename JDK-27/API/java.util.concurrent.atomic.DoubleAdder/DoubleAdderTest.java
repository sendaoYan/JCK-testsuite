/*
 * @test
 * @summary java.util.concurrent.atomic.DoubleAdder conformance: add/sum/reset.
 * @run main DoubleAdderTest
 */

import java.util.concurrent.atomic.DoubleAdder;

public class DoubleAdderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAdder() {
        DoubleAdder adder = new DoubleAdder();
        check(adder.sum() == 0.0, "initial sum 0");
        adder.add(1.5);
        adder.add(2.5);
        check(adder.sum() == 4.0, "add/sum");
        adder.reset();
        check(adder.sum() == 0.0, "reset");
        adder.add(3.0);
        check(adder.sumThenReset() == 3.0, "sumThenReset returns old");
        check(adder.sum() == 0.0, "sumThenReset resets");
        check(adder.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testAdder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
