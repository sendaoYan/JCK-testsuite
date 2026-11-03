/*
 * @test
 * @summary java.util.concurrent.RunnableScheduledFuture conformance: white-box structure + black-box behavior.
 * @run main RunnableScheduledFutureTest
 */

import java.util.concurrent.*;

public class RunnableScheduledFutureTest {
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
        Class<?> c = cls("java.util.concurrent.RunnableScheduledFuture");
        check(c.isInterface(), "RunnableScheduledFuture is an interface");
        check(RunnableFuture.class.isAssignableFrom(c), "extends RunnableFuture");
        check(ScheduledFuture.class.isAssignableFrom(c), "extends ScheduledFuture");
        hasMethod(c, "isPeriodic");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> oneShot = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            RunnableScheduledFuture<?> periodic = (RunnableScheduledFuture<?>)
                exec.scheduleAtFixedRate(() -> { }, 1, 1, TimeUnit.HOURS);
            check(!oneShot.isPeriodic(), "one-shot is not periodic");
            check(periodic.isPeriodic(), "fixed-rate is periodic");
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
