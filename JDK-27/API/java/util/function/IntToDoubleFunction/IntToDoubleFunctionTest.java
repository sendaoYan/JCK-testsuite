/*
 * @test
 * @summary java.util.function.IntToDoubleFunction contract: apply converts primitive to primitive.
 * @run main IntToDoubleFunctionTest
 */

import java.util.function.IntToDoubleFunction;

public class IntToDoubleFunctionTest {
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
        IntToDoubleFunction c = x -> (double) x;
        check(c.applyAsDouble(2) == 2.0, "apply");
        check(c.applyAsDouble(0) == 0, "apply zero");
    }

    // ===================== 契约深化：精确宽化矩阵（int 完整可表示） =====================
    static void testContracts() {
        IntToDoubleFunction f = n -> (double) n;
        check(f.applyAsDouble(0) == 0.0, "zero");
        check(f.applyAsDouble(1) == 1.0, "one");
        check(f.applyAsDouble(Integer.MAX_VALUE) == 2147483647.0, "MAX exact");
        check(f.applyAsDouble(Integer.MIN_VALUE) == -2147483648.0, "MIN exact");
        check(1.0 / f.applyAsDouble(3) == 0.3333333333333333, "exact binary fraction");
        IntToDoubleFunction scaled = n -> n / 2.0;
        check(scaled.applyAsDouble(5) == 2.5, "half exact");
        IntToDoubleFunction negZero = n -> -0.0 * n;
        check(negZero.applyAsDouble(3) == 0.0 && 1 / negZero.applyAsDouble(3) == Double.NEGATIVE_INFINITY,
              "negative zero semantics");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
