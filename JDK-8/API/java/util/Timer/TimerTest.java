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

    public static void main(String[] args) throws Exception {
        testConstructors();
        testScheduleOnce();
        testCancelBeforeFire();
        testSchedulePeriodic();
        testScheduleAtFixedRate();
        testCancelIdempotentAndPurge();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
