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


    // ===================== 契约深化：Greedy 语义（总是继续）/静态工厂（JDK-27 侧锁定） =====================
    static void testContracts() {
        java.util.List<Integer> sink = new java.util.ArrayList<Integer>();
        Gatherer.Integrator.Greedy<Void, Integer, Integer> g =
                Gatherer.Integrator.ofGreedy((s, e, down) -> down.push(e));
        check(g.integrate(null, 1, e -> { sink.add(e); return true; }), "greedy integrate true");
        check(g.integrate(null, 2, e -> { sink.add(e); return true; }), "greedy continues after first");
        check(sink.equals(java.util.Arrays.asList(1, 2)), "greedy pushed both");
        // 在 gatherer 中使用：总是消费全部输入
        Gatherer<Integer, Void, Integer> all =
                Gatherer.of(Gatherer.Integrator.ofGreedy(
                        (Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e)));
        check(java.util.stream.Stream.of(1, 2, 3).gather(all).toList()
              .equals(java.util.Arrays.asList(1, 2, 3)), "greedy-driven gatherer consumes all");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
