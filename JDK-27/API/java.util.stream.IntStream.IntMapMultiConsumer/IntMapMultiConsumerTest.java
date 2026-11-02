/*
 * @test
 * @summary java.util.stream.IntStream.IntMapMultiConsumer conformance: mapMulti primitive element expansion.
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

    static void testMapMulti() {
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
    }

    static void testFunctionalInterface() {
        check(IntStream.IntMapMultiConsumer.class.isInterface(), "is an interface");
        long abstractCount = Arrays.stream(IntStream.IntMapMultiConsumer.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (accept)");
        boolean hasAccept = Arrays.stream(IntStream.IntMapMultiConsumer.class.getMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 2);
        check(hasAccept, "accept(int, IntConsumer) declared");
    }

    public static void main(String[] args) {
        testMapMulti();
        testFunctionalInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
