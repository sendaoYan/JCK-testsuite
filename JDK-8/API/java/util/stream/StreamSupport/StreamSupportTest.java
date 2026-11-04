/*
 * @test
 * @summary java.util.stream.StreamSupport API conformance: stream/intStream/longStream/doubleStream.
 * @run main StreamSupportTest
 */

import java.util.Arrays;
import java.util.List;
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

    static void testStreamSupport() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Stream<Integer> s = StreamSupport.stream(list.spliterator(), false);
        check(s.count() == 3 && !s.isParallel(), "stream (sequential)");
        Stream<Integer> sp = StreamSupport.stream(list.spliterator(), true);
        check(sp.isParallel(), "stream (parallel)");

        IntStream is = StreamSupport.intStream(Arrays.spliterator(new int[] {1, 2, 3}), false);
        check(is.sum() == 6, "intStream");
        IntStream isp = StreamSupport.intStream(Arrays.spliterator(new int[] {1, 2, 3}), true);
        check(isp.isParallel(), "intStream (parallel)");

        LongStream ls = StreamSupport.longStream(Arrays.spliterator(new long[] {1L, 2L}), false);
        check(ls.sum() == 3L, "longStream");
        LongStream lsp = StreamSupport.longStream(Arrays.spliterator(new long[] {1L, 2L}), true);
        check(lsp.isParallel(), "longStream (parallel)");

        DoubleStream ds = StreamSupport.doubleStream(Arrays.spliterator(new double[] {1.0, 2.0}), false);
        check(ds.sum() == 3.0, "doubleStream");
        DoubleStream dsp = StreamSupport.doubleStream(Arrays.spliterator(new double[] {1.0, 2.0}), true);
        check(dsp.isParallel(), "doubleStream (parallel)");
    }

    public static void main(String[] args) {
        testStreamSupport();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
