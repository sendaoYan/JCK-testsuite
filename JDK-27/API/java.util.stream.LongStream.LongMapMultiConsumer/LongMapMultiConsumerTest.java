/*
 * @test
 * @summary java.util.stream.LongStream.LongMapMultiConsumer conformance: mapMulti primitive element expansion.
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

    static void testMapMulti() {
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
    }

    static void testFunctionalInterface() {
        check(LongStream.LongMapMultiConsumer.class.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(LongStream.LongMapMultiConsumer.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (accept)");
        boolean hasAccept = Arrays.stream(LongStream.LongMapMultiConsumer.class.getMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2);
        check(hasAccept, "accept(long, LongConsumer) declared");
    }

    public static void main(String[] args) {
        testMapMulti();
        testFunctionalInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
