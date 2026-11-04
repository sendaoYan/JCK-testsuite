/*
 * @test
 * @summary java.util.function.LongConsumer API conformance: accept/andThen.
 * @run main LongConsumerTest
 */

import java.util.function.LongConsumer;

public class LongConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongConsumer() {
        long[] holder = new long[1];
        LongConsumer c = x -> holder[0] = x;
        c.accept(9L);
        check(holder[0] == 9L, "accept");
        c.andThen(x -> holder[0] += x).accept(5L);
        check(holder[0] == 10L, "andThen");
    }

    public static void main(String[] args) {
        testLongConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
