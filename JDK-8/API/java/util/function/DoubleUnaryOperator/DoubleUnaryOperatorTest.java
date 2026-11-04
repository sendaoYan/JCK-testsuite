/*
 * @test
 * @summary java.util.function.DoubleUnaryOperator contract: apply, compose/andThen ordering, identity, null->NPE.
 * @run main DoubleUnaryOperatorTest
 */

import java.util.function.DoubleUnaryOperator;

public class DoubleUnaryOperatorTest {
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
        DoubleUnaryOperator inc = x -> x + 1;
        check(inc.applyAsDouble(1.0) == 2, "apply");
        check(inc.andThen(x -> x * 10).applyAsDouble(1.0) == 20.0, "andThen g(f(x))");
        check(inc.compose(x -> x * 10).applyAsDouble(1.0) == 11.0, "compose f(g(x))");
        check(DoubleUnaryOperator.identity().applyAsDouble(1.0) == 1.0, "identity");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> inc.compose(null), "compose(null)");
    }

    // ===================== 契约深化：compose/andThen 顺序矩阵/identity/NPE =====================
    static void testContracts() {
        final StringBuilder order = new StringBuilder();
        DoubleUnaryOperator dbl = d -> { order.append('d'); return d * 2; };
        DoubleUnaryOperator inc = d -> { order.append('i'); return d + 1; };
        order.setLength(0);
        check(dbl.andThen(inc).applyAsDouble(3.0) == 7.0, "andThen: dbl then inc");
        check(order.toString().equals("di"), "andThen order d,i");
        order.setLength(0);
        check(dbl.compose(inc).applyAsDouble(3.0) == 8.0, "compose: inc then dbl");
        check(order.toString().equals("id"), "compose order i,d");
        check(DoubleUnaryOperator.identity().applyAsDouble(42.5) == 42.5, "identity passes through");
        check(DoubleUnaryOperator.identity().applyAsDouble(Double.NaN) != DoubleUnaryOperator.identity().applyAsDouble(Double.NaN)
              || Double.isNaN(DoubleUnaryOperator.identity().applyAsDouble(Double.NaN)), "identity NaN");
        checkThrows(NullPointerException.class, () -> dbl.andThen(null), "andThen(null) NPE");
        checkThrows(NullPointerException.class, () -> dbl.compose(null), "compose(null) NPE");
        // 三段链
        check(dbl.andThen(dbl).andThen(inc).applyAsDouble(1.0) == 5.0, "3-stage chain");
        // 自组合 n 次
        DoubleUnaryOperator twice = dbl.andThen(dbl);
        check(twice.applyAsDouble(1.5) == 6.0, "self composition");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
