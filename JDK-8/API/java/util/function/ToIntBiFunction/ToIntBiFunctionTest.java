/*
 * @test
 * @summary java.util.function.ToIntBiFunction contract: apply extracts primitive from two objects.
 * @run main ToIntBiFunctionTest
 */

import java.util.function.ToIntBiFunction;

public class ToIntBiFunctionTest {
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
        ToIntBiFunction<String, String> cmp = (a, b) -> a.compareTo(b);
        check(cmp.applyAsInt("a", "b") < 0, "apply");
        check(cmp.applyAsInt("b", "a") > 0, "apply reversed");
        check(cmp.applyAsInt("a", "a") == 0, "apply equal");
    }

    // ===================== 契约深化：双参精确/溢出回绕/组合使用 =====================
    static void testContracts() {
        ToIntBiFunction<String, String> cmp = String::compareTo;
        check(cmp.applyAsInt("a", "b") < 0 && cmp.applyAsInt("b", "a") > 0 && cmp.applyAsInt("a", "a") == 0,
              "compareTo sign convention");
        ToIntBiFunction<Integer, Integer> mul = (a, b) -> a * b;
        check(mul.applyAsInt(Integer.MAX_VALUE, 2) == -2, "overflow wraps");
        ToIntBiFunction<int[], int[]> dot2 = (a, b) -> a[0] * b[0] + a[1] * b[1];
        check(dot2.applyAsInt(new int[] {1, 2}, new int[] {3, 4}) == 11, "dot product");
        ToIntBiFunction<String, Character> idx = (s, c) -> s.indexOf(c.charValue());
        check(idx.applyAsInt("banana", 'n') == 2, "indexOf");
        ToIntBiFunction<Object, Object> nullGuard = (a, b) -> a == null ? -1 : 1;
        check(nullGuard.applyAsInt(null, "x") == -1, "null branch");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
