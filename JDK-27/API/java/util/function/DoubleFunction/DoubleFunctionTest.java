/*
 * @test
 * @summary java.util.function.DoubleFunction contract: apply maps double to object.
 * @run main DoubleFunctionTest
 */

import java.util.function.DoubleFunction;

public class DoubleFunctionTest {
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
        DoubleFunction<String> f = Double::toString;
        check("5.0".equals(f.apply(5.0)), "apply");
        check("0.0".equals(f.apply(0.0)), "apply zero");
        check("-3.5".equals(f.apply(-3.5)), "apply negative");
        DoubleFunction<Double> half = x -> x / 2;
        check(half.apply(3.0) == 1.5, "apply unary");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
