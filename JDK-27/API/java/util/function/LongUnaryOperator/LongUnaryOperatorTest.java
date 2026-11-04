/*
 * @test
 * @summary java.util.function.LongUnaryOperator contract: apply, compose/andThen ordering, identity, null->NPE.
 * @run main LongUnaryOperatorTest
 */

import java.util.function.LongUnaryOperator;

public class LongUnaryOperatorTest {
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
        LongUnaryOperator inc = x -> x + 1;
        check(inc.applyAsLong(1L) == 2, "apply");
        check(inc.andThen(x -> x * 10).applyAsLong(1L) == 20L, "andThen g(f(x))");
        check(inc.compose(x -> x * 10).applyAsLong(1L) == 11L, "compose f(g(x))");
        check(LongUnaryOperator.identity().applyAsLong(1L) == 1L, "identity");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> inc.compose(null), "compose(null)");
    }

    // ===================== 契约深化：compose/andThen 顺序矩阵/identity/NPE =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        LongUnaryOperator dbl = n -> { order.append('d'); return n * 2; };
        LongUnaryOperator inc = n -> { order.append('i'); return n + 1; };
        order.setLength(0);
        check(dbl.andThen(inc).applyAsLong(3L) == 7L && order.toString().equals("di"), "andThen d,i");
        order.setLength(0);
        check(dbl.compose(inc).applyAsLong(3L) == 8L && order.toString().equals("id"), "compose i,d");
        check(LongUnaryOperator.identity().applyAsLong(-11L) == -11L, "identity passthrough");
        checkThrows(NullPointerException.class, () -> dbl.andThen(null), "andThen(null) NPE");
        checkThrows(NullPointerException.class, () -> dbl.compose(null), "compose(null) NPE");
        check(dbl.applyAsLong(Long.MAX_VALUE / 2 + 1) < 0
              && dbl.andThen(dbl).applyAsLong(Long.MAX_VALUE / 2 + 1) == 0L, "overflow wraps in chain");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
