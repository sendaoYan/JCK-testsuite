/*
 * @test
 * @summary java.util.Spliterator.OfLong conformance: estimateSize/forEachRemaining/trySplit.
 * @run main OfLongTest
 */

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.LongConsumer;

public class OfLongTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfLong() {
        Spliterator.OfLong sp = Arrays.spliterator(new long[] { 1L, 2L, 3L });
        check(sp instanceof Spliterator.OfLong, "instanceof");
        check(sp.estimateSize() == 3, "estimateSize");
        final long[] sum = new long[1];
        sp.forEachRemaining((LongConsumer) x -> sum[0] += x);
        check(sum[0] == 6L, "forEachRemaining");

        Spliterator.OfLong sp2 = Arrays.spliterator(new long[] { 5L, 6L, 7L });
        Spliterator.OfLong split = sp2.trySplit();
        long total = sp2.estimateSize() + (split == null ? 0 : split.estimateSize());
        check(total == 3, "trySplit preserves total size");
    }

    public static void main(String[] args) {
        testOfLong();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
