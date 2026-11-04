/*
 * @test
 * @summary java.util.Spliterators.AbstractIntSpliterator conformance.
 * @run main AbstractIntSpliteratorTest
 */

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;

public class AbstractIntSpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractIntSpliterator() {
        Spliterator.OfInt sp = new Spliterators.AbstractIntSpliterator(2, Spliterator.SIZED) {
            int i = 0;
            public boolean tryAdvance(IntConsumer action) {
                if (i < 2) { action.accept(i * 10); i++; return true; }
                return false;
            }
        };
        check(sp.estimateSize() == 2, "estimateSize");
        final int[] sum = new int[1];
        sp.forEachRemaining((IntConsumer) x -> sum[0] += x);
        check(sum[0] == 10, "forEachRemaining");
    }

    public static void main(String[] args) {
        testAbstractIntSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
