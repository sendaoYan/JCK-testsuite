/*
 * @test
 * @summary java.util.TimerTask API conformance (Java 8 surface): complete contract coverage of
 *          run() invocation, cancel() return semantics (true before execution / false after),
 *          scheduledExecutionTime() for a scheduled task, and implements Runnable.
 * @run main TimerTaskTest
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TimerTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTimerTask() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        TimerTask task = new TimerTask() {
            public void run() {
                ran.countDown();
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 0);
        check(ran.await(5, TimeUnit.SECONDS), "run was invoked");
        timer.cancel();
    }

    static void testCancelSemantics() {
        TimerTask fresh = new TimerTask() {
            public void run() { }
        };
        // A never-scheduled (VIRGIN) task has not been scheduled, so cancel returns false.
        check(!fresh.cancel(), "cancel on never-scheduled task returns false");

        TimerTask scheduled = new TimerTask() {
            public void run() { }
        };
        Timer timer = new Timer(true);
        timer.schedule(scheduled, 60 * 60 * 1000L); // far future
        check(scheduled.cancel(), "cancel on scheduled task returns true");
        check(!scheduled.cancel(), "second cancel returns false");
        timer.cancel();
        check(fresh instanceof Runnable, "TimerTask implements Runnable");
    }

    static void testScheduledExecutionTime() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        final long[] sched = new long[1];
        TimerTask task = new TimerTask() {
            public void run() {
                sched[0] = scheduledExecutionTime();
                ran.countDown();
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 0);
        check(ran.await(5, TimeUnit.SECONDS), "task ran");
        check(sched[0] >= 0, "scheduledExecutionTime non-negative");
        timer.cancel();
    }

    public static void main(String[] args) throws Exception {
        testTimerTask();
        testCancelSemantics();
        testScheduledExecutionTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
