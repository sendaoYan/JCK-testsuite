/*
 * @test
 * @summary java.util.function.DoubleToLongFunction contract: apply converts primitive to primitive.
 * @run main DoubleToLongFunctionTest
 */

import java.util.function.DoubleToLongFunction;

public class DoubleToLongFunctionTest {
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
        DoubleToLongFunction c = x -> (long) x;
        check(c.applyAsLong(2.9) == 2, "apply");
        check(c.applyAsLong(0) == 0, "apply zero");
    }

    // ===================== 契约深化：double->long 窄化转换语义矩阵 =====================
    static void testContracts() {
        DoubleToLongFunction f = d -> (long) d;
        check(f.applyAsLong(1e30) == Long.MAX_VALUE, "huge saturates Long.MAX");
        check(f.applyAsLong(-1e30) == Long.MIN_VALUE, "huge negative saturates Long.MIN");
        check(f.applyAsLong(Double.NaN) == 0L, "NaN -> 0");
        check(f.applyAsLong(2.999) == 2, "truncates");
        check(f.applyAsLong(-2.999) == -2, "negative truncates toward zero");
        check(f.applyAsLong(12345678901.0) == 12345678901L, "exact large value");
        // 有损往返：long->double->long 丢失低位
        long big = 9007199254740993L; // 2^53+1
        DoubleToLongFunction lossy = d -> (long) (double) d;
        check(lossy.applyAsLong((double) big) == 9007199254740992L, "2^53+1 loses bit through double");
        check(f.applyAsLong(0.5) == 0, "fractional to 0");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
