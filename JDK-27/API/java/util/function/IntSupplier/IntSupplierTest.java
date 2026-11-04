/*
 * @test
 * @summary java.util.function.IntSupplier contract: lazily evaluates and returns primitive.
 * @run main IntSupplierTest
 */

import java.util.function.IntSupplier;

public class IntSupplierTest {
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
        IntSupplier sup = () -> 5;
        check(sup.getAsInt() == 5, "get");
        IntSupplier counter = new IntSupplier() {
            int i = 0;
            public int getAsInt() { return ++i; }
        };
        check(counter.getAsInt() == 1, "get 1");
        check(counter.getAsInt() == 2, "get 2");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
