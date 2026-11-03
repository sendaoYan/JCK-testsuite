/*
 * @test
 * @summary java.util.stream.StreamSupport conformance: white-box structure + black-box behavior.
 * @run main StreamSupportTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
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
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    static boolean hasStatic(Class<?> c, String name, int params) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params
                    && Modifier.isStatic(m.getModifiers())) return true;
        }
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.StreamSupport");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "StreamSupport is final class");
        check(hasStatic(c, "stream", 2), "stream(Spliterator, boolean) declared");
        check(hasStatic(c, "stream", 3), "stream(Supplier, int, boolean) declared");
        check(hasStatic(c, "intStream", 2), "intStream(Spliterator.OfInt, boolean) declared");
        check(hasStatic(c, "intStream", 3), "intStream(Supplier, int, boolean) declared");
        check(hasStatic(c, "longStream", 2), "longStream(Spliterator.OfLong, boolean) declared");
        check(hasStatic(c, "longStream", 3), "longStream(Supplier, int, boolean) declared");
        check(hasStatic(c, "doubleStream", 2), "doubleStream(Spliterator.OfDouble, boolean) declared");
        check(hasStatic(c, "doubleStream", 3), "doubleStream(Supplier, int, boolean) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<Integer> list = Arrays.asList(1, 2, 3);

        // stream(Spliterator, boolean)
        Stream<Integer> s = StreamSupport.stream(list.spliterator(), false);
        check(s.count() == 3 && !s.isParallel(), "stream (sequential)");
        Stream<Integer> sp = StreamSupport.stream(list.spliterator(), true);
        check(sp.isParallel(), "stream (parallel)");

        // stream from an Iterator via Spliterators
        Iterator<String> it = List.of("a", "b", "c").iterator();
        List<String> fromIter = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(it, 0), false)
                .collect(java.util.stream.Collectors.toList());
        check(fromIter.equals(List.of("a", "b", "c")), "stream from iterator spliterator");

        // stream from an Iterable
        List<Integer> fromIterable = StreamSupport.stream(
                ((Iterable<Integer>) list).spliterator(), false)
                .map(x -> x * 2)
                .collect(java.util.stream.Collectors.toList());
        check(fromIterable.equals(List.of(2, 4, 6)), "stream from iterable, map applied");

        // supplier-based stream overload
        List<Integer> supplierList = StreamSupport.stream(
                () -> Arrays.asList(1, 2, 3, 4).spliterator(),
                Spliterator.SIZED | Spliterator.SUBSIZED, false)
                .filter(x -> x % 2 == 0)
                .collect(java.util.stream.Collectors.toList());
        check(supplierList.equals(List.of(2, 4)), "supplier-based stream with characteristics");

        // intStream from an int[] subrange (start/end)
        int[] arr = {10, 20, 30, 40, 50};
        IntStream is = StreamSupport.intStream(
                Arrays.spliterator(arr, 1, 4), false);
        check(is.sum() == 90, "intStream over int[] subrange sums 20+30+40");
        IntStream isp = StreamSupport.intStream(Arrays.spliterator(new int[]{1, 2, 3}), true);
        check(isp.isParallel(), "intStream (parallel)");

        // intStream supplier overload
        IntStream is2 = StreamSupport.intStream(
                () -> Arrays.spliterator(new int[]{1, 2, 3}),
                Spliterator.SIZED | Spliterator.SUBSIZED, false);
        check(is2.map(x -> x * x).sum() == 14, "supplier-based intStream");

        // longStream
        LongStream ls = StreamSupport.longStream(Arrays.spliterator(new long[]{1L, 2L, 3L}), false);
        check(ls.sum() == 6L, "longStream");
        LongStream lsp = StreamSupport.longStream(Arrays.spliterator(new long[]{1L, 2L}), true);
        check(lsp.isParallel(), "longStream (parallel)");

        // doubleStream
        DoubleStream ds = StreamSupport.doubleStream(Arrays.spliterator(new double[]{1.0, 2.0, 3.0}), false);
        check(ds.sum() == 6.0, "doubleStream");
        DoubleStream dsp = StreamSupport.doubleStream(Arrays.spliterator(new double[]{1.0, 2.0}), true);
        check(dsp.isParallel(), "doubleStream (parallel)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
