/*
 * @test
 * @summary java.util.concurrent.ScheduledFuture conformance: white-box structure + black-box behavior.
 * @run main ScheduledFutureTest
 */

import java.util.concurrent.*;

public class ScheduledFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ScheduledFuture");
        check(c.isInterface(), "ScheduledFuture is an interface");
        check(Delayed.class.isAssignableFrom(c), "extends Delayed");
        check(Future.class.isAssignableFrom(c), "extends Future");
        hasMethod(c, "getDelay", TimeUnit.class);
        hasMethod(c, "compareTo", Object.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            final int[] value = new int[1];
            ScheduledFuture<?> f = exec.schedule(() -> { value[0] = 7; }, 100, TimeUnit.MILLISECONDS);
            check(f instanceof Delayed, "instanceof Delayed");
            long remaining = f.getDelay(TimeUnit.MILLISECONDS);
            check(remaining > 0 && remaining <= 100, "getDelay in (0, 100] before run");
            check(f.get() == null, "get() waits for completion");
            check(value[0] == 7, "task executed");
            check(f.isDone() && !f.isCancelled(), "isDone after completion");
            check(f.getDelay(TimeUnit.MILLISECONDS) <= 0, "getDelay <= 0 after completion");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
