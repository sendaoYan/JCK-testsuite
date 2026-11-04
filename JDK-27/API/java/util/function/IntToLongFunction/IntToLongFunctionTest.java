/*
 * @test
 * @summary java.util.function.IntToLongFunction contract: apply converts primitive to primitive.
 * @run main IntToLongFunctionTest
 */

import java.util.function.IntToLongFunction;

public class IntToLongFunctionTest {
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
        IntToLongFunction c = x -> (long) x;
        check(c.applyAsLong(2) == 2, "apply");
        check(c.applyAsLong(0) == 0, "apply zero");
    }

    // ===================== 契约深化：int->long 宽化无损矩阵 =====================
    static void testContracts() {
        IntToLongFunction f = n -> (long) n;
        check(f.applyAsLong(0) == 0L, "zero");
        check(f.applyAsLong(Integer.MAX_VALUE) == 2147483647L, "MAX");
        check(f.applyAsLong(Integer.MIN_VALUE) == -2147483648L, "MIN");
        IntToLongFunction summed = n -> (long) n + Integer.MAX_VALUE;
        check(summed.applyAsLong(1) == 2147483648L, "no overflow in long domain");
        IntToLongFunction mr = Integer::toUnsignedLong;
        check(mr.applyAsLong(-1) == 4294967295L, "toUnsignedLong method ref");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
