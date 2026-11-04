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

    // ===================== 契约深化：null 返回/异常透传/类型多态/方法引用族 =====================
    static void testContracts() {
        Supplier<Object> nullSup = () -> null;
        check(nullSup.get() == null, "null return legal");
        RuntimeException boom = new RuntimeException("sup");
        try {
            Supplier<Object> thrower = () -> { throw boom; };
            thrower.get();
            check(false, "exception relayed");
        } catch (RuntimeException e) {
            check(e == boom, "no wrapping");
        }
        Supplier<java.util.List<String>> listMr = java.util.ArrayList<String>::new;
        check(listMr.get().isEmpty(), "ctor method ref supplier");
        Integer boxed = Integer.valueOf(42);
        Supplier<Integer> boxing = boxed::intValue;
        check(boxing.get() == 42, "boxed constant method ref");
        // 每次调用新实例
        Supplier<Object> fresh = Object::new;
        check(fresh.get() != fresh.get(), "distinct instance per get");
        // 多态赋值
        Supplier<? extends Number> num = () -> Integer.valueOf(3);
        Number n = num.get();
        check(n.intValue() == 3, "covariant supplier");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
