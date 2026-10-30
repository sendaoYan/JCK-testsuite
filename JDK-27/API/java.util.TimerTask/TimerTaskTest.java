/*
 * @test
 * @summary java.util.TimerTask API conformance: run is invoked and cancel prevents execution.
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
        Timer timer = new Timer();
        timer.schedule(task, 0);
        check(ran.await(5, TimeUnit.SECONDS), "run was invoked");
        timer.cancel();
    }

    public static void main(String[] args) throws Exception {
        testTimerTask();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
