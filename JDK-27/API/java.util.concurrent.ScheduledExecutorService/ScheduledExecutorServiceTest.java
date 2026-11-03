/*
 * @test
 * @summary java.util.concurrent.ScheduledExecutorService conformance: white-box structure +
 *          black-box behavior (schedule/scheduleAtFixedRate/scheduleWithFixedDelay).
 * @run main ScheduledExecutorServiceTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutorServiceTest {
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
        Class<?> c = cls("java.util.concurrent.ScheduledExecutorService");
        check(c.isInterface(), "ScheduledExecutorService is an interface");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1 && ifs[0] == ExecutorService.class, "extends ExecutorService");
        hasMethod(c, "schedule", Callable.class, long.class, TimeUnit.class);
        hasMethod(c, "schedule", Runnable.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleAtFixedRate", Runnable.class, long.class, long.class, TimeUnit.class);
        hasMethod(c, "scheduleWithFixedDelay", Runnable.class, long.class, long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        try {
            // schedule(Callable, 0 delay) -> result
            ScheduledFuture<Integer> f = ses.schedule(new Callable<Integer>() {
                public Integer call() { return 99; }
            }, 0, TimeUnit.MILLISECONDS);
            check(f.get() == 99, "scheduled Callable result");
            check(f.isDone(), "scheduled future is done");

            // schedule(Runnable, 0 delay) -> null result once done
            final boolean[] ran = new boolean[1];
            ScheduledFuture<?> rf = ses.schedule((Runnable) () -> ran[0] = true, 0, TimeUnit.MILLISECONDS);
            check(rf.get() == null, "scheduled Runnable returns null");
            check(ran[0], "scheduled Runnable executed");

            // scheduleAtFixedRate: runs at least once, then cancel immediately
            CountDownLatch firstRun = new CountDownLatch(1);
            ScheduledFuture<?> fr = ses.scheduleAtFixedRate(firstRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(firstRun.await(10, TimeUnit.SECONDS), "scheduleAtFixedRate first run");
            check(fr.cancel(true), "scheduleAtFixedRate cancelled");

            // scheduleWithFixedDelay: runs at least once, then cancel immediately
            CountDownLatch delayedRun = new CountDownLatch(1);
            ScheduledFuture<?> fd = ses.scheduleWithFixedDelay(delayedRun::countDown, 0, 1, TimeUnit.SECONDS);
            check(delayedRun.await(10, TimeUnit.SECONDS), "scheduleWithFixedDelay first run");
            check(fd.cancel(true), "scheduleWithFixedDelay cancelled");
        } finally {
            ses.shutdownNow();
        }
        check(ses.awaitTermination(10, TimeUnit.SECONDS), "terminates after shutdownNow");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
