/*
 * @test
 * @summary java.util.function.BinaryOperator contract: minBy/maxBy, apply, andThen, null comparator->NPE, tie keeps first.
 * @run main BinaryOperatorTest
 */

import java.util.function.BinaryOperator;

public class BinaryOperatorTest {
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
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
        check(max.apply(1, 2) == 2, "maxBy");
        check(max.apply(2, 1) == 2, "maxBy reversed");
        BinaryOperator<Integer> min = BinaryOperator.minBy(Integer::compareTo);
        check(min.apply(1, 2) == 1, "minBy");
        check(min.apply(2, 1) == 1, "minBy reversed");
        check(max.apply(1, 1) == 1, "max tie first arg");
        checkThrows(NullPointerException.class, () -> BinaryOperator.maxBy(null), "maxBy(null)");
        checkThrows(NullPointerException.class, () -> BinaryOperator.minBy(null), "minBy(null)");
        BinaryOperator<String> concat = (a, b) -> a + b;
        check("ab".equals(concat.apply("a", "b")), "apply");
        check("AB".equals(concat.andThen(String::toUpperCase).apply("a", "b")), "andThen");
        checkThrows(NullPointerException.class, () -> concat.andThen(null), "andThen(null)");
    }

    // ===================== 契约深化：minBy/maxBy 比较语义/相等时返回/BiFunction 继承 =====================
    static void testContracts() {
        BinaryOperator<Integer> min = BinaryOperator.minBy(Integer::compare);
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compare);
        check(min.apply(3, 7) == 3 && min.apply(7, 3) == 3, "minBy returns lesser");
        check(max.apply(3, 7) == 7 && max.apply(7, 3) == 7, "maxBy returns greater");
        Integer a = new Integer(5), b = new Integer(5);
        check(min.apply(a, b) == a, "minBy with equal compare returns first operand");
        check(max.apply(a, b) == a, "maxBy with equal compare returns first operand");
        checkThrows(NullPointerException.class, () -> BinaryOperator.minBy(null), "minBy(null) NPE");
        checkThrows(NullPointerException.class, () -> BinaryOperator.maxBy(null), "maxBy(null) NPE");
        // 比较器被调用两次（两个方向各一）
        final int[] cmpCalls = {0};
        java.util.Comparator<Integer> counting = (x, y) -> { cmpCalls[0]++; return Integer.compare(x, y); };
        BinaryOperator.minBy(counting).apply(1, 2);
        check(cmpCalls[0] == 1, "comparator invoked once per apply");
        // String.CASE_INSENSITIVE_ORDER 场景
        BinaryOperator<String> ciMax = BinaryOperator.maxBy(String.CASE_INSENSITIVE_ORDER);
        check("BANANA".equals(ciMax.apply("apple", "BANANA")), "CI max picks case-insensitive greater");
        // BinaryOperator 是 BiFunction（可赋值/被 Stream.reduce 使用）
        java.util.function.BiFunction<Integer, Integer, Integer> asBF = (BinaryOperator<Integer>) (x, y) -> x * y;
        check(asBF.apply(6, 7) == 42, "assignable to BiFunction");
        check(java.util.stream.IntStream.rangeClosed(1, 5).boxed()
                .reduce(1, (x, y) -> x * y) == 120, "reduce uses BinaryOperator");
        // 逆序比较器 min/max 对调
        BinaryOperator<Integer> minRev = BinaryOperator.minBy(java.util.Collections.reverseOrder());
        check(minRev.apply(3, 7) == 7, "reversed comparator flips minBy");
    }

    public static void main(String[] args) {
        testMain();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
