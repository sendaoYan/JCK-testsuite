/*
 * @test
 * @summary JLS 14.19: a synchronized statement locks the monitor of a non-null
 *          value, allows re-entrant acquisition by the same thread, and throws
 *          NullPointerException if the expression evaluates to null.
 * @run main SynchronizedStatement
 */

public class SynchronizedStatement {
    public static void main(String[] args) {
        Object lock = new Object();
        synchronized (lock) {
            synchronized (lock) {   // re-entrant: same thread may re-acquire
                // no deadlock
            }
        }

        Object n = null;
        try {
            synchronized (n) {}
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        System.out.println("PASS");
    }
}
