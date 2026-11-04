/*
 * @test
 * @summary java.util.function.BooleanSupplier contract: getAsBoolean evaluates.
 * @run main BooleanSupplierTest
 */

import java.util.function.BooleanSupplier;

public class BooleanSupplierTest {
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
        BooleanSupplier t = () -> true;
        check(t.getAsBoolean(), "getAsBoolean true");
        BooleanSupplier f = () -> false;
        check(!f.getAsBoolean(), "getAsBoolean false");
        final boolean[] flip = new boolean[1];
        BooleanSupplier toggle = () -> { flip[0] = !flip[0]; return flip[0]; };
        check(toggle.getAsBoolean(), "toggle 1 true");
        check(!toggle.getAsBoolean(), "toggle 2 false");
        check(toggle.getAsBoolean(), "toggle 3 true");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
