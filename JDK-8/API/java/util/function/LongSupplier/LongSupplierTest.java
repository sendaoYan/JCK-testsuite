/*
 * @test
 * @summary java.util.function.LongSupplier contract: lazily evaluates and returns primitive.
 * @run main LongSupplierTest
 */

import java.util.function.LongSupplier;

public class LongSupplierTest {
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
        LongSupplier sup = () -> 5L;
        check(sup.getAsLong() == 5, "get");
        LongSupplier counter = new LongSupplier() {
            long i = 0;
            public long getAsLong() { return ++i; }
        };
        check(counter.getAsLong() == 1, "get 1");
        check(counter.getAsLong() == 2, "get 2");
    }

    // ===================== 契约深化：精确值/有状态序列/边界值/异常透传 =====================
    static void testContracts() {
        LongSupplier v = () -> 42L;
        check(v.getAsLong() == 42L, "exact constant");
        final long[] i = {0};
        LongSupplier counter = () -> ++i[0];
        check(counter.getAsLong() == 1 && counter.getAsLong() == 2, "stateful");
        LongSupplier max = () -> Long.MAX_VALUE;
        check(max.getAsLong() == Long.MAX_VALUE, "edge constant");
        Long boxed = Long.valueOf(123456789012345L);
        LongSupplier mr = boxed::longValue;
        check(mr.getAsLong() == 123456789012345L, "boxed method ref");
        RuntimeException boom = new RuntimeException("ls");
        try {
            LongSupplier thrower = () -> { throw boom; };
            thrower.getAsLong();
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "no wrapping");
        }
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
