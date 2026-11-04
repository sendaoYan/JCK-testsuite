/*
 * @test
 * @summary java.util.stream.Gatherer.Integrator conformance: white-box structure + black-box behavior.
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
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Gatherer$Integrator");
        check(c.isInterface(), "Integrator is an interface");
        boolean integ = java.util.Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("integrate")
                        && java.lang.reflect.Modifier.isAbstract(m.getModifiers())
                        && m.getParameterCount() == 3);
        check(integ, "abstract integrate(A, T, Downstream) declared");
        check(java.util.Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("of")),
                "static of() declared");
        check(java.util.Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("ofGreedy")),
                "static ofGreedy() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static final class CollectingDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        public boolean push(Integer e) { collected.add(e); return true; }
    }

    static void testBlackBox() {
        CollectingDownstream d = new CollectingDownstream();
        Gatherer.Integrator<Void, Integer, Integer> integ = (s, e, down) -> down.push(e * 10);
        check(integ.integrate(null, 3, d), "integrate returns true");
        check(d.collected.equals(List.of(30)), "integrate transformed and pushed");

        Gatherer.Integrator<Void, Integer, Integer> original = (s, e, down) -> down.push(e);
        Gatherer.Integrator<Void, Integer, Integer> wrapped = Gatherer.Integrator.of(original);
        CollectingDownstream d2 = new CollectingDownstream();
        check(wrapped.integrate(null, 7, d2), "of() delegates");
        check(d2.collected.equals(List.of(7)), "of() result matches original");

        Gatherer.Integrator.Greedy<Void, Integer, Integer> greedy =
                Gatherer.Integrator.ofGreedy((s, e, down) -> down.push(e));
        check(greedy != null, "ofGreedy() returns non-null");
        CollectingDownstream d3 = new CollectingDownstream();
        check(greedy.integrate(null, 5, d3), "greedy integrate runs");
        check(d3.collected.equals(List.of(5)), "greedy integrate pushed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
