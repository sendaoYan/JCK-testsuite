/*
 * @test
 * @summary java.util.function.BiFunction contract: apply and andThen ordering, null->NPE, exception relay.
 * @run main BiFunctionTest
 */

import java.util.function.BiFunction;

public class BiFunctionTest {
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
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        check(add.apply(2, 3) == 5, "apply");
        check(add.apply(-1, 1) == 0, "apply zero");
        BiFunction<Integer, Integer, String> concat = add.andThen(Object::toString);
        check("5".equals(concat.apply(2, 3)), "andThen g(f(x))");
        checkThrows(NullPointerException.class, () -> add.andThen(null), "andThen(null)");
        final int[] calls = new int[1];
        BiFunction<Integer,Integer,Integer> lazy = add.andThen(x -> { calls[0]++; return x; });
        check(calls[0] == 0, "andThen lazy");
        check(lazy.apply(1, 2) == 3, "andThen result");
        check(calls[0] == 1, "called once");
        IllegalStateException ex = new IllegalStateException("boom");
        BiFunction<Integer,Integer,Integer> fail = (a, b) -> { throw ex; };
        try { fail.andThen(x -> x + 1).apply(1, 2); throw new AssertionError("relay"); }
        catch (IllegalStateException c) { check(c == ex, "relay"); }
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
