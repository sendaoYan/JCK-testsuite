/*
 * @test
 * @summary java.util.function.IntFunction contract: apply maps int to object.
 * @run main IntFunctionTest
 */

import java.util.function.IntFunction;

public class IntFunctionTest {
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
        IntFunction<String> f = Integer::toString;
        check("5".equals(f.apply(5)), "apply");
        check("0".equals(f.apply(0)), "apply zero");
        check("-3".equals(f.apply(-3)), "apply negative");
        IntFunction<int[]> arr = int[]::new;
        check(arr.apply(3).length == 3, "apply new array");
        checkThrows(NegativeArraySizeException.class, () -> arr.apply(-1), "array negative length");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
