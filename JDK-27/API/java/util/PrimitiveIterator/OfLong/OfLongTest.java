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


    // ===================== 契约深化：next 混用/装箱 Consumer 重载/耗尽异常 =====================
    static void testOfLongContracts() {
        java.util.PrimitiveIterator.OfLong it = java.util.stream.LongStream.of(7L, 8L).iterator();
        check(it.nextLong() == 7L && it.next().equals(Long.valueOf(8L)),
              "nextLong() and boxed next() interoperate");
        try { it.nextLong(); check(false, "exhausted nextLong NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextLong NSEE"); }
        final java.util.List<Long> boxed = new java.util.ArrayList<Long>();
        java.util.stream.LongStream.of(1L, 2L).iterator()
              .forEachRemaining((java.util.function.Consumer<Long>) boxed::add);
        check(boxed.size() == 2 && boxed.get(0) == 1L && boxed.get(1) == 2L,
              "boxed Consumer overload drains all elements");
        check(!java.util.stream.LongStream.empty().iterator().hasNext(), "empty stream iterator");
    }

    public static void main(String[] args) {
        testOfLong();
        testOfLongContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
