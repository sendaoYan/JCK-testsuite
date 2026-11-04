/*
 * @test
 * @summary java.util.function.IntConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main IntConsumerTest
 */

import java.util.function.IntConsumer;

public class IntConsumerTest {
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
        final int[] log = new int[1];
        IntConsumer inc = x -> { log[0] += x; };
        inc.accept(5);
        check(log[0] == 5, "accept");
        inc.accept(0);
        check(log[0] == 5, "accept zero");
        IntConsumer dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(5);
        check(log[0] == 5, "andThen inc then dec unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
    }

    // ===================== 契约深化：andThen 顺序/异常中继/NPE =====================
    static void testContracts() {
        final java.util.List<Integer> a = new java.util.ArrayList<Integer>();
        final java.util.List<Integer> b = new java.util.ArrayList<Integer>();
        IntConsumer ca = a::add, cb = b::add;
        ca.andThen(cb).accept(7);
        check(a.size() == 1 && b.size() == 1 && a.get(0) == 7 && b.get(0) == 7,
              "andThen both accept same value");
        ca.andThen(cb).andThen(ca).accept(8);
        check(a.size() == 3 && b.size() == 2, "3-stage chain counts");
        checkThrows(NullPointerException.class, () -> ca.andThen(null), "andThen(null) NPE");
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("ic");
        try {
            ((IntConsumer) v -> { throw boom; }).andThen(v -> ran[0]++).accept(1);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        check(ran[0] == 0, "second not run after first throws");
        // 边界值入参
        final java.util.List<Integer> edge = new java.util.ArrayList<Integer>();
        IntConsumer rec = edge::add;
        rec.accept(Integer.MIN_VALUE);
        rec.accept(Integer.MAX_VALUE);
        check(edge.get(0) == Integer.MIN_VALUE && edge.get(1) == Integer.MAX_VALUE, "edge values pass");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
