/*
 * @test
 * @summary JLS 17.2.2 (Java SE 8): notify() wakes up a single thread from the
 *          object's wait set, leaving the remaining waiters blocked.
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
                        lock.wait();
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

        allWaiting.await();
        synchronized (lock) { lock.notify(); }

        check(firstDone.await(5, TimeUnit.SECONDS), "one waiter was notified and finished");
        check(woken.get() == 1, "notify() woke exactly one of " + N + " waiters");

        // 被唤醒线程可能尚未退出 run()（仍 alive），isAlive 计数会把"刚被唤醒者"误算成"仍阻塞者"。
        // 改为统计仍处于 WAITING（wait() 未返回）的线程数：notify() 后应恰好 N-1 个仍在等待。
        int waiting = 0;
        for (Thread t : ts) if (t.getState() == Thread.State.WAITING) waiting++;
        check(waiting == N - 1, "remaining waiters still blocked in wait()");

        synchronized (lock) { lock.notifyAll(); }
        for (Thread t : ts) t.join(5000);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
