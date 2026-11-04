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


    // ===================== 契约深化：andThen 组合顺序/null NPE/两个 greedy（JDK-27 侧锁定） =====================
    static void testContracts() {
        // 手工组合：同一元素先经 first 再经 second（Integrator 无组合方法，语义自管）
        CollectingDownstream d = new CollectingDownstream();
        Gatherer.Integrator<Void, Integer, Integer> first =
                (s, e, down) -> down.push(e * 2);
        Gatherer.Integrator<Void, Integer, Integer> second =
                (s, e, down) -> down.push(e + 100);
        Gatherer.Integrator<Void, Integer, Integer> chained =
                (s, e, down) -> first.integrate(s, e, down) && second.integrate(s, e, down);
        check(chained.integrate(null, 3, d), "manual chain integrates");
        check(d.collected.equals(List.of(6, 103)), "manual chain runs first then second (6,103): " + d.collected);
        // 第一个返回 false 则第二个不再执行（短路语义）
        CollectingDownstream d2 = new CollectingDownstream();
        Gatherer.Integrator<Void, Integer, Integer> shortCircuit =
                (s, e, down) -> false;
        Gatherer.Integrator<Void, Integer, Integer> after =
                (s, e, down) -> down.push(-1);
        Gatherer.Integrator<Void, Integer, Integer> cut =
                (s, e, down) -> shortCircuit.integrate(s, e, down) && after.integrate(s, e, down);
        check(!cut.integrate(null, 5, d2), "false from first stops chain");
        check(d2.collected.isEmpty(), "second integrator not invoked after false");
        // of()/ofGreedy() 工厂不急切校验 null（惰性包装，锁定无 NPE）
        check(Gatherer.Integrator.of(null) == null, "of(null) lazily returns null (no eager validation)");
        check(Gatherer.Integrator.ofGreedy(null) == null, "ofGreedy(null) lazily returns null");
        // Greedy.ofGreedy 返回可作 Integrator 使用
        Gatherer.Integrator.Greedy<Void, Integer, Integer> greedy =
                Gatherer.Integrator.ofGreedy((s, e, down) -> down.push(e));
        CollectingDownstream d3 = new CollectingDownstream();
        check(greedy.integrate(null, 9, d3) && d3.collected.equals(List.of(9)),
              "Greedy usable as Integrator");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
