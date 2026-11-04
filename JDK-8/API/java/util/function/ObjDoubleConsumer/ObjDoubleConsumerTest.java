/*
 * @test
 * @summary java.util.function.ObjDoubleConsumer contract: accept object plus primitive, side effects.
 * @run main ObjDoubleConsumerTest
 */

import java.util.function.ObjDoubleConsumer;

public class ObjDoubleConsumerTest {
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
        final StringBuilder sb = new StringBuilder();
        ObjDoubleConsumer<String> acc = (s, v) -> { sb.append(s).append(v); };
        acc.accept("v=", 7.0);
        check("v=7.0".equals(sb.toString()), "accept");
        acc.accept("!", 7.0);
        check("v=7.0!7.0".equals(sb.toString()), "accept second");
    }
    public static void main(String[] args) {
        testMain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
