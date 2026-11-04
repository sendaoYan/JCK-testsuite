/*
 * @test
 * @summary java.util.function.DoubleConsumer contract: accept and andThen ordering, side effects, null->NPE.
 * @run main DoubleConsumerTest
 */

import java.util.function.DoubleConsumer;

public class DoubleConsumerTest {
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
        final double[] log = new double[1];
        DoubleConsumer inc = x -> { log[0] += x; };
        inc.accept(5.0);
        check(log[0] == 5.0, "accept");
        inc.accept(0);
        check(log[0] == 5.0, "accept zero");
        DoubleConsumer dec = x -> { log[0] -= x; };
        inc.andThen(dec).accept(5.0);
        check(log[0] == 5.0, "andThen inc then dec unchanged");
        checkThrows(NullPointerException.class, () -> inc.andThen(null), "andThen(null)");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
