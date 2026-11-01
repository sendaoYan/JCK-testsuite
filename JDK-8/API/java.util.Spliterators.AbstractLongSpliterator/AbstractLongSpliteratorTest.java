/*
 * @test
 * @summary java.util.Spliterators.AbstractLongSpliterator conformance.
 * @run main AbstractLongSpliteratorTest
 */

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.LongConsumer;

public class AbstractLongSpliteratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractLongSpliterator() {
        Spliterator.OfLong sp = new Spliterators.AbstractLongSpliterator(2, Spliterator.SIZED) {
            int i = 0;
            public boolean tryAdvance(LongConsumer action) {
                if (i < 2) { action.accept(i * 10L); i++; return true; }
                return false;
            }
        };
        check(sp.estimateSize() == 2, "estimateSize");
        final long[] sum = new long[1];
        sp.forEachRemaining((LongConsumer) x -> sum[0] += x);
        check(sum[0] == 10L, "forEachRemaining");
    }

    public static void main(String[] args) {
        testAbstractLongSpliterator();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
