/*
 * @test
 * @summary java.util.concurrent.RunnableScheduledFuture API conformance: isPeriodic.
 * @run main RunnableScheduledFutureTest
 */

import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RunnableScheduledFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRunnableScheduledFuture() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> oneShot = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 1, java.util.concurrent.TimeUnit.HOURS);
            RunnableScheduledFuture<?> periodic = (RunnableScheduledFuture<?>)
                exec.scheduleAtFixedRate(() -> { }, 1, 1, java.util.concurrent.TimeUnit.HOURS);
            check(!oneShot.isPeriodic(), "one-shot is not periodic");
            check(periodic.isPeriodic(), "fixed-rate is periodic");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- white-box: interface shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<RunnableScheduledFuture> c = RunnableScheduledFuture.class;
        check(c.isInterface(), "RunnableScheduledFuture is an interface");
        check(RunnableFuture.class.isAssignableFrom(c), "extends RunnableFuture");
        check(ScheduledFuture.class.isAssignableFrom(c), "extends ScheduledFuture");
        check(Runnable.class.isAssignableFrom(c), "is a Runnable");
        check(java.util.concurrent.Future.class.isAssignableFrom(c), "is a Future");
        check(java.util.concurrent.Delayed.class.isAssignableFrom(c), "is a Delayed");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method isPeriodic()");
        check(c.getMethod("isPeriodic").getReturnType() == boolean.class,
              "isPeriodic returns boolean");
    }

    // ---- scheduler products are RunnableScheduledFuture; periodicity matrix ----
    static void testSchedulerProducts() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            Object oneShot = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(oneShot instanceof RunnableScheduledFuture,
                  "schedule() product is a RunnableScheduledFuture");
            Object fixedRate = exec.scheduleAtFixedRate(() -> { }, 1, 1, TimeUnit.HOURS);
            check(fixedRate instanceof RunnableScheduledFuture,
                  "scheduleAtFixedRate() product is a RunnableScheduledFuture");
            Object fixedDelay = exec.scheduleWithFixedDelay(() -> { }, 1, 1, TimeUnit.HOURS);
            check(fixedDelay instanceof RunnableScheduledFuture,
                  "scheduleWithFixedDelay() product is a RunnableScheduledFuture");
            check(!((RunnableScheduledFuture<?>) oneShot).isPeriodic(), "one-shot not periodic");
            check(((RunnableScheduledFuture<?>) fixedRate).isPeriodic(), "fixed-rate periodic");
            check(((RunnableScheduledFuture<?>) fixedDelay).isPeriodic(), "fixed-delay periodic");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- delay surface inherited from Delayed ----
    static void testDelaySurface() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> f = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 2, TimeUnit.HOURS);
            long hours = f.getDelay(TimeUnit.HOURS);
            check(hours >= 0, "getDelay(HOURS) >= 0");
            check(hours <= 2, "getDelay(HOURS) <= initial 2");
            long seconds = f.getDelay(TimeUnit.SECONDS);
            check(seconds > 0 && seconds <= 2 * 3600, "getDelay(SECONDS) in (0, 7200]");
            check(f.compareTo(f) == 0, "compareTo reflexive");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- cancel semantics vs the work queue (default removeOnCancelPolicy=false) ----
    static void testCancelRemovesTask() {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        try {
            RunnableScheduledFuture<?> f = (RunnableScheduledFuture<?>)
                exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(exec.getQueue().size() == 1, "task sits in the work queue");
            check(f.cancel(false), "cancel succeeds on a pending scheduled task");
            check(f.isCancelled() && f.isDone(), "cancelled and done");
            // empirically-locked (both JDK-8 and JDK-27): with the default
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
        testRunnableScheduledFuture();
        testWhiteBoxStructure();
        testSchedulerProducts();
        testDelaySurface();
        testCancelRemovesTask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
