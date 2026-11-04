/*
 * @test
 * @summary java.util.function.LongToDoubleFunction contract: apply converts primitive to primitive.
 * @run main LongToDoubleFunctionTest
 */

import java.util.function.LongToDoubleFunction;

public class LongToDoubleFunctionTest {
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
        LongToDoubleFunction c = x -> (double) x;
        check(c.applyAsDouble(2) == 2.0, "apply");
        check(c.applyAsDouble(0) == 0, "apply zero");
    }

    // ===================== 契约深化：long->double 有损宽化矩阵 =====================
    static void testContracts() {
        LongToDoubleFunction f = n -> (double) n;
        check(f.applyAsDouble(0L) == 0.0, "zero");
        check(f.applyAsDouble(Long.MAX_VALUE) == 9.223372036854776E18, "MAX rounds");
        check(f.applyAsDouble(1L << 52) == 4503599627370496.0, "2^52 exact");
        check(f.applyAsDouble((1L << 53) + 1) == 9007199254740992.0, "2^53+1 loses bit");
        LongToDoubleFunction frac = n -> n / 4.0;
        check(frac.applyAsDouble(5L) == 1.25, "division exact");
        LongToDoubleFunction mr = n -> Long.valueOf(n).doubleValue();
        check(mr.applyAsDouble(-7L) == -7.0, "method ref");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
