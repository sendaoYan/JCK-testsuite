/*
 * @test
 * @summary java.util.function.Supplier contract: get lazily evaluates and returns fresh values.
 * @run main SupplierTest
 */

import java.util.function.Supplier;

public class SupplierTest {
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
        Supplier<String> sup = () -> "hello";
        check("hello".equals(sup.get()), "get");
        check("hello".equals(sup.get()), "get idempotent");
        Supplier<Integer> counter = new Supplier<Integer>() {
            int i = 0;
            public Integer get() { return ++i; }
        };
        check(counter.get() == 1, "get 1");
        check(counter.get() == 2, "get 2");
        Supplier<Object> nullSup = () -> null;
        check(nullSup.get() == null, "supplier may return null");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
