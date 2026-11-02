/*
 * @test
 * @summary java.util.stream.DoubleStream.DoubleMapMultiConsumer conformance: mapMulti primitive element expansion.
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

    static void testMapMulti() {
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
    }

    static void testFunctionalInterface() {
        check(DoubleStream.DoubleMapMultiConsumer.class.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(DoubleStream.DoubleMapMultiConsumer.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (accept)");
        boolean hasAccept = Arrays.stream(DoubleStream.DoubleMapMultiConsumer.class.getMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2);
        check(hasAccept, "accept(double, DoubleConsumer) declared");
    }

    public static void main(String[] args) {
        testMapMulti();
        testFunctionalInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
