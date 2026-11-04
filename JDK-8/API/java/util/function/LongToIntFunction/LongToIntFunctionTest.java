/*
 * @test
 * @summary java.util.function.LongToIntFunction contract: apply converts primitive to primitive.
 * @run main LongToIntFunctionTest
 */

import java.util.function.LongToIntFunction;

public class LongToIntFunctionTest {
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
        LongToIntFunction c = x -> (int) x;
        check(c.applyAsInt(2) == 2, "apply");
        check(c.applyAsInt(0) == 0, "apply zero");
    }

    // ===================== 契约深化：long->int 窄化（低 32 位）语义矩阵 =====================
    static void testContracts() {
        LongToIntFunction f = n -> (int) n;
        check(f.applyAsInt(5L) == 5, "small exact");
        check(f.applyAsInt(0xFFFFFFFFL) == -1, "low 32 bits sign-extended");
        check(f.applyAsInt(0x7FFFFFFFL) == Integer.MAX_VALUE, "MAX passthrough");
        check(f.applyAsInt(0x80000000L) == Integer.MIN_VALUE, "bit 31 becomes sign");
        check(f.applyAsInt(0x100000000L) == 0, "high bits dropped");
        check(f.applyAsInt(0x1_0000_0002L) == 2, "only low 32 kept");
        check(f.applyAsInt(-1L) == -1, "negative long -> -1");
        LongToIntFunction mr = n -> (int) n;
        check(mr.applyAsInt(Long.MAX_VALUE) == -1, "MAX_VALUE low bits all 1");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
