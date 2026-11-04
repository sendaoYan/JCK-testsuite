/*
 * @test
 * @summary JVMS 5.5: a class is initialized at most once even when several
 *          threads race to initialize it; the single run of <clinit> is
 *          serialized and the losing threads observe the completed state.
 * @run main ConcurrentClinitOnce
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentClinitOnce {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Target {
        static final AtomicInteger inits = new AtomicInteger();
        static int value;
        static {
            inits.incrementAndGet();
            value = 7;
        }
        static int read() { return value; }
    }

    public static void main(String[] args) throws Exception {
        final CountDownLatch start = new CountDownLatch(1);
        final int N = 2;
        Thread[] threads = new Thread[N];

        for (int i = 0; i < N; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    try { start.await(); }
                    catch (InterruptedException e) { throw new Error(e); }
                    Target.read();   // first active use races with the others
                }
            });
            threads[i].start();
        }

        start.countDown();
        for (Thread t : threads) t.join(30000);
        for (Thread t : threads)
            check(!t.isAlive(), "worker thread completed without hanging");

        check(Target.inits.get() == 1,
                "<clinit> ran exactly once despite concurrent initialization");
        check(Target.value == 7,
                "the single <clinit> run produced the completed state");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
