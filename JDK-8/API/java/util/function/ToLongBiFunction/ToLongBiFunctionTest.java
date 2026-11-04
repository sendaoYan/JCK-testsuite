/*
 * @test
 * @summary java.util.function.ToLongBiFunction contract: apply extracts primitive from two objects.
 * @run main ToLongBiFunctionTest
 */

import java.util.function.ToLongBiFunction;

public class ToLongBiFunctionTest {
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
        ToLongBiFunction<String, String> cmp = (a, b) -> a.compareTo(b);
        check(cmp.applyAsLong("a", "b") < 0, "apply");
        check(cmp.applyAsLong("b", "a") > 0, "apply reversed");
        check(cmp.applyAsLong("a", "a") == 0, "apply equal");
    }

    // ===================== 契约深化：双参精确/long 域无溢出 =====================
    static void testContracts() {
        ToLongBiFunction<Integer, Integer> mul = (a, b) -> (long) a * b;
        check(mul.applyAsLong(Integer.MAX_VALUE, Integer.MAX_VALUE) == 4611686014132420609L,
              "long domain no overflow");
        ToLongBiFunction<String, String> combined = (a, b) -> a.length() * 1000000000L + b.length();
        check(combined.applyAsLong("ab", "c") == 2000000001L, "exact composition");
        ToLongBiFunction<long[], long[]> dot = (a, b) -> a[0] * b[0];
        check(dot.applyAsLong(new long[] {1000000000L }, new long[] {1000000000L }) == 1000000000000000000L,
              "big multiply exact");
        ToLongBiFunction<Object, Object> nullGuard = (a, b) -> a == null || b == null ? -1L : 1L;
        check(nullGuard.applyAsLong(null, null) == -1L, "null branch");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
