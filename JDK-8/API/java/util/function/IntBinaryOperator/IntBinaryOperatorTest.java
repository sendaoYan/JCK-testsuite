/*
 * @test
 * @summary java.util.function.IntBinaryOperator contract: apply operates on two primitives.
 * @run main IntBinaryOperatorTest
 */

import java.util.function.IntBinaryOperator;

public class IntBinaryOperatorTest {
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
        IntBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsInt(2, 3) == 5, "apply");
        check(add.applyAsInt(0, 0) == 0, "apply zero");
        check(add.applyAsInt(-1, 1) == 0, "apply inverse");
    }

    // ===================== 契约深化：精确整数运算/溢出回绕/方法引用 =====================
    static void testContracts() {
        IntBinaryOperator add = (x, y) -> x + y;
        check(add.applyAsInt(2, 3) == 5, "exact add");
        check(add.applyAsInt(Integer.MAX_VALUE, 1) == Integer.MIN_VALUE, "overflow wraps");
        IntBinaryOperator sub = (x, y) -> x - y;
        check(sub.applyAsInt(Integer.MIN_VALUE, 1) == Integer.MAX_VALUE, "underflow wraps");
        IntBinaryOperator mul = (x, y) -> x * y;
        check(mul.applyAsInt(65536, 65536) == 0, "mul wraps to 0");
        IntBinaryOperator max = Math::max;
        check(max.applyAsInt(-1, 1) == 1, "Math::max ref");
        IntBinaryOperator min = Integer::min;
        check(min.applyAsInt(-1, 1) == -1, "static method ref");
        // 有状态累计
        final int[] acc = {100};
        IntBinaryOperator fold = (x, y) -> acc[0] = acc[0] - x * y;
        fold.applyAsInt(2, 5);
        fold.applyAsInt(1, 1);
        check(acc[0] == 89, "side-effecting fold exact");
        IntBinaryOperator anon = new IntBinaryOperator() {
            public int applyAsInt(int l, int r) { return l / r; }
        };
        check(anon.applyAsInt(7, 2) == 3, "anonymous impl truncating division");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
