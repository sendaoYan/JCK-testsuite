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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
