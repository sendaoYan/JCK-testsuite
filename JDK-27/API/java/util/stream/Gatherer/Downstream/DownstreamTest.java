/*
 * @test
 * @summary java.util.stream.Gatherer.Downstream conformance: white-box structure + black-box behavior.
 * @run main DownstreamTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherer;

public class DownstreamTest {
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
        Class<?> c = Class.forName("java.util.stream.Gatherer$Downstream");
        check(c.isInterface(), "Downstream is an interface");
        boolean push = java.util.Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("push")
                        && java.lang.reflect.Modifier.isAbstract(m.getModifiers())
                        && m.getReturnType() == boolean.class);
        check(push, "abstract push(T) returning boolean declared");
        boolean rejecting = java.util.Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("isRejecting") && m.getParameterCount() == 0);
        check(rejecting, "default isRejecting() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static final class DefaultDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        public boolean push(Integer e) { collected.add(e); return true; }
    }

    static final class RejectingDownstream implements Gatherer.Downstream<Integer> {
        final List<Integer> collected = new ArrayList<>();
        boolean rejecting;
        public boolean push(Integer e) { collected.add(e); return !rejecting; }
        public boolean isRejecting() { return rejecting; }
    }

    static void testBlackBox() {
        DefaultDownstream d = new DefaultDownstream();
        check(!d.isRejecting(), "default isRejecting() is false");
        check(d.push(1), "push returns true while accepting");
        check(d.push(2), "push returns true again");
        check(d.collected.equals(List.of(1, 2)), "push delivered both elements");

        RejectingDownstream r = new RejectingDownstream();
        r.rejecting = true;
        check(r.isRejecting(), "isRejecting() reflects rejecting state");
        check(r.push(3) == false, "push returns false while rejecting");
        check(r.collected.equals(List.of(3)), "push still recorded the element");
    }


    // ===================== 契约深化：push 返回 false 语义（作为继续提示）（JDK-27 侧锁定） =====================
    static void testContracts() {
        // 自定义 Downstream：push 返回 false 表示不再继续
        final java.util.List<Integer> got = new java.util.ArrayList<Integer>();
        final boolean[] stop = {false};
        Gatherer.Downstream<Integer> controlled = new Gatherer.Downstream<Integer>() {
            public boolean push(Integer e) {
                if (stop[0]) return false;
                got.add(e);
                return true;
            }
        };
        check(controlled.push(1), "push true while open");
        stop[0] = true;
        check(!controlled.push(2), "push false when closed");
        check(got.equals(java.util.Arrays.asList(1)), "closed push not recorded");
        // gatherer 集成：integrator 转发 push 结果 -> false 短路上游
        Gatherer<Integer, Void, Integer> relay =
                Gatherer.of((Void s, Integer e, Gatherer.Downstream<? super Integer> d) -> d.push(e));
        check(java.util.stream.Stream.of(1, 2, 3).limit(2).gather(relay).toList()
              .equals(java.util.Arrays.asList(1, 2)), "relay gatherer end-to-end");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
