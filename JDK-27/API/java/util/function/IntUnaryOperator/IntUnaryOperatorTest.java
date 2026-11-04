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
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
