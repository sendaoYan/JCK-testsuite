/*
 * @test
 * @summary java.util.function.Consumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main ConsumerTest
 */

import java.util.function.Consumer;

public class ConsumerTest {
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
        final int[] log = new int[1];
        Consumer<Integer> inc = x -> { log[0] += x; };
        inc.accept(5);
        check(log[0] == 5, "accept");
        inc.accept(-2);
        check(log[0] == 3, "accept negative");
        Consumer<Integer> dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(10);
        check(log[0] == 3, "andThen: inc then dec -> unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
        final int[] order = new int[1];
        Consumer<Integer> first = x -> { check(order[0] == 0, "first"); order[0] = 1; };
        Consumer<Integer> second = x -> { check(order[0] == 1, "second"); order[0] = 2; };
        first.andThen(second).accept(0);
        check(order[0] == 2, "order completed");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
