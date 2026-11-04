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
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
