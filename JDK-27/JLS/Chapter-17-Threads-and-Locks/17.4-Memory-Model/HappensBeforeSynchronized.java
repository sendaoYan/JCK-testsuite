/*
 * @test
 * @summary JLS 17.4.5: an unlock on a monitor happens-before every subsequent lock
 *          on that same monitor, so writes inside a synchronized block are visible
 *          to a thread that later acquires the same lock.
 * @run main HappensBeforeSynchronized
 */

public class HappensBeforeSynchronized {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final Object lock = new Object();
    static int data = 0;
    static boolean ready = false;   // guarded by lock

    public static void main(String[] args) throws Exception {
        final int[] seen = {-1};

        Thread reader = new Thread(() -> {
            boolean got = false;
            while (!got) {
                synchronized (lock) {
                    got = ready;
                    if (got) seen[0] = data;
                }
                if (!got) Thread.yield();
            }
        });

        reader.start();
        synchronized (lock) {
            data = 42;
            ready = true;          // release happens-before the reader's acquire
        }
        reader.join(5000);

        check(!reader.isAlive(), "reader terminated");
        check(seen[0] == 42, "unlock happens-before subsequent lock makes data visible");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
