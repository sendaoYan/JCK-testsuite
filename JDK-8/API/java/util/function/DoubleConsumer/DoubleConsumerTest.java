/*
 * @test
 * @summary java.util.function.DoubleConsumer API conformance: accept/andThen.
 * @run main DoubleConsumerTest
 */

import java.util.function.DoubleConsumer;

public class DoubleConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleConsumer() {
        double[] holder = new double[1];
        DoubleConsumer c = x -> holder[0] = x;
        c.accept(9.5);
        check(holder[0] == 9.5, "accept");
        c.andThen(x -> holder[0] += x).accept(0.5);
        check(holder[0] == 1.0, "andThen");
    }

    public static void main(String[] args) {
        testDoubleConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
