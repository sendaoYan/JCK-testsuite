/*
 * @test
 * @summary java.util.function.LongConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main LongConsumerTest
 */

import java.util.function.LongConsumer;

public class LongConsumerTest {
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
        final long[] log = new long[1];
        LongConsumer inc = x -> { log[0] += x; };
        inc.accept(5L);
        check(log[0] == 5L, "accept");
        inc.accept(0);
        check(log[0] == 5L, "accept zero");
        LongConsumer dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(5L);
        check(log[0] == 5L, "andThen inc then dec unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
    }

    // ===================== 契约深化：andThen 顺序/异常中继/NPE/边界值 =====================
    static void testContracts() {
        final java.util.List<Long> a = new java.util.ArrayList<Long>();
        final java.util.List<Long> b = new java.util.ArrayList<Long>();
        LongConsumer ca = a::add, cb = b::add;
        ca.andThen(cb).accept(42L);
        check(a.size() == 1 && b.size() == 1 && a.get(0) == 42L, "andThen both accept");
        ca.andThen(cb).andThen(ca).accept(1L);
        check(a.size() == 3 && b.size() == 2, "3-stage chain counts");
        checkThrows(NullPointerException.class, () -> ca.andThen(null), "andThen(null) NPE");
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("lc");
        try {
            ((LongConsumer) v -> { throw boom; }).andThen(v -> ran[0]++).accept(1L);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        check(ran[0] == 0, "second not run");
        final java.util.List<Long> edge = new java.util.ArrayList<Long>();
        LongConsumer rec = edge::add;
        rec.accept(Long.MIN_VALUE);
        rec.accept(Long.MAX_VALUE);
        check(edge.get(0) == Long.MIN_VALUE && edge.get(1) == Long.MAX_VALUE, "edge values");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
