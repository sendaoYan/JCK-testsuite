/*
 * @test
 * @summary java.util.stream.Gatherer conformance: functional interface, factory methods, accessors, andThen.
 * @run main GathererTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class GathererTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFunctionalInterface() {
        check(Gatherer.class.isInterface(), "Gatherer is an interface");
        long abstractCount = Arrays.stream(Gatherer.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (integrator)");
    }

    static void testOfAndAccessors() {
        Gatherer<Integer, Void, Integer> identity =
                Gatherer.of((Void state, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e));
        check(identity.initializer() != null, "initializer() non-null");
        check(identity.integrator() != null, "integrator() non-null");
        check(identity.combiner() != null, "combiner() non-null");
        check(identity.finisher() != null, "finisher() non-null");
        check(identity.initializer().get() == null, "default initializer yields null (Void state)");
        List<Integer> out = Stream.of(1, 2, 3).gather(identity).toList();
        check(out.equals(List.of(1, 2, 3)), "identity gather passes elements through");
    }

    static void testOfSequentialWithState() {
        Gatherer<Integer, int[], Integer> counter = Gatherer.ofSequential(
                () -> new int[1],
                (int[] state, Integer e, Gatherer.Downstream<? super Integer> d) -> { state[0]++; return true; },
                (int[] state, Gatherer.Downstream<? super Integer> d) -> d.push(state[0]));
        List<Integer> out = Stream.of(1, 2, 3, 4).gather(counter).toList();
        check(out.equals(List.of(4)), "stateful ofSequential counts elements and finishes once");
    }

    static void testAndThen() {
        Gatherer<Integer, ?, Integer> plusOne =
                Gatherer.of((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e + 1));
        Gatherer<Integer, ?, Integer> timesTwo =
                Gatherer.of((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e * 2));
        List<Integer> out = Stream.of(1, 2, 3).gather(plusOne.andThen(timesTwo)).toList();
        check(out.equals(List.of(4, 6, 8)), "andThen composes gatherers ((e+1)*2)");
    }

    public static void main(String[] args) {
        testFunctionalInterface();
        testOfAndAccessors();
        testOfSequentialWithState();
        testAndThen();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
