/*
 * @test
 * @summary java.util.PrimitiveIterator.OfDouble conformance: nextDouble/forEachRemaining.
 * @run main OfDoubleTest
 */

import java.util.PrimitiveIterator;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

public class OfDoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOfDouble() {
        PrimitiveIterator.OfDouble it = DoubleStream.of(1.0, 2.0, 3.0).iterator();
        check(it instanceof PrimitiveIterator.OfDouble, "instanceof");
        check(it.hasNext(), "hasNext");
        check(it.nextDouble() == 1.0, "nextDouble first");
        check(it.nextDouble() == 2.0 && it.nextDouble() == 3.0, "nextDouble advances");
        check(!it.hasNext(), "exhausted");

        final double[] sum = new double[1];
        DoubleStream.of(4.0, 5.0).iterator().forEachRemaining((DoubleConsumer) x -> sum[0] += x);
        check(sum[0] == 9.0, "forEachRemaining");
    }

    public static void main(String[] args) {
        testOfDouble();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
