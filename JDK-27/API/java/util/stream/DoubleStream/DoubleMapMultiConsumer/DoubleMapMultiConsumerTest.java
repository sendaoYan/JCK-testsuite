/*
 * @test
 * @summary java.util.stream.DoubleStream.DoubleMapMultiConsumer conformance: white-box structure + black-box behavior.
 * @run main DoubleMapMultiConsumerTest
 */

import java.util.Arrays;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

public class DoubleMapMultiConsumerTest {
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
        Class<?> c = Class.forName("java.util.stream.DoubleStream$DoubleMapMultiConsumer");
        check(c.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method");
        boolean hasAccept = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == double.class
                        && m.getParameterTypes()[1] == DoubleConsumer.class);
        check(hasAccept, "accept(double, DoubleConsumer) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        double[] got = DoubleStream.of(1.5, 2.5)
                .mapMulti((double x, DoubleConsumer sink) -> { sink.accept(x); sink.accept(-x); })
                .toArray();
        check(Arrays.equals(got, new double[]{1.5, -1.5, 2.5, -2.5}),
              "each element expanded into itself and its negation");

        double[] empty = DoubleStream.empty()
                .mapMulti((double x, DoubleConsumer sink) -> sink.accept(x))
                .toArray();
        check(empty.length == 0, "empty stream maps to empty");

        double[] scaled = DoubleStream.of(1.0, 2.0)
                .mapMulti((double x, DoubleConsumer sink) -> { sink.accept(x); sink.accept(x * 10); })
                .toArray();
        check(Arrays.equals(scaled, new double[]{1.0, 10.0, 2.0, 20.0}), "multiple emissions per element");

        double[] suppressed = DoubleStream.of(1.0, 2.0, 3.0)
                .mapMulti((double x, DoubleConsumer sink) -> {})
                .toArray();
        check(suppressed.length == 0, "suppressing every element yields empty");

        double[] filtered = DoubleStream.of(1.0, 2.0, 3.0, 4.0)
                .mapMulti((double x, DoubleConsumer sink) -> { if (x % 2 == 0) sink.accept(x); })
                .toArray();
        check(Arrays.equals(filtered, new double[]{2.0, 4.0}), "conditional emission acts like filter");
    }


    // ===================== 契约深化：作为 mapMulti 函数式接口丢弃元素（JDK-27 侧锁定） =====================
    static void testContracts() {
        java.util.stream.DoubleStream.DoubleMapMultiConsumer dropped =
                (double value, java.util.function.DoubleConsumer sink) -> { if (value >= 2.0) sink.accept(value); };
        final double[] acc = {0.0};
        java.util.function.DoubleConsumer sink = v -> acc[0] += v;
        dropped.accept(2.5, sink);
        check(acc[0] == 2.5, "passing value forwarded");
        dropped.accept(1.0, sink);
        check(acc[0] == 2.5, "filtered value dropped (sink not invoked)");
        check(java.util.stream.DoubleStream.of(1.0, 2.0, 3.0)
              .mapMulti((double v, java.util.function.DoubleConsumer sink2) -> { if (v >= 2.0) sink2.accept(v); })
              .toArray().length == 2, "mapMulti filters end-to-end");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
