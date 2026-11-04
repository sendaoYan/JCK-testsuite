/*
 * @test
 * @summary java.util.function.LongBinaryOperator contract: apply operates on two primitives.
 * @run main LongBinaryOperatorTest
 */

import java.util.function.LongBinaryOperator;

public class LongBinaryOperatorTest {
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
        LongBinaryOperator add = (a, b) -> a + b;
        check(add.applyAsLong(2L, 3) == 5, "apply");
        check(add.applyAsLong(0, 0) == 0, "apply zero");
        check(add.applyAsLong(-1, 1) == 0, "apply inverse");
    }

    // ===================== 契约深化：精确 long 运算/溢出回绕/方法引用 =====================
    static void testContracts() {
        LongBinaryOperator add = (x, y) -> x + y;
        check(add.applyAsLong(2, 3) == 5, "exact add");
        check(add.applyAsLong(Long.MAX_VALUE, 1) == Long.MIN_VALUE, "overflow wraps");
        LongBinaryOperator sub = (x, y) -> x - y;
        check(sub.applyAsLong(Long.MIN_VALUE, 1) == Long.MAX_VALUE, "underflow wraps");
        LongBinaryOperator max = Math::max;
        check(max.applyAsLong(-1L, 1L) == 1L, "Math::max ref");
        LongBinaryOperator xor = (x, y) -> x ^ y;
        check(xor.applyAsLong(0xFF00L, 0x0FF0L) == 0xF0F0L, "xor exact");
        LongBinaryOperator anon = new LongBinaryOperator() {
            public long applyAsLong(long l, long r) { return l % r; }
        };
        check(anon.applyAsLong(-7L, 3L) == -1, "anonymous remainder sign follows dividend");
        check(anon.applyAsLong(7L, -3L) == 1, "remainder sign follows dividend positive");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
