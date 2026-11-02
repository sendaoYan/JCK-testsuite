/*
 * @test
 * @summary java.util.stream.Gatherer.Integrator conformance: integrate, of, ofGreedy.
 * @run main IntegratorTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public class IntegratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class CollectingDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        public boolean push(Integer e) { collected.add(e); return true; }
    }

    static void testIntegrate() {
        CollectingDownstream d = new CollectingDownstream();
        Gatherer.Integrator<Void, Integer, Integer> integ = (s, e, down) -> down.push(e * 10);
        check(integ.integrate(null, 3, d), "integrate returns true");
        check(d.collected.equals(List.of(30)), "integrate transformed and pushed");
    }

    static void testOfWraps() {
        Gatherer.Integrator<Void, Integer, Integer> original = (s, e, down) -> down.push(e);
        Gatherer.Integrator<Void, Integer, Integer> wrapped = Gatherer.Integrator.of(original);
        CollectingDownstream d = new CollectingDownstream();
        check(wrapped.integrate(null, 7, d), "of() delegates");
        check(d.collected.equals(List.of(7)), "of() result matches original");
    }

    static void testOfGreedy() {
        Gatherer.Integrator.Greedy<Void, Integer, Integer> greedy =
                Gatherer.Integrator.ofGreedy((s, e, down) -> down.push(e));
        check(greedy != null, "ofGreedy() returns non-null");
        CollectingDownstream d = new CollectingDownstream();
        check(greedy.integrate(null, 5, d), "greedy integrate runs");
        check(d.collected.equals(List.of(5)), "greedy integrate pushed");
    }

    static void testInterfaceShape() {
        check(Gatherer.Integrator.class.isInterface(), "Integrator is an interface");
        long abstractCount = java.util.Arrays.stream(Gatherer.Integrator.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                .count();
        check(abstractCount == 1, "exactly one abstract method (integrate)");
    }

    public static void main(String[] args) {
        testIntegrate();
        testOfWraps();
        testOfGreedy();
        testInterfaceShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
