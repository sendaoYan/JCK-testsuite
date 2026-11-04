/*
 * @test
 * @summary java.util.Spliterator.OfInt conformance: estimateSize/forEachRemaining/trySplit.
 * @run main OfIntTest
 */

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.IntConsumer;

public class OfIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfInt() {
        Spliterator.OfInt sp = Arrays.spliterator(new int[] { 1, 2, 3 });
        check(sp instanceof Spliterator.OfInt, "instanceof");
        check(sp.estimateSize() == 3, "estimateSize");
        check(sp.characteristics() != 0, "characteristics");
        final int[] sum = new int[1];
        sp.forEachRemaining((IntConsumer) x -> sum[0] += x);
        check(sum[0] == 6, "forEachRemaining");

        Spliterator.OfInt sp2 = Arrays.spliterator(new int[] { 5, 6, 7 });
        Spliterator.OfInt split = sp2.trySplit();
        long total = sp2.estimateSize() + (split == null ? 0 : split.estimateSize());
        check(total == 3, "trySplit preserves total size");
    }

    public static void main(String[] args) {
        testOfInt();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
