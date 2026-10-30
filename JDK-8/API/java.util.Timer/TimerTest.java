/*
 * @test
 * @summary java.util.Timer API conformance: schedule a task and cancel.
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

    static void testTimer() throws Exception {
        AtomicBoolean taskRan = new AtomicBoolean();
        CountDownLatch ran = new CountDownLatch(1);
        Timer timer = new Timer();
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

    public static void main(String[] args) throws Exception {
        testTimer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
