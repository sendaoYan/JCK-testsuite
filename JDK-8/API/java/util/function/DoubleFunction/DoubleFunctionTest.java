/*
 * @test
 * @summary java.util.function.DoubleFunction contract: apply maps double to object.
 * @run main DoubleFunctionTest
 */

import java.util.function.DoubleFunction;

public class DoubleFunctionTest {
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
        DoubleFunction<String> f = Double::toString;
        check("5.0".equals(f.apply(5.0)), "apply");
        check("0.0".equals(f.apply(0.0)), "apply zero");
        check("-3.5".equals(f.apply(-3.5)), "apply negative");
        DoubleFunction<Double> half = x -> x / 2;
        check(half.apply(3.0) == 1.5, "apply unary");
    }

    // ===================== 契约深化：泛型返回/精确输入/null 返回/重载无关性 =====================
    static void testContracts() {
        DoubleFunction<String> f = d -> "x" + d;
        check(f.apply(0.5).equals("x0.5"), "apply exact");
        DoubleFunction<double[]> arr = d -> new double[] {d, d};
        check(arr.apply(2.5).length == 2 && arr.apply(2.5)[1] == 2.5, "generic array result");
        DoubleFunction<Object> nuller = d -> d < 0 ? null : "pos";
        check(nuller.apply(-1.0) == null && "pos".equals(nuller.apply(1.0)), "null result legal");
        // 特殊值入参
        final java.util.List<Double> seen = new java.util.ArrayList<Double>();
        DoubleFunction<Void> rec = d -> { seen.add(d); return null; };
        rec.apply(Double.NaN);
        rec.apply(Double.NEGATIVE_INFINITY);
        check(seen.size() == 2 && Double.isNaN(seen.get(0)) && seen.get(1) == Double.NEGATIVE_INFINITY,
              "special doubles passed through");
        // 方法引用
        DoubleFunction<Double> boxed = Double::valueOf;
        check(boxed.apply(3.25).doubleValue() == 3.25, "method reference impl");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
