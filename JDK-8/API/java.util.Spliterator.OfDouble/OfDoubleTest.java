/*
 * @test
 * @summary java.util.Spliterator.OfDouble conformance: estimateSize/forEachRemaining/trySplit.
 * @run main OfDoubleTest
 */

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.DoubleConsumer;

public class OfDoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfDouble() {
        Spliterator.OfDouble sp = Arrays.spliterator(new double[] { 1.0, 2.0, 3.0 });
        check(sp instanceof Spliterator.OfDouble, "instanceof");
        check(sp.estimateSize() == 3, "estimateSize");
        final double[] sum = new double[1];
        sp.forEachRemaining((DoubleConsumer) x -> sum[0] += x);
        check(sum[0] == 6.0, "forEachRemaining");

        Spliterator.OfDouble sp2 = Arrays.spliterator(new double[] { 5.0, 6.0, 7.0 });
        Spliterator.OfDouble split = sp2.trySplit();
        long total = sp2.estimateSize() + (split == null ? 0 : split.estimateSize());
        check(total == 3, "trySplit preserves total size");
    }

    public static void main(String[] args) {
        testOfDouble();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
