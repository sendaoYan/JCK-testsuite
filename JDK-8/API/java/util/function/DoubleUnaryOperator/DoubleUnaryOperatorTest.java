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
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
