/*
 * @test
 * @summary java.util.function.ToDoubleBiFunction contract: apply extracts primitive from two objects.
 * @run main ToDoubleBiFunctionTest
 */

import java.util.function.ToDoubleBiFunction;

public class ToDoubleBiFunctionTest {
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
        ToDoubleBiFunction<String, String> cmp = (a, b) -> a.compareTo(b);
        check(cmp.applyAsDouble("a", "b") < 0, "apply");
        check(cmp.applyAsDouble("b", "a") > 0, "apply reversed");
        check(cmp.applyAsDouble("a", "a") == 0, "apply equal");
    }

    // ===================== 契约深化：双参精确/特殊值/组合使用 =====================
    static void testContracts() {
        ToDoubleBiFunction<String, String> totalLen = (a, b) -> a.length() + b.length();
        check(totalLen.applyAsDouble("ab", "cde") == 5.0, "exact");
        ToDoubleBiFunction<Integer, Integer> hypot = Math::hypot;
        check(hypot.applyAsDouble(3, 4) == 5.0, "3-4-5 exact");
        check(Double.isNaN(((ToDoubleBiFunction<Double, Double>) (a, b) -> a / b).applyAsDouble(0.0, 0.0)),
              "0/0 NaN");
        ToDoubleBiFunction<double[], int[]> mix = (arr, n) -> arr[0] * n[0];
        check(mix.applyAsDouble(new double[] {2.5}, new int[] {4}) == 10.0, "array args");
        ToDoubleBiFunction<Object, Object> nullSafe = (a, b) -> a == null || b == null ? -1.0 : 1.0;
        check(nullSafe.applyAsDouble(null, "x") == -1.0 && nullSafe.applyAsDouble("x", "y") == 1.0,
              "null handling by function");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
