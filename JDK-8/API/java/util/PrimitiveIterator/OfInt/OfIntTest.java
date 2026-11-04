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


    // ===================== 契约深化：next 混用/装箱 Consumer 重载/耗尽异常 =====================
    static void testOfIntContracts() {
        PrimitiveIterator.OfInt it = IntStream.of(7, 8).iterator();
        check(it.nextInt() == 7 && it.next().equals(Integer.valueOf(8)),
              "nextInt() and boxed next() interoperate");
        try { it.nextInt(); check(false, "exhausted nextInt NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextInt NSEE"); }
        final java.util.List<Integer> boxed = new java.util.ArrayList<Integer>();
        IntStream.of(1, 2).iterator().forEachRemaining((java.util.function.Consumer<Integer>) boxed::add);
        check(boxed.size() == 2 && boxed.get(0) == 1 && boxed.get(1) == 2,
              "boxed Consumer overload drains all elements");
        check(!IntStream.empty().iterator().hasNext(), "empty stream iterator");
    }

    public static void main(String[] args) {
        testOfInt();
        testOfIntContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
