/*
 * @test
 * @summary java.util.function.IntSupplier contract: lazily evaluates and returns primitive.
 * @run main IntSupplierTest
 */

import java.util.function.IntSupplier;

public class IntSupplierTest {
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
        IntSupplier sup = () -> 5;
        check(sup.getAsInt() == 5, "get");
        IntSupplier counter = new IntSupplier() {
            int i = 0;
            public int getAsInt() { return ++i; }
        };
        check(counter.getAsInt() == 1, "get 1");
        check(counter.getAsInt() == 2, "get 2");
    }

    // ===================== 契约深化：精确值/有状态序列/边界值/异常透传 =====================
    static void testContracts() {
        IntSupplier seven = () -> 7;
        check(seven.getAsInt() == 7, "exact constant");
        final int[] i = {0};
        IntSupplier counter = () -> ++i[0];
        check(counter.getAsInt() == 1 && counter.getAsInt() == 2, "stateful sequence");
        IntSupplier min = () -> Integer.MIN_VALUE;
        IntSupplier max = () -> Integer.MAX_VALUE;
        check(min.getAsInt() == Integer.MIN_VALUE && max.getAsInt() == Integer.MAX_VALUE, "edge constants");
        IntSupplier mr = "abc"::length;
        check(mr.getAsInt() == 3, "method reference supplier");
        RuntimeException boom = new RuntimeException("is");
        try {
            IntSupplier thrower = () -> { throw boom; };
            thrower.getAsInt();
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
