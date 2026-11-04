/*
 * @test
 * @summary java.util.PrimitiveIterator.OfLong conformance: nextLong/forEachRemaining.
 * @run main OfLongTest
 */

import java.util.PrimitiveIterator;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

public class OfLongTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfLong() {
        PrimitiveIterator.OfLong it = LongStream.of(1L, 2L, 3L).iterator();
        check(it instanceof PrimitiveIterator.OfLong, "instanceof");
        check(it.hasNext(), "hasNext");
        check(it.nextLong() == 1L, "nextLong first");
        check(it.nextLong() == 2L && it.nextLong() == 3L, "nextLong advances");
        check(!it.hasNext(), "exhausted");

        final long[] sum = new long[1];
        LongStream.of(4L, 5L).iterator().forEachRemaining((LongConsumer) x -> sum[0] += x);
        check(sum[0] == 9L, "forEachRemaining");
    }

    public static void main(String[] args) {
        testOfLong();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
