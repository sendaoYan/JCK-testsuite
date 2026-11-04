/*
 * @test
 * @summary java.util.function.DoubleBinaryOperator contract: apply operates on two primitives.
 * @run main DoubleBinaryOperatorTest
 */

import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorTest {
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
        DoubleBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsDouble(2.0, 3) == 5, "apply");
        check(add.applyAsDouble(0, 0) == 0, "apply zero");
        check(add.applyAsDouble(-1, 1) == 0, "apply inverse");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
