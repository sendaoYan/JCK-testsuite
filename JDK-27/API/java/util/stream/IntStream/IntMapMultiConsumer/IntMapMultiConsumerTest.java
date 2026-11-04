/*
 * @test
 * @summary java.util.stream.IntStream.IntMapMultiConsumer conformance: white-box structure + black-box behavior.
 * @run main IntMapMultiConsumerTest
 */

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class IntMapMultiConsumerTest {
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
        Class<?> c = Class.forName("java.util.stream.IntStream$IntMapMultiConsumer");
        check(c.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method");
        boolean hasAccept = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == int.class
                        && m.getParameterTypes()[1] == IntConsumer.class);
        check(hasAccept, "accept(int, IntConsumer) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        int[] got = IntStream.of(1, 2, 3)
                .mapMulti((int x, IntConsumer sink) -> { sink.accept(x); sink.accept(-x); })
                .toArray();
        check(Arrays.equals(got, new int[]{1, -1, 2, -2, 3, -3}),
              "each element expanded into itself and its negation");

        int[] empty = IntStream.empty()
                .mapMulti((int x, IntConsumer sink) -> sink.accept(x))
                .toArray();
        check(empty.length == 0, "empty stream maps to empty");

        int[] suppressed = IntStream.of(5)
                .mapMulti((int x, IntConsumer sink) -> {})
                .toArray();
        check(suppressed.length == 0, "suppressing every element yields empty");

        int[] filtered = IntStream.of(1, 2, 3, 4, 5, 6)
                .mapMulti((int x, IntConsumer sink) -> { if (x % 2 == 1) sink.accept(x); })
                .toArray();
        check(Arrays.equals(filtered, new int[]{1, 3, 5}), "conditional emission acts like filter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
