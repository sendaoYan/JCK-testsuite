/*
 * @test
 * @summary java.util.function.IntConsumer API conformance: accept/andThen.
 * @run main IntConsumerTest
 */

import java.util.function.IntConsumer;

public class IntConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntConsumer() {
        int[] holder = new int[1];
        IntConsumer c = x -> holder[0] = x;
        c.accept(9);
        check(holder[0] == 9, "accept");
        c.andThen(x -> holder[0] += x).accept(5);
        check(holder[0] == 10, "andThen");
    }

    public static void main(String[] args) {
        testIntConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
