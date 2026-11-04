/*
 * @test
 * @summary JLS 17.1 (Java SE 8): a synchronized statement acquires and releases the
 *          monitor of a specific object; concurrent updates under the same monitor
 *          are mutually exclusive, and the monitor is reentrant.
 * @run main SynchronizedStatementLock
 */

public class SynchronizedStatementLock {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Object lock = new Object();
    static int counter = 0;
    static final int ITERS = 200_000;

    static void inc() {
        synchronized (lock) { counter++; }
    }

    static int reentrant() {
        synchronized (lock) {
            synchronized (lock) {
                return 42;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Thread a = new Thread(() -> { for (int i = 0; i < ITERS; i++) inc(); });
        Thread b = new Thread(() -> { for (int i = 0; i < ITERS; i++) inc(); });
        a.start();
        b.start();
        a.join();
        b.join();

        check(counter == 2 * ITERS, "mutual exclusion on one monitor");
        check(reentrant() == 42, "synchronized block reentrancy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
