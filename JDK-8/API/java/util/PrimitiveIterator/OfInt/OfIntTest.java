/*
 * @test
 * @summary java.util.PrimitiveIterator.OfInt conformance: nextInt/forEachRemaining.
 * @run main OfIntTest
 */

import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class OfIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfInt() {
        PrimitiveIterator.OfInt it = IntStream.of(1, 2, 3).iterator();
        check(it instanceof PrimitiveIterator.OfInt, "instanceof");
        check(it.hasNext(), "hasNext");
        check(it.nextInt() == 1, "nextInt first");
        check(it.nextInt() == 2 && it.nextInt() == 3, "nextInt advances");
        check(!it.hasNext(), "exhausted");

        final int[] sum = new int[1];
        IntStream.of(4, 5).iterator().forEachRemaining((IntConsumer) x -> sum[0] += x);
        check(sum[0] == 9, "forEachRemaining");
    }

    public static void main(String[] args) {
        testOfInt();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
