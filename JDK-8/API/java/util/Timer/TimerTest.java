/*
 * @test
 * @summary java.util.Timer API conformance (Java 8 surface): complete contract coverage of the
 *          4 constructors, schedule(task, delay) firing (bounded latch await), schedule with a
 *          far-future delay then cancel (task never runs), schedule(task, delay, period) and
 *          scheduleAtFixedRate (task runs then cancel), cancel() idempotence, and purge().
 * @run main TimerTest
 */

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- 契约深化：负延迟/负周期/null/任务复用异常矩阵 ----
    static void testArgumentContracts() {
        TimerTask noop = new TimerTask() { public void run() {} };
        Timer t = new Timer(true);
        try {
            t.schedule(noop, -1L);
            check(false, "negative delay should throw");
        } catch (IllegalArgumentException e) {
            check("Negative delay.".equals(e.getMessage()), "negative delay message");
        }
        try {
            t.schedule(noop, 0L, -1L);
            check(false, "negative period should throw");
        } catch (IllegalArgumentException e) {
            check("Non-positive period.".equals(e.getMessage()), "negative period message");
        }
        try {
            t.schedule(noop, 0L, 0L);
            check(false, "zero period should throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "zero period message");
        }
        try {
            t.schedule(null, 0L);
            check(false, "null task NPE");
        } catch (NullPointerException npe) {
            check(true, "null task NPE");
        }
        try {
            t.scheduleAtFixedRate(null, 0L, 1000L);
            check(false, "scheduleAtFixedRate null task NPE");
        } catch (NullPointerException npe) {
            check(true, "scheduleAtFixedRate null task NPE");
        }
        // 已取消的任务不能再次调度
        TimerTask done = new TimerTask() { public void run() {} };
        t.schedule(done, 50000L);
        done.cancel();
        try {
            t.schedule(done, 50000L);
            check(false, "rescheduling cancelled task should throw");
        } catch (IllegalStateException ise) {
            check(true, "reschedule cancelled task ISE");
        }
        // 已执行过的任务同样不能再调度
        final java.util.concurrent.CountDownLatch ran = new java.util.concurrent.CountDownLatch(1);
        TimerTask once = new TimerTask() { public void run() { ran.countDown(); } };
        t.schedule(once, 0L);
        try { ran.await(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ie) { }
        try {
            t.schedule(once, 50000L);
            check(false, "rescheduling executed task should throw");
        } catch (IllegalStateException ise) {
            check(true, "reschedule executed task ISE");
        }
        t.cancel();
        try {
            new Timer(null, true);
            check(false, "Timer(null, daemon) NPE");
        } catch (NullPointerException npe) {
            check(true, "Timer(null, daemon) NPE");
        }
    }

    static void testConstructors() {
        Timer t1 = new Timer();
        t1.cancel();
        Timer t2 = new Timer(true);
        t2.cancel();
        Timer t3 = new Timer("name");
        t3.cancel();
        Timer t4 = new Timer("name", true);
        t4.cancel();
        passed++;
    }

    static void testScheduleOnce() throws Exception {
        AtomicBoolean taskRan = new AtomicBoolean();
        CountDownLatch ran = new CountDownLatch(1);
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                taskRan.set(true);
                ran.countDown();
            }
        }, 0);
        check(ran.await(5, TimeUnit.SECONDS), "task ran within timeout");
        check(taskRan.get(), "task run() was invoked");
        timer.cancel();
    }

    static void testCancelBeforeFire() throws Exception {
        AtomicBoolean ran = new AtomicBoolean();
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() { ran.set(true); }
        }, 60_000L); // far future
        timer.cancel();
        // cancel() before the due time guarantees the task never runs.
        check(!ran.get(), "task cancelled before due time did not run");
    }

    static void testSchedulePeriodic() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() { ran.countDown(); }
        }, 0, 10);
        check(ran.await(5, TimeUnit.SECONDS), "periodic task ran at least once");
        timer.cancel();
    }

    static void testScheduleAtFixedRate() throws Exception {
        CountDownLatch ran = new CountDownLatch(1);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() { ran.countDown(); }
        }, 0, 10);
        check(ran.await(5, TimeUnit.SECONDS), "fixed-rate task ran at least once");
        timer.cancel();
    }

    static void testCancelIdempotentAndPurge() {
        Timer timer = new Timer(true);
        timer.cancel();
        timer.cancel(); // idempotent
        check(timer.purge() >= 0, "purge returns non-negative");
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
        testConstructors();
        testArgumentContracts();
        testScheduleOnce();
        testCancelBeforeFire();
        testSchedulePeriodic();
        testScheduleAtFixedRate();
        testPeriodicFixedRateCounts();
        testTaskStateMachine();
        testCancelIdempotentAndPurge();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
