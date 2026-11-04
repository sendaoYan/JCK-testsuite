/*
 * @test
 * @summary java.util.function.UnaryOperator contract: identity and inherited Function composition.
 * @run main UnaryOperatorTest
 */

import java.util.function.UnaryOperator;

public class UnaryOperatorTest {
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
        UnaryOperator<Integer> inc = x -> x + 1;
        check(inc.apply(1) == 2, "apply");
        check(inc.andThen(x -> x * 10).apply(1) == 20, "andThen");
        check(inc.compose((Integer x) -> x * 10).apply(1) == 11, "compose");
        check(UnaryOperator.<Integer>identity().apply(42) == 42, "identity");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        checkThrows(NullPointerException.class, () -> inc.compose(null), "compose(null)");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
