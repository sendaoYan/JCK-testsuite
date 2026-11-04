/*
 * @test
 * @summary java.util.function.BiConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main BiConsumerTest
 */

import java.util.function.BiConsumer;

public class BiConsumerTest {
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
        final int[] acc = new int[1];
        BiConsumer<Integer, Integer> add = (a, b) -> { acc[0] += a * b; };
        add.accept(2, 3);
        check(acc[0] == 6, "accept");
        add.accept(0, 100);
        check(acc[0] == 6, "accept zero");
        BiConsumer<Integer, Integer> sub = (a, b) -> { acc[0] -= a * b; };
        add.andThen(sub).accept(2, 3);
        check(acc[0] == 6, "andThen add then sub -> unchanged");
        checkThrows(NullPointerException.class, () -> add.andThen(null), "andThen(null)");
        final int[] order = new int[1];
        add.andThen((a, b) -> { check(order[0] == 0, "first"); order[0] = 1; }).accept(0, 0);
        check(order[0] == 1, "order");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
