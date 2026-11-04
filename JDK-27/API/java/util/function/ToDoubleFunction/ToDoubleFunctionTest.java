/*
 * @test
 * @summary java.util.function.ToDoubleFunction contract: apply extracts primitive from object.
 * @run main ToDoubleFunctionTest
 */

import java.util.function.ToDoubleFunction;

public class ToDoubleFunctionTest {
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
        ToDoubleFunction<String> len = String::length;
        check(len.applyAsDouble("abc") == 3, "apply");
        check(len.applyAsDouble("") == 0, "apply empty");
        ToDoubleFunction<Object> hash = Object::hashCode;
        check(hash.applyAsDouble(new Object()) != 0, "apply hashCode");
    }

    // ===================== 契约深化：精确映射/特殊值/方法引用族 =====================
    static void testContracts() {
        ToDoubleFunction<String> len = String::length;
        check(len.applyAsDouble("hello") == 5.0, "method ref");
        ToDoubleFunction<Integer> box = Integer::doubleValue;
        check(box.applyAsDouble(7) == 7.0, "unbox method ref");
        ToDoubleFunction<double[]> first = a -> a[0];
        check(first.applyAsDouble(new double[] {1.25}) == 1.25, "array element");
        ToDoubleFunction<Object> hashScaled = o -> (o == null ? 0 : 1.5);
        check(hashScaled.applyAsDouble(null) == 0.0 && hashScaled.applyAsDouble("x") == 1.5, "null branch");
        // 计数器证明每元素恰好一次
        final int[] calls = {0};
        ToDoubleFunction<Integer> counting = n -> { calls[0]++; return n; };
        counting.applyAsDouble(1);
        counting.applyAsDouble(2);
        check(calls[0] == 2, "invocation count");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
