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
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
