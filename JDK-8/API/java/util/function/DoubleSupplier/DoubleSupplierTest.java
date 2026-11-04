/*
 * @test
 * @summary java.util.function.DoubleSupplier contract: lazily evaluates and returns primitive.
 * @run main DoubleSupplierTest
 */

import java.util.function.DoubleSupplier;

public class DoubleSupplierTest {
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
        DoubleSupplier sup = () -> 5.0;
        check(sup.getAsDouble() == 5, "get");
        DoubleSupplier counter = new DoubleSupplier() {
            double i = 0;
            public double getAsDouble() { return ++i; }
        };
        check(counter.getAsDouble() == 1, "get 1");
        check(counter.getAsDouble() == 2, "get 2");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
