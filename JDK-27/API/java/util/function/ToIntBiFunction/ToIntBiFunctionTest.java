/*
 * @test
 * @summary java.util.function.ToIntBiFunction contract: apply extracts primitive from two objects.
 * @run main ToIntBiFunctionTest
 */

import java.util.function.ToIntBiFunction;

public class ToIntBiFunctionTest {
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
        ToIntBiFunction<String, String> cmp = (a, b) -> a.compareTo(b);
        check(cmp.applyAsInt("a", "b") < 0, "apply");
        check(cmp.applyAsInt("b", "a") > 0, "apply reversed");
        check(cmp.applyAsInt("a", "a") == 0, "apply equal");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
