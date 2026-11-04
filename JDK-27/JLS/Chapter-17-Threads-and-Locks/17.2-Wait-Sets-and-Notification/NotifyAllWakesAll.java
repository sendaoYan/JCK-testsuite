/*
 * @test
 * @summary JLS 17.2.2: notifyAll() wakes up every thread waiting on the object's
 *          wait set.
 * @run main NotifyAllWakesAll
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class NotifyAllWakesAll {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        final Object lock = new Object();
        final int N = 4;
        final AtomicInteger woken = new AtomicInteger();
        final CountDownLatch allWaiting = new CountDownLatch(N);
        final Thread[] ts = new Thread[N];

        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                synchronized (lock) {
                    allWaiting.countDown();
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    woken.incrementAndGet();
                }
            });
            ts[i].start();
        }

        allWaiting.await();
        synchronized (lock) { lock.notifyAll(); }   // wake all N

        for (Thread t : ts) t.join(5000);
        check(woken.get() == N, "notifyAll() woke all " + N + " waiters");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
