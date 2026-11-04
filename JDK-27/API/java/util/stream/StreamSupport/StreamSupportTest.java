/*
 * @test
 * @summary java.util.stream.StreamSupport contract: stream/intStream/longStream/doubleStream
 *          from spliterator and supplier, null->NPE, characteristics propagation.
 * @run main StreamSupportTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testStream() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Stream<Integer> s = StreamSupport.stream(list.spliterator(), false);
        check(s.count() == 3 && !s.isParallel(), "stream sequential");
        Stream<Integer> sp = StreamSupport.stream(list.spliterator(), true);
        check(sp.isParallel(), "stream parallel");
        checkThrows(NullPointerException.class, () -> StreamSupport.stream((Spliterator<Integer>) null, false), "stream null spliterator NPE");
    }

    static void testStreamSupplier() {
        Supplier<Spliterator<Integer>> sup = () -> Arrays.asList(1, 2, 3).spliterator();
        Stream<Integer> s = StreamSupport.stream(sup, Spliterator.SIZED | Spliterator.ORDERED, false);
        check(s.count() == 3 && !s.isParallel(), "stream supplier sequential");
        Stream<Integer> sp = StreamSupport.stream(sup, Spliterator.SIZED | Spliterator.ORDERED, true);
        check(sp.isParallel(), "stream supplier parallel");
        checkThrows(NullPointerException.class, () -> StreamSupport.stream((Supplier<Spliterator<Integer>>) null, 0, false), "stream null supplier NPE");
    }

    static void testPrimitiveStreams() {
        check(StreamSupport.intStream(Arrays.spliterator(new int[]{1, 2, 3}), false).sum() == 6, "intStream");
        check(StreamSupport.intStream(Arrays.spliterator(new int[]{1, 2, 3}), true).isParallel(), "intStream parallel");
        check(StreamSupport.longStream(Arrays.spliterator(new long[]{1L, 2L}), false).sum() == 3L, "longStream");
        check(StreamSupport.longStream(Arrays.spliterator(new long[]{1L, 2L}), true).isParallel(), "longStream parallel");
        check(StreamSupport.doubleStream(Arrays.spliterator(new double[]{1.0, 2.0}), false).sum() == 3.0, "doubleStream");
        check(StreamSupport.doubleStream(Arrays.spliterator(new double[]{1.0, 2.0}), true).isParallel(), "doubleStream parallel");
        checkThrows(NullPointerException.class, () -> StreamSupport.intStream(null, false), "intStream null NPE");
        checkThrows(NullPointerException.class, () -> StreamSupport.longStream(null, false), "longStream null NPE");
        checkThrows(NullPointerException.class, () -> StreamSupport.doubleStream(null, false), "doubleStream null NPE");
    }

    public static void main(String[] args) {
        testStream();
        testStreamSupplier();
        testPrimitiveStreams();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
