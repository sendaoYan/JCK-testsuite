/*
 * @test
 * @summary JLS 17.2.3: interrupting a thread blocked in wait() removes it from
 *          the wait set and causes wait() to throw InterruptedException.
 * @run main WaitInterrupt
 */

import java.util.concurrent.CountDownLatch;

public class WaitInterrupt {
    static final Object lock = new Object();

    public static void main(String[] args) throws Exception {
        final boolean[] interrupted = {false};
        final CountDownLatch entered = new CountDownLatch(1);

        Thread worker = new Thread(() -> {
            synchronized (lock) {
                entered.countDown();   // worker holds the monitor and is about to wait
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    interrupted[0] = true;
                }
            }
        });

        worker.start();
        entered.await();              // worker has the monitor
        worker.interrupt();
        worker.join(5000);

        if (worker.isAlive()) throw new AssertionError("worker did not terminate");
        if (!interrupted[0]) throw new AssertionError("InterruptedException not thrown");

        System.out.println("PASS");
    }
}
