/*
 * @test
 * @summary java.util.function.DoubleBinaryOperator contract: apply operates on two primitives.
 * @run main DoubleBinaryOperatorTest
 */

import java.util.function.DoubleBinaryOperator;

public class DoubleBinaryOperatorTest {
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
        DoubleBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsDouble(2.0, 3) == 5, "apply");
        check(add.applyAsDouble(0, 0) == 0, "apply zero");
        check(add.applyAsDouble(-1, 1) == 0, "apply inverse");
    }

    // ===================== 契约深化：精确算术/NaN/Inf/方法引用/溢出 =====================
    static void testContracts() {
        DoubleBinaryOperator add = (x, y) -> x + y;
        check(add.applyAsDouble(0.1, 0.2) == 0.30000000000000004, "IEEE754 exact sum");
        check(add.applyAsDouble(1, Double.NaN) != add.applyAsDouble(1, Double.NaN), "NaN propagates and != itself");
        check(Double.isInfinite(add.applyAsDouble(1, Double.POSITIVE_INFINITY)), "Inf propagates");
        DoubleBinaryOperator max = Math::max;
        check(max.applyAsDouble(3.5, -2.5) == 3.5, "Math::max method ref");
        check(Double.isNaN(max.applyAsDouble(Double.NaN, 5.0)), "Math.max propagates NaN");
        DoubleBinaryOperator wrap = (x, y) -> x * y;
        check(wrap.applyAsDouble(1e308, 10) == Double.POSITIVE_INFINITY, "overflow to Inf");
        // 匿名类
        DoubleBinaryOperator anon = new DoubleBinaryOperator() {
            public double applyAsDouble(double l, double r) { return l - r; }
        };
        check(anon.applyAsDouble(1.5, 0.25) == 1.25, "anonymous impl");
        // 有状态（累计器）
        final double[] acc = {0};
        DoubleBinaryOperator into = (x, y) -> acc[0] = x + y;
        into.applyAsDouble(2, 3);
        check(acc[0] == 5.0, "side-effect captures both args");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
