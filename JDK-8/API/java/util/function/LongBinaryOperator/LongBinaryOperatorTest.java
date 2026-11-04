/*
 * @test
 * @summary java.util.function.LongBinaryOperator contract: apply operates on two primitives.
 * @run main LongBinaryOperatorTest
 */

import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorTest {
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
        LongBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsLong(2L, 3) == 5, "apply");
        check(add.applyAsLong(0, 0) == 0, "apply zero");
        check(add.applyAsLong(-1, 1) == 0, "apply inverse");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
