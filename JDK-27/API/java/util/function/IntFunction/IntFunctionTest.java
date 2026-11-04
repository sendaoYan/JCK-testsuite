/*
 * @test
 * @summary java.util.function.IntFunction contract: apply maps int to object.
 * @run main IntFunctionTest
 */

import java.util.function.IntFunction;

public class IntFunctionTest {
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
        IntFunction<String> f = Integer::toString;
        check("5".equals(f.apply(5)), "apply");
        check("0".equals(f.apply(0)), "apply zero");
        check("-3".equals(f.apply(-3)), "apply negative");
        IntFunction<int[]> arr = int[]::new;
        check(arr.apply(3).length == 3, "apply new array");
        checkThrows(NegativeArraySizeException.class, () -> arr.apply(-1), "array negative length");
    }

    // ===================== 契约深化：泛型返回/null 返回/方法引用构造 =====================
    static void testContracts() {
        IntFunction<String[]> arr = String[]::new;
        check(arr.apply(3).length == 3 && arr.apply(0).length == 0, "ctor method ref array");
        IntFunction<String> s = n -> "n" + n;
        check(s.apply(-5).equals("n-5"), "negative input exact");
        IntFunction<Object> nuller = n -> n < 0 ? null : Integer.valueOf(n);
        check(nuller.apply(-1) == null && nuller.apply(3).equals(Integer.valueOf(3)), "null result legal");
        IntFunction<int[]> arr2 = int[]::new;
        check(arr2.apply(4).length == 4, "primitive array ctor ref");
        // 匿名类
        IntFunction<Integer> anon = new IntFunction<Integer>() {
            public Integer apply(int v) { return v + 1; }
        };
        check(anon.apply(41) == 42, "anonymous impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
