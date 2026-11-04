/*
 * @test
 * @summary java.util.function.LongFunction contract: apply maps long to object.
 * @run main LongFunctionTest
 */

import java.util.function.LongFunction;

public class LongFunctionTest {
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
        LongFunction<String> f = Long::toString;
        check("5".equals(f.apply(5L)), "apply");
        check("0".equals(f.apply(0L)), "apply zero");
        check("-3".equals(f.apply(-3L)), "apply negative");
        LongFunction<Long> neg = x -> -x;
        check(neg.apply(3L) == -3L, "apply unary");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
