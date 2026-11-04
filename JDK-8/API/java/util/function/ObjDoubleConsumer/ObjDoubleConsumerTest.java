/*
 * @test
 * @summary java.util.function.ObjDoubleConsumer contract: accept object plus primitive, side effects.
 * @run main ObjDoubleConsumerTest
 */

import java.util.function.ObjDoubleConsumer;

public class ObjDoubleConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static void testMain() {
        final StringBuilder sb = new StringBuilder();
        ObjDoubleConsumer<String> acc = (s, v) -> { sb.append(s).append(v); };
        acc.accept("v=", 7.0);
        check("v=7.0".equals(sb.toString()), "accept");
        acc.accept("!", 7.0);
        check("v=7.0!7.0".equals(sb.toString()), "accept second");
    }

    // ===================== 契约深化：双参精确捕获/null 对象/NaN =====================
    static void testContracts() {
        final java.util.List<String> log = new java.util.ArrayList<String>();
        ObjDoubleConsumer<String> c = (s, d) -> log.add(s + ":" + d);
        c.accept("a", 1.5);
        c.accept("b", Double.NaN);
        check(log.size() == 2 && log.get(0).equals("a:1.5"), "both args captured");
        check(log.get(1).endsWith("NaN"), "NaN formats");
        final boolean[] sawNull = {false};
        ObjDoubleConsumer<String> nullOk = (s, d) -> sawNull[0] = s == null;
        nullOk.accept(null, 0.0);
        check(sawNull[0], "null object arg acceptable");
        // 顺序副作用
        final StringBuilder order = new StringBuilder();
        ObjDoubleConsumer<String> rec = (s, d) -> order.append(s).append((int) d);
        rec.accept("x", 2.0);
        check(order.toString().equals("x2"), "order of side effects");
        ObjDoubleConsumer<Integer> anon = new ObjDoubleConsumer<Integer>() {
            public void accept(Integer i, double d) { order.append(i + (int) d); }
        };
        order.setLength(0);
        anon.accept(1, 2.0);
        check(order.toString().equals("3"), "anonymous impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
