/*
 * @test
 * @summary JLS 17.1: a synchronized block provides mutual exclusion; two threads
 *          incrementing a shared counter under the same monitor produce the exact
 *          sum of their increments.
 * @run main MutualExclusion
 */

public class MutualExclusion {
    static final Object lock = new Object();
    static final int ITERS = 100_000;
    static int counter = 0;

    static void inc() {
        synchronized (lock) {
            counter++;
        }
    }

    public static void main(String[] args) throws Exception {
        Thread a = new Thread(() -> { for (int i = 0; i < ITERS; i++) inc(); });
        Thread b = new Thread(() -> { for (int i = 0; i < ITERS; i++) inc(); });
        a.start();
        b.start();
        a.join();
        b.join();

        if (counter != 2 * ITERS)
            throw new AssertionError("counter == " + counter + ", expected " + (2 * ITERS));

        System.out.println("PASS");
    }
}
