/*
 * @test
 * @summary JLS 17.2.3 (Java SE 8): when a thread blocked in wait() is interrupted,
 *          wait() throws InterruptedException and clears the thread's interrupt
 *          status.
 * @run main WaitInterruptStatusCleared
 */

import java.util.concurrent.CountDownLatch;

public class WaitInterruptStatusCleared {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        final boolean[] cleared = {false};
        final CountDownLatch entered = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                entered.countDown();
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    cleared[0] = !Thread.currentThread().isInterrupted();
                }
            }
        });

        worker.start();
        entered.await();
        worker.interrupt();
        worker.join(5000);

        check(!worker.isAlive(), "worker terminated after interrupt");
        check(cleared[0], "interrupt status cleared when InterruptedException is thrown");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
