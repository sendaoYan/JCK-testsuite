/*
 * @test
 * @summary java.util.stream.Gatherer.Integrator.Greedy conformance: white-box structure + black-box behavior.
 * @run main GreedyTest
 */

import java.util.stream.Gatherer;

public class GreedyTest {
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
        Class<?> c = Class.forName("java.util.stream.Gatherer$Integrator$Greedy");
        check(c.isInterface(), "Greedy is an interface");
        check(Gatherer.Integrator.class.isAssignableFrom(c), "Greedy extends Integrator");
        check(c.getGenericInterfaces().length == 1, "Greedy has exactly one direct superinterface");
        long abstractCount = java.util.Arrays.stream(c.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers())).count();
        check(abstractCount == 0, "Greedy declares no abstract methods of its own");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Gatherer.Integrator<Void, Integer, Integer> plain =
                Gatherer.Integrator.of((s, e, d) -> d.push(e));
        Gatherer.Integrator.Greedy<Void, Integer, Integer> greedy =
                Gatherer.Integrator.ofGreedy((s, e, d) -> d.push(e));

        check(greedy instanceof Gatherer.Integrator.Greedy, "ofGreedy yields Greedy");
        check(!(plain instanceof Gatherer.Integrator.Greedy), "of yields non-greedy Integrator");
        check(greedy instanceof Gatherer.Integrator, "Greedy is also an Integrator");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
