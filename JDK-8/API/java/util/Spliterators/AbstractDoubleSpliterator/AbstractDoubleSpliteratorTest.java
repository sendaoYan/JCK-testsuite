/*
 * @test
 * @summary java.util.Spliterators.AbstractDoubleSpliterator conformance.
 * @run main AbstractDoubleSpliteratorTest
 */

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.DoubleConsumer;

public class AbstractDoubleSpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractDoubleSpliterator() {
        Spliterator.OfDouble sp = new Spliterators.AbstractDoubleSpliterator(2, Spliterator.SIZED) {
            int i = 0;
            public boolean tryAdvance(DoubleConsumer action) {
                if (i < 2) { action.accept(i * 10.0); i++; return true; }
                return false;
            }
        };
        check(sp.estimateSize() == 2, "estimateSize");
        final double[] sum = new double[1];
        sp.forEachRemaining((DoubleConsumer) x -> sum[0] += x);
        check(sum[0] == 10.0, "forEachRemaining");
    }

    public static void main(String[] args) {
        testAbstractDoubleSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
