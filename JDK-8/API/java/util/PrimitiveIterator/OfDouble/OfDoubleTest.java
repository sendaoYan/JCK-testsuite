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


    // ===================== 契约深化：next 混用/装箱 Consumer 重载/耗尽异常 =====================
    static void testOfDoubleContracts() {
        java.util.PrimitiveIterator.OfDouble it = java.util.stream.DoubleStream.of(0.5, 1.5).iterator();
        check(it.nextDouble() == 0.5 && it.next().equals(Double.valueOf(1.5)),
              "nextDouble() and boxed next() interoperate");
        try { it.nextDouble(); check(false, "exhausted nextDouble NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextDouble NSEE"); }
        final java.util.List<Double> boxed = new java.util.ArrayList<Double>();
        java.util.stream.DoubleStream.of(1.5, 2.5).iterator()
              .forEachRemaining((java.util.function.Consumer<Double>) boxed::add);
        check(boxed.size() == 2 && boxed.get(0) == 1.5 && boxed.get(1) == 2.5,
              "boxed Consumer overload drains all elements");
        check(!java.util.stream.DoubleStream.empty().iterator().hasNext(), "empty stream iterator");
    }

    public static void main(String[] args) {
        testOfDouble();
        testOfDoubleContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
