/*
 * @test
 * @summary java.util.concurrent.ScheduledFuture API conformance: delay/cancel via scheduler.
 * @run main ScheduledFutureTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledFutureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testScheduledFuture() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            final int[] value = new int[1];
            ScheduledFuture<?> f = exec.schedule(() -> { value[0] = 7; }, 50, TimeUnit.MILLISECONDS);
            check(f instanceof Delayed, "extends Delayed");
            f.get();
            check(value[0] == 7, "task executed");
            check(f.isDone(), "isDone after get");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- white-box: interface shape (no methods of its own) ----
    static void testWhiteBoxStructure() throws Exception {
        Class<ScheduledFuture> c = ScheduledFuture.class;
        check(c.isInterface(), "ScheduledFuture is an interface");
        check(Delayed.class.isAssignableFrom(c), "extends Delayed");
        check(Future.class.isAssignableFrom(c), "extends Future");
        check(c.getDeclaredMethods().length == 0,
              "declares no methods of its own (Delayed + Future union)");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        check(RunnableScheduledFuture.class.isAssignableFrom(java.util.concurrent.FutureTask.class)
                  == false, "plain FutureTask is not scheduled");
    }

    // ---- instanceof + delay surface on a long-delay task ----
    static void testDelaySurface() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledFuture<?> f = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(f instanceof Delayed, "product is Delayed");
            long hours = f.getDelay(TimeUnit.HOURS);
            check(hours >= 0 && hours <= 1, "getDelay(HOURS) in [0, 1]");
            long minutes = f.getDelay(TimeUnit.MINUTES);
            check(minutes > 0 && minutes <= 60, "getDelay(MINUTES) in (0, 60]");
            long seconds = f.getDelay(TimeUnit.SECONDS);
            check(seconds > 0 && seconds <= 3600, "getDelay(SECONDS) in (0, 3600]");
            check(minutes * 60 <= seconds && seconds < (minutes + 1) * 60,
                  "MINUTES/SECONDS conversions are consistent (floor)");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- compareTo orders by trigger time ----
    static void testCompareToTriggerTime() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledFuture<?> earlier = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            ScheduledFuture<?> later = exec.schedule(() -> { }, 2, TimeUnit.HOURS);
            check(earlier.compareTo(later) < 0, "earlier trigger compares less");
            check(later.compareTo(earlier) > 0, "later trigger compares greater");
            check(earlier.compareTo(earlier) == 0, "compareTo reflexive");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- zero-delay execution + negative delay after completion ----
    static void testZeroDelay() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledFuture<String> f = exec.schedule(new Callable<String>() {
                public String call() { return "ran"; }
            }, 0, TimeUnit.MILLISECONDS);
            check("ran".equals(f.get()), "zero-delay Callable runs and returns its value");
            check(f.isDone() && !f.isCancelled(), "completed normally");
            check(f.getDelay(TimeUnit.NANOSECONDS) <= 0, "delay <= 0 after completion");
            check(f.getDelay(TimeUnit.HOURS) <= 0, "delay <= 0 in any unit after completion");
            ScheduledFuture<?> r = exec.schedule(new Runnable() { public void run() { } },
                0, TimeUnit.MILLISECONDS);
            check(r.get() == null, "Runnable variant get() is null");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- cancel semantics ----
    static void testCancel() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledFuture<?> f = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(f.cancel(true), "cancel(true) on pending task succeeds");
            check(f.isCancelled() && f.isDone(), "cancelled and done");
            boolean got = false;
            try { f.get(); }
            catch (CancellationException expected) { got = true; }
            check(got, "get() on cancelled scheduled future throws CancellationException");
            check(!f.cancel(false), "re-cancel returns false");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testScheduledFuture();
        testWhiteBoxStructure();
        testDelaySurface();
        testCompareToTriggerTime();
        testZeroDelay();
        testCancel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
