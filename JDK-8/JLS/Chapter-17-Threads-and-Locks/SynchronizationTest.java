/*
 * @test
 * @summary JLS 17.1 (Java SE 8): synchronized methods provide mutual exclusion, so
 *          concurrent increments do not lose updates.
 * @run main SynchronizationTest
 */

public class SynchronizationTest {
    static int counter = 0;
    static final int N = 10000;

    static synchronized void inc() { counter++; }

    public static void main(String[] args) throws Exception {
        Thread t1 = new Thread(() -> { for (int i = 0; i < N; i++) inc(); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < N; i++) inc(); });
        t1.start(); t2.start();
        t1.join(); t2.join();
        if (counter != 2 * N) throw new AssertionError("lost update: " + counter);
        System.out.println("PASS");
    }
}
