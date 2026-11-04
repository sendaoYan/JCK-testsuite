/*
 * @test
 * @summary java.util.function.LongFunction contract: apply maps long to object.
 * @run main LongFunctionTest
 */

import java.util.function.LongFunction;

public class LongFunctionTest {
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
        LongFunction<String> f = Long::toString;
        check("5".equals(f.apply(5L)), "apply");
        check("0".equals(f.apply(0L)), "apply zero");
        check("-3".equals(f.apply(-3L)), "apply negative");
        LongFunction<Long> neg = x -> -x;
        check(neg.apply(3L) == -3L, "apply unary");
    }

    // ===================== 契约深化：泛型返回/null 返回/方法引用构造/边界值 =====================
    static void testContracts() {
        LongFunction<String> s = n -> "L" + n;
        check(s.apply(-5L).equals("L-5"), "negative input exact");
        LongFunction<Long[]> arr = n -> new Long[(int) n];
        check(arr.apply(3).length == 3, "array ctor sized by arg");
        LongFunction<Object> nuller = n -> n == 0 ? null : Long.valueOf(n);
        check(nuller.apply(0L) == null && nuller.apply(9L).equals(Long.valueOf(9)), "null result legal");
        LongFunction<String> edge = n -> String.valueOf(n);
        check(edge.apply(Long.MIN_VALUE).equals("-9223372036854775808"), "MIN formats");
        check(edge.apply(Long.MAX_VALUE).equals("9223372036854775807"), "MAX formats");
        LongFunction<Integer> anon = new LongFunction<Integer>() {
            public Integer apply(long v) { return (int) v; }
        };
        check(anon.apply(5L) == 5, "anonymous impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
