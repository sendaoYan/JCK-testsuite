/*
 * @test
 * @summary java.util.function.DoubleToLongFunction contract: apply converts primitive to primitive.
 * @run main DoubleToLongFunctionTest
 */

import java.util.function.DoubleToLongFunction;

public class DoubleToLongFunctionTest {
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
        DoubleToLongFunction c = x -> (long) x;
        check(c.applyAsLong(2.9) == 2, "apply");
        check(c.applyAsLong(0) == 0, "apply zero");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
