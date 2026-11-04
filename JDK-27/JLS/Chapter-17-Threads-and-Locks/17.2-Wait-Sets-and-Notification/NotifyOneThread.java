/*
 * @test
 * @summary JLS 17.2.2: notify() wakes up a single thread from the object's wait
 *          set, leaving the remaining waiters blocked.
 * @run main NotifyOneThread
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NotifyOneThread {
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
        final CountDownLatch firstDone = new CountDownLatch(1);
        final Thread[] ts = new Thread[N];

        for (int i = 0; i < N; i++) {
            ts[i] = new Thread(() -> {
                synchronized (lock) {
                    allWaiting.countDown();
                    try {
                        lock.wait();          // single wait; see below for why safe
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    woken.incrementAndGet();
                    firstDone.countDown();
                }
            });
            ts[i].start();
        }

        // By the time this returns, every waiter has entered its synchronized
        // block. Acquiring the lock next blocks until each has called wait() and
        // released the monitor, so notify() cannot be lost.
        allWaiting.await();
        synchronized (lock) { lock.notify(); }   // wake exactly one

        check(firstDone.await(5, TimeUnit.SECONDS), "one waiter was notified and finished");
        check(woken.get() == 1, "notify() woke exactly one of " + N + " waiters");

        int alive = 0;
        for (Thread t : ts) if (t.isAlive()) alive++;
        check(alive == N - 1, "remaining waiters still blocked");

        // Cleanup: release the remaining waiters so no non-daemon threads linger.
        synchronized (lock) { lock.notifyAll(); }
        for (Thread t : ts) t.join(5000);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
