/*
 * @test
 * @summary java.util.stream.Gatherer.Integrator.Greedy conformance: marker subtype of Integrator with greedy semantics.
 * @run main GreedyTest
 */

import java.util.stream.Gatherer;

public class GreedyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHierarchy() {
        check(Gatherer.Integrator.Greedy.class.isInterface(), "Greedy is an interface");
        check(Gatherer.Integrator.class.isAssignableFrom(Gatherer.Integrator.Greedy.class),
              "Greedy extends Integrator");
        check(Gatherer.Integrator.Greedy.class.getGenericInterfaces().length == 1,
              "Greedy has exactly one direct superinterface");
    }

    static void testGreedyFlagSemantics() {
        Gatherer.Integrator<Void, Integer, Integer> plain =
                Gatherer.Integrator.of((s, e, d) -> d.push(e));
        Gatherer.Integrator.Greedy<Void, Integer, Integer> greedy =
                Gatherer.Integrator.ofGreedy((s, e, d) -> d.push(e));

        check(greedy instanceof Gatherer.Integrator.Greedy, "ofGreedy yields Greedy");
        check(!(plain instanceof Gatherer.Integrator.Greedy), "of yields non-greedy Integrator");
    }

    public static void main(String[] args) {
        testHierarchy();
        testGreedyFlagSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
