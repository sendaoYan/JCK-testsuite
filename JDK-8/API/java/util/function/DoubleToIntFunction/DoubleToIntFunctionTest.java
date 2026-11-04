/*
 * @test
 * @summary java.util.function.DoubleToIntFunction contract: apply converts primitive to primitive.
 * @run main DoubleToIntFunctionTest
 */

import java.util.function.DoubleToIntFunction;

public class DoubleToIntFunctionTest {
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
        DoubleToIntFunction c = x -> (int) x;
        check(c.applyAsInt(2.9) == 2, "apply");
        check(c.applyAsInt(0) == 0, "apply zero");
    }

    // ===================== 契约深化：窄化转换语义（cast）精确矩阵 =====================
    static void testContracts() {
        DoubleToIntFunction f = d -> (int) d;
        check(f.applyAsInt(3.99) == 3, "truncates toward zero");
        check(f.applyAsInt(-3.99) == -3, "negative truncates toward zero");
        check(f.applyAsInt(Double.NaN) == 0, "NaN -> 0");
        check(f.applyAsInt(Double.POSITIVE_INFINITY) == Integer.MAX_VALUE, "+Inf saturates MAX");
        check(f.applyAsInt(Double.NEGATIVE_INFINITY) == Integer.MIN_VALUE, "-Inf saturates MIN");
        check(f.applyAsInt(1e100) == Integer.MAX_VALUE, "huge saturates MAX");
        check(f.applyAsInt(-1e100) == Integer.MIN_VALUE, "huge negative saturates MIN");
        check(f.applyAsInt(2147483647.0) == Integer.MAX_VALUE, "exact MAX boundary");
        // 显式舍入实现（对比 cast 语义）
        DoubleToIntFunction round = d -> (int) Math.round(d);
        check(round.applyAsInt(2.5) == 3 && f.applyAsInt(2.5) == 2, "round vs cast differ at 2.5");
        DoubleToIntFunction viaMath = v -> (int) v;
        check(viaMath.applyAsInt(0.0) == 0 && viaMath.applyAsInt(-0.9) == 0, "small magnitudes to 0");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
