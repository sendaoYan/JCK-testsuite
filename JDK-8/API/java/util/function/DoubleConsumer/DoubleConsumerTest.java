/*
 * @test
 * @summary java.util.function.DoubleConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main DoubleConsumerTest
 */

import java.util.function.DoubleConsumer;

public class DoubleConsumerTest {
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
        final double[] log = new double[1];
        DoubleConsumer inc = x -> { log[0] += x; };
        inc.accept(5.0);
        check(log[0] == 5.0, "accept");
        inc.accept(0);
        check(log[0] == 5.0, "accept zero");
        DoubleConsumer dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(5.0);
        check(log[0] == 5.0, "andThen inc then dec unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
    }

    // ===================== 契约深化：andThen 顺序/异常中继/NPE/NaN 元素 =====================
    static void testContracts() {
        final java.util.List<Double> seen = new java.util.ArrayList<Double>();
        DoubleConsumer a = seen::add;
        final java.util.List<Double> seen2 = new java.util.ArrayList<Double>();
        DoubleConsumer b = seen2::add;
        a.andThen(b).accept(1.5);
        check(seen.size() == 1 && seen2.size() == 1 && seen2.get(0) == 1.5, "andThen both consume same value");
        checkThrows(NullPointerException.class, () -> a.andThen(null), "andThen(null) NPE");
        final int[] ran = {0};
        RuntimeException boom = new RuntimeException("dc");
        try {
            ((DoubleConsumer) v -> { throw boom; }).andThen(v -> ran[0]++).accept(2.5);
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed");
        }
        check(ran[0] == 0, "second not run after first throws");
        final boolean[] sawNaN = {false};
        DoubleConsumer nanOk = v -> sawNaN[0] = Double.isNaN(v);
        nanOk.accept(Double.NaN);
        check(sawNaN[0], "NaN value acceptable");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
