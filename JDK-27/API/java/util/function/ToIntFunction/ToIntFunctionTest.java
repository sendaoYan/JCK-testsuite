/*
 * @test
 * @summary java.util.function.ToIntFunction contract: apply extracts primitive from object.
 * @run main ToIntFunctionTest
 */

import java.util.function.ToIntFunction;

public class ToIntFunctionTest {
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
        ToIntFunction<String> len = String::length;
        check(len.applyAsInt("abc") == 3, "apply");
        check(len.applyAsInt("") == 0, "apply empty");
        ToIntFunction<Object> hash = Object::hashCode;
        check(hash.applyAsInt(new Object()) != 0, "apply hashCode");
    }

    // ===================== 契约深化：精确映射/方法引用族/边界值 =====================
    static void testContracts() {
        ToIntFunction<String> len = String::length;
        check(len.applyAsInt("") == 0 && len.applyAsInt("abc") == 3, "length method ref");
        ToIntFunction<Integer> id = Integer::intValue;
        check(id.applyAsInt(Integer.MAX_VALUE) == Integer.MAX_VALUE, "MAX boundary");
        ToIntFunction<Character> ch = c -> (int) c.charValue();
        check(ch.applyAsInt('A') == 65, "char widening");
        ToIntFunction<double[]> signum = a -> (int) Math.signum(a[0]);
        check(signum.applyAsInt(new double[] {-2.0 }) == -1, "signum negative");
        final int[] calls = {0};
        ToIntFunction<Object> counting = o -> { calls[0]++; return 0; };
        counting.applyAsInt("x");
        counting.applyAsInt("y");
        check(calls[0] == 2, "invocation count");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
