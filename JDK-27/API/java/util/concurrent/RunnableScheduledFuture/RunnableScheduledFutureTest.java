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

    // ---- Part 3: full periodicity + delay matrix across all three schedule forms ----
    static void testSchedulerProductsMatrix() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            Object oneShot = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(oneShot instanceof RunnableScheduledFuture, "schedule() product type");
            Object fixedDelay = exec.scheduleWithFixedDelay(() -> { }, 1, 1, TimeUnit.HOURS);
            check(fixedDelay instanceof RunnableScheduledFuture,
                  "scheduleWithFixedDelay() product type");
            check(((RunnableScheduledFuture<?>) fixedDelay).isPeriodic(),
                  "fixed-delay is periodic");
            RunnableScheduledFuture<?> f = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 2, TimeUnit.HOURS);
            long hours = f.getDelay(TimeUnit.HOURS);
            check(hours >= 0 && hours <= 2, "getDelay(HOURS) in [0, 2]");
            long seconds = f.getDelay(TimeUnit.SECONDS);
            check(seconds > 0 && seconds <= 2 * 3600, "getDelay(SECONDS) in (0, 7200]");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- Part 4: cancel semantics vs the work queue (default removeOnCancelPolicy=false) ----
    static void testCancelVsQueue() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> f = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(exec.getQueue().size() == 1, "task sits in the work queue");
            check(f.cancel(false), "cancel succeeds on a pending scheduled task");
            check(f.isCancelled() && f.isDone(), "cancelled and done");
            // empirically-locked (JDK-8 and JDK-27 alike): with the default
            // removeOnCancelPolicy==false the cancelled task stays in the queue
            check(exec.getQueue().size() == 1, "cancelled task still queued (default policy, 实测)");
            check(exec.getQueue().contains(f), "the queued entry is the future itself");
            exec.purge();
            check(exec.getQueue().isEmpty(), "purge() removes cancelled queue entries");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSchedulerProductsMatrix();
        testCancelVsQueue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
