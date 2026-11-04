/*
 * @test
 * @summary java.util.TimerTask conformance: white-box structure plus black-box behavior of
 *          run invocation, cancel semantics (unscheduled cancel returns false, scheduled cancel
 *          returns true and prevents execution), and scheduledExecutionTime.
 * @run main TimerTaskTest
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimerTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.TimerTask");
        check(!c.isInterface(), "TimerTask is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "TimerTask is abstract");
        check(Runnable.class.isAssignableFrom(c), "TimerTask implements Runnable");
        c.getMethod("run");
        c.getMethod("cancel");
        c.getMethod("scheduledExecutionTime");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testUnscheduledCancel();
        testScheduledCancel();
        testRuns();
    }

    static void testUnscheduledCancel() {
        TimerTask task = new TimerTask() { public void run() { } };
        check(!task.cancel(), "cancel before scheduled returns false");
        check(task.scheduledExecutionTime() == 0L, "scheduledExecutionTime before schedule == 0");
    }

    static void testScheduledCancel() {
        AtomicBoolean ran = new AtomicBoolean();
        TimerTask task = new TimerTask() { public void run() { ran.set(true); } };
        Timer timer = new Timer(true);
        try {
            timer.schedule(task, 3600000L); // far future, stays queued
            check(task.cancel(), "cancel a scheduled task returns true");
            timer.purge();
            check(!ran.get(), "cancelled task did not run");
        } finally {
            timer.cancel();
        }
    }

    static void testRuns() {
        CountDownLatch ran = new CountDownLatch(1);
        TimerTask task = new TimerTask() {
            public void run() {
                ran.countDown();
            }
        };
        Timer timer = new Timer(true);
        try {
            timer.schedule(task, 0);
            check(ran.await(10, TimeUnit.SECONDS), "run was invoked");
        } catch (InterruptedException e) {
            throw new AssertionError("interrupted: " + e);
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
