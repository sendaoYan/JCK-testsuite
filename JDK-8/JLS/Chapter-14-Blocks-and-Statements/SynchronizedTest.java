/*
 * @test
 * @summary JLS 14.19 (Java SE 8): a synchronized statement acquires a monitor, and
 *          Java locks are reentrant.
 * @run main SynchronizedTest
 */

public class SynchronizedTest {
    public static void main(String[] args) {
        Object lock = new Object();
        int result;
        synchronized (lock) {
            synchronized (lock) {   // reentrant acquisition
                result = 42;
            }
        }
        if (result != 42) throw new AssertionError("synchronized reentrant");
        System.out.println("PASS");
    }
}
