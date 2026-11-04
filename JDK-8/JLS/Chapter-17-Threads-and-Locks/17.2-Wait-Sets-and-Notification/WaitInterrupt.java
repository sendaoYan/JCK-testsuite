/*
 * @test
 * @summary JLS 17.2.3 (Java SE 8): interrupting a thread blocked in wait() removes
 *          it from the wait set and causes wait() to throw InterruptedException.
 * @run main WaitInterrupt
 */

import java.util.concurrent.CountDownLatch;

public class WaitInterrupt {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        final boolean[] interrupted = {false};
        final CountDownLatch entered = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                entered.countDown();
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    interrupted[0] = true;
                }
            }
        });

        worker.start();
        entered.await();
        worker.interrupt();
        worker.join(5000);

        check(!worker.isAlive(), "worker terminated after interrupt");
        check(interrupted[0], "wait() threw InterruptedException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
