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
            // The remaining delay is (triggerTime - now); it never exceeds the
            // requested 100ms, but under -Xcomp / a loaded machine the schedule
            // + this read can take >100ms, so `remaining > 0` is not observable
            // here (the task may already be due). Only the upper bound is hard.
            check(remaining <= 100, "getDelay never exceeds the requested delay before run: " + remaining);
            check(f.get() == null, "get() waits for completion");
            check(value[0] == 7, "task executed");
            check(f.isDone() && !f.isCancelled(), "isDone after completion");
            check(f.getDelay(TimeUnit.MILLISECONDS) <= 0, "getDelay <= 0 after completion");
        } finally {
            exec.shutdownNow();
        }
    }

    // ---- Part 3: white-box lock: no methods of its own ----
    static void testInterfaceLock() throws Exception {
        Class<?> c = cls("java.util.concurrent.ScheduledFuture");
        check(c.getDeclaredMethods().length == 0,
              "declares no methods of its own (Delayed + Future union)");
        check(c.getTypeParameters().length == 1, "one type parameter <V>");
        check(RunnableScheduledFuture.class.isAssignableFrom(FutureTask.class) == false,
              "plain FutureTask is not scheduled");
    }

    // ---- Part 4: trigger-time ordering via compareTo ----
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

    // ---- Part 5: zero-delay Callable/Runnable + cancel semantics ----
    static void testZeroDelayAndCancel() throws Exception {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        try {
            ScheduledFuture<String> f = exec.schedule(() -> "ran", 0, TimeUnit.MILLISECONDS);
            check("ran".equals(f.get()), "zero-delay Callable runs and returns its value");
            check(f.isDone() && !f.isCancelled(), "completed normally");
            check(f.getDelay(TimeUnit.NANOSECONDS) <= 0, "delay <= 0 after completion");
            ScheduledFuture<?> r = exec.schedule(() -> { }, 0, TimeUnit.MILLISECONDS);
            check(r.get() == null, "Runnable variant get() is null");
            ScheduledFuture<?> c = exec.schedule(() -> { }, 1, TimeUnit.HOURS);
            check(c.cancel(true), "cancel(true) on pending task succeeds");
            check(c.isCancelled() && c.isDone(), "cancelled and done");
            boolean got = false;
            try { c.get(); }
            catch (CancellationException expected) { got = true; }
            check(got, "get() on cancelled scheduled future throws CancellationException");
            check(!c.cancel(false), "re-cancel returns false (FutureTask semantics)");
        } finally {
            exec.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceLock();
        testCompareToTriggerTime();
        testZeroDelayAndCancel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
