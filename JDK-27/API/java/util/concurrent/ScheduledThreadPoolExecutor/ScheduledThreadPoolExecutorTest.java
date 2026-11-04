/*
 * @test
 * @summary java.util.concurrent.ScheduledThreadPoolExecutor conformance: white-box structure +
 *          black-box behavior (schedule/scheduleAtFixedRate/scheduleWithFixedDelay/policies).
 * @run main ScheduledThreadPoolExecutorTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ScheduledThreadPoolExecutor");
        check(!c.isInterface(), "ScheduledThreadPoolExecutor is a class");
        check(c.getSuperclass() == ThreadPoolExecutor.class, "extends ThreadPoolExecutor");
        check(ScheduledExecutorService.class.isAssignableFrom(c), "implements ScheduledExecutorService");
        hasMethod(c, "schedule", Callable.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleAtFixedRate", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleWithFixedDelay", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "getContinueExistingPeriodicTasksAfterShutdownPolicy");
        hasMethod(c, "setContinueExistingPeriodicTasksAfterShutdownPolicy", boolean.class);
        hasMethod(c, "setRemoveOnCancelPolicy", boolean.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        try {
            // submit a Callable
            java.util.concurrent.Future<Integer> f = stpe.submit(new Callable<Integer>() {
                public Integer call() { return 5; }
            });
            check(f.get() == 5, "submit result");

            // schedule(Callable, 0) -> result
            ScheduledFuture<Integer> sf = stpe.schedule(() -> 12, 0, TimeUnit.MILLISECONDS);
            check(sf.get() == 12, "schedule result");

            // schedule(Runnable, 0) -> runs
            final boolean[] ran = new boolean[1];
            ScheduledFuture<?> rf = stpe.schedule((Runnable) () -> ran[0] = true, 0, TimeUnit.MILLISECONDS);
            check(rf.get() == null && ran[0], "schedule(Runnable) runs");

            // scheduleAtFixedRate: first run then cancel
            CountDownLatch fixedRun = new CountDownLatch(1);
            ScheduledFuture<?> fixed = stpe.scheduleAtFixedRate(fixedRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(fixedRun.await(10, TimeUnit.SECONDS), "scheduleAtFixedRate first run");
            check(fixed.cancel(true), "cancel fixed-rate task");

            // scheduleWithFixedDelay: first run then cancel
            CountDownLatch delayRun = new CountDownLatch(1);
            ScheduledFuture<?> delayed = stpe.scheduleWithFixedDelay(delayRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(delayRun.await(10, TimeUnit.SECONDS), "scheduleWithFixedDelay first run");
            check(delayed.cancel(true), "cancel fixed-delay task");

            // shutdown policies (defaults + setters)
            check(!stpe.getContinueExistingPeriodicTasksAfterShutdownPolicy(), "default: do not continue periodic after shutdown");
            stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(true);
            check(stpe.getContinueExistingPeriodicTasksAfterShutdownPolicy(), "setContinueExistingPeriodicTasksAfterShutdownPolicy(true)");
            stpe.setExecuteExistingDelayedTasksAfterShutdownPolicy(true);
            check(stpe.getExecuteExistingDelayedTasksAfterShutdownPolicy(), "setExecuteExistingDelayedTasksAfterShutdownPolicy(true)");
            check(!stpe.getRemoveOnCancelPolicy(), "default removeOnCancel false");
            stpe.setRemoveOnCancelPolicy(true);
            check(stpe.getRemoveOnCancelPolicy(), "setRemoveOnCancelPolicy(true)");
            check(stpe.getQueue() != null, "getQueue returns the work queue");
        } finally {
            stpe.shutdownNow();
        }
        check(stpe.awaitTermination(10, TimeUnit.SECONDS), "terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
