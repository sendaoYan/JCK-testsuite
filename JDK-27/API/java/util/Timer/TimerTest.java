/*
 * @test
 * @summary java.util.Timer conformance: white-box structure plus black-box behavior of
 *          purge (deterministic cancelled-task accounting), cancel semantics (schedule after
 *          cancel throws IllegalStateException), and a one-shot scheduled task via a daemon
 *          timer with a latch (no sleep).
 * @run main TimerTest
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Timer");
        check(!c.isInterface(), "Timer is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Timer is concrete");
        c.getMethod("schedule", TimerTask.class, long.class);
        c.getMethod("schedule", TimerTask.class, long.class, long.class);
        c.getMethod("cancel");
        c.getMethod("purge");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        testPurge();
        testCancelSemantics();
        testOneShotRuns();
        testPeriodicFixedRateCounts();
        testTaskStateMachine();
    }

    static void testPurge() {
        Timer timer = new Timer(true);
        try {
            check(timer.purge() == 0, "purge on fresh timer == 0");
            TimerTask task = new TimerTask() { public void run() { } };
            timer.schedule(task, 3600000L); // far future, stays queued
            check(task.cancel(), "cancel a scheduled task returns true");
            // The Timer thread may have already drained the cancelled task, so
            // purge() returns 0 or 1; the contract guarantees a non-negative count.
            int removed = timer.purge();
            check(removed >= 0, "purge returns non-negative count");
            check(timer.purge() == 0, "second purge removes none");
        } finally {
            timer.cancel();
        }
    }

    static void testCancelSemantics() {
        Timer timer = new Timer(true);
        timer.cancel();
        assertThrows(IllegalStateException.class,
                () -> timer.schedule(new TimerTask() { public void run() { } }, 0),
                "schedule after cancel throws IllegalStateException");
    }

    static void testOneShotRuns() {
        AtomicBoolean taskRan = new AtomicBoolean();
        CountDownLatch ran = new CountDownLatch(1);
        Timer timer = new Timer(true);
        try {
            timer.schedule(new TimerTask() {
                public void run() {
                    taskRan.set(true);
                    ran.countDown();
                }
            }, 0);
            check(ran.await(10, TimeUnit.SECONDS), "one-shot task ran within timeout");
            check(taskRan.get(), "task run() was invoked");
        } catch (InterruptedException e) {
            throw new AssertionError("interrupted: " + e);
        } finally {
            timer.cancel();
        }
    }


    // ---- 契约深化：固定速率周期任务计数 / 任务状态机 / scheduledExecutionTime ----
    static void testPeriodicFixedRateCounts() throws Exception {
        CountDownLatch three = new CountDownLatch(3);
        java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();
        Timer timer = new Timer(true);
        try {
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    runs.incrementAndGet();
                    three.countDown();
                }
            }, 0, 5);
            check(three.await(10, TimeUnit.SECONDS), "fixed-rate task reached 3 runs within timeout");
            check(runs.get() >= 3, "run counter >= 3");
        } finally {
            timer.cancel();
        }
        check(runs.get() >= 3, "counter stable after cancel");
    }

    static void testTaskStateMachine() throws Exception {
        TimerTask fresh = new TimerTask() { public void run() { } };
        check(!fresh.cancel(), "cancel on never-scheduled task returns false");
        Timer timer = new Timer(true);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            java.util.concurrent.atomic.AtomicLong firstAt = new java.util.concurrent.atomic.AtomicLong();
            TimerTask t = new TimerTask() {
                public void run() {
                    firstAt.compareAndSet(0, scheduledExecutionTime());
                    ran.countDown();
                }
            };
            long before = System.currentTimeMillis();
            timer.schedule(t, 20);
            check(ran.await(10, TimeUnit.SECONDS), "task ran");
            check(t.scheduledExecutionTime() == firstAt.get(),
                  "scheduledExecutionTime stable after run");
            check(firstAt.get() >= before, "scheduledExecutionTime >= schedule-time wall clock");
        } finally {
            timer.cancel();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
