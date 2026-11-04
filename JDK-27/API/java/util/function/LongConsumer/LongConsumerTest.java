/*
 * @test
 * @summary java.util.function.LongConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main LongConsumerTest
 */

import java.util.function.LongConsumer;

public class LongConsumerTest {
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
        final long[] log = new long[1];
        LongConsumer inc = x -> { log[0] += x; };
        inc.accept(5L);
        check(log[0] == 5L, "accept");
        inc.accept(0);
        check(log[0] == 5L, "accept zero");
        LongConsumer dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(5L);
        check(log[0] == 5L, "andThen inc then dec unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
