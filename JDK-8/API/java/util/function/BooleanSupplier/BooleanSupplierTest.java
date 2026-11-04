/*
 * @test
 * @summary java.util.function.BooleanSupplier contract: getAsBoolean evaluates.
 * @run main BooleanSupplierTest
 */

import java.util.function.BooleanSupplier;

public class BooleanSupplierTest {
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
        BooleanSupplier t = () -> true;
        check(t.getAsBoolean(), "getAsBoolean true");
        BooleanSupplier f = () -> false;
        check(!f.getAsBoolean(), "getAsBoolean false");
        final boolean[] flip = new boolean[1];
        BooleanSupplier toggle = () -> { flip[0] = !flip[0]; return flip[0]; };
        check(toggle.getAsBoolean(), "toggle 1 true");
        check(!toggle.getAsBoolean(), "toggle 2 false");
        check(toggle.getAsBoolean(), "toggle 3 true");
    }

    // ===================== 契约深化：两种取值/有状态/方法引用/异常透传 =====================
    static void testContracts() {
        BooleanSupplier t = () -> true, f = () -> false;
        check(t.getAsBoolean() && t.getAsBoolean(), "constant true stable");
        check(!f.getAsBoolean(), "constant false");
        final boolean[] flip = {false};
        BooleanSupplier toggle = () -> flip[0] = !flip[0];
        check(toggle.getAsBoolean() && !toggle.getAsBoolean() && toggle.getAsBoolean(),
              "stateful supplier toggles from false");
        java.util.List<String> empty = new java.util.ArrayList<String>();
        BooleanSupplier mr = empty::isEmpty;
        check(mr.getAsBoolean(), "method reference to isEmpty");
        empty.add("x");
        check(!mr.getAsBoolean(), "same supplier reflects backing object");
        RuntimeException boom = new RuntimeException("bs");
        try {
            BooleanSupplier thrower = () -> { throw boom; };
            thrower.getAsBoolean();
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "exact exception relayed, no wrapping");
        }
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
