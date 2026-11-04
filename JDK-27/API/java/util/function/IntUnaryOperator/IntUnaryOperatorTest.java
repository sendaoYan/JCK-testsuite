/*
 * @test
 * @summary java.util.function.IntUnaryOperator contract: apply, compose/andThen ordering, identity, null->NPE.
 * @run main IntUnaryOperatorTest
 */

import java.util.function.IntUnaryOperator;

public class IntUnaryOperatorTest {
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
        IntUnaryOperator inc = x -> x + 1;
        check(inc.applyAsInt(1) == 2, "apply");
        check(inc.andThen(x -> x * 10).applyAsInt(1) == 20, "andThen g(f(x))");
        check(inc.compose(x -> x * 10).applyAsInt(1) == 11, "compose f(g(x))");
        check(IntUnaryOperator.identity().applyAsInt(1) == 1, "identity");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> inc.compose(null), "compose(null)");
    }

    // ===================== 契约深化：compose/andThen 顺序矩阵/identity/NPE =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        IntUnaryOperator dbl = n -> { order.append('d'); return n * 2; };
        IntUnaryOperator inc = n -> { order.append('i'); return n + 1; };
        order.setLength(0);
        check(dbl.andThen(inc).applyAsInt(3) == 7 && order.toString().equals("di"), "andThen d,i");
        order.setLength(0);
        check(dbl.compose(inc).applyAsInt(3) == 8 && order.toString().equals("id"), "compose i,d");
        check(IntUnaryOperator.identity().applyAsInt(-9) == -9, "identity passthrough");
        check(IntUnaryOperator.identity().applyAsInt(Integer.MAX_VALUE) == Integer.MAX_VALUE, "identity MAX");
        checkThrows(NullPointerException.class, () -> dbl.andThen(null), "andThen(null) NPE");
        checkThrows(NullPointerException.class, () -> dbl.compose(null), "compose(null) NPE");
        check(dbl.andThen(dbl).andThen(dbl).applyAsInt(1) == 8, "triple chain");
        IntUnaryOperator wrap = n -> n + 1;
        check(wrap.applyAsInt(Integer.MAX_VALUE) == Integer.MIN_VALUE, "overflow wraps in operator");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
