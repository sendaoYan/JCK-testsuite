/*
 * @test
 * @summary java.util.stream.LongStream.LongMapMultiConsumer conformance: white-box structure + black-box behavior.
 * @run main LongMapMultiConsumerTest
 */

import java.util.Arrays;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

public class LongMapMultiConsumerTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.LongStream$LongMapMultiConsumer");
        check(c.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method");
        boolean hasAccept = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == long.class
                        && m.getParameterTypes()[1] == LongConsumer.class);
        check(hasAccept, "accept(long, LongConsumer) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        long[] got = LongStream.of(1L, 2L, 3L)
                .mapMulti((long x, LongConsumer sink) -> { sink.accept(x); sink.accept(-x); })
                .toArray();
        check(Arrays.equals(got, new long[]{1L, -1L, 2L, -2L, 3L, -3L}),
              "each element expanded into itself and its negation");

        long[] empty = LongStream.empty()
                .mapMulti((long x, LongConsumer sink) -> sink.accept(x))
                .toArray();
        check(empty.length == 0, "empty stream maps to empty");

        long[] scaled = LongStream.of(1L, 2L)
                .mapMulti((long x, LongConsumer sink) -> { sink.accept(x); sink.accept(x * 10); })
                .toArray();
        check(Arrays.equals(scaled, new long[]{1L, 10L, 2L, 20L}), "multiple emissions per element");

        long[] suppressed = LongStream.of(9L)
                .mapMulti((long x, LongConsumer sink) -> {})
                .toArray();
        check(suppressed.length == 0, "suppressing every element yields empty");
    }


    // ===================== 契约深化：作为 mapMulti 函数式接口丢弃元素（JDK-27 侧锁定） =====================
    static void testContracts() {
        java.util.stream.LongStream.LongMapMultiConsumer dropped =
                (long value, java.util.function.LongConsumer sink) -> { if (value % 2 == 0) sink.accept(value); };
        final long[] acc = {0L};
        java.util.function.LongConsumer sink = v -> acc[0] += v;
        dropped.accept(4L, sink);
        check(acc[0] == 4L, "even value forwarded");
        dropped.accept(3L, sink);
        check(acc[0] == 4L, "odd value dropped (sink not invoked)");
        check(java.util.stream.LongStream.of(1, 2, 3, 4)
              .mapMulti((long v, java.util.function.LongConsumer sink2) -> { if (v % 2 == 0) sink2.accept(v); })
              .toArray().length == 2, "mapMulti drops odd elements end-to-end");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
