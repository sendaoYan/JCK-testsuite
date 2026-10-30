/*
 * @test
 * @summary JLS 17.3: Thread.yield is only a scheduling hint; it returns normally
 *          and has no synchronization semantics.
 * @run main YieldHints
 */

public class YieldHints {
    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            Thread.yield();
        }
        // The calling thread simply continues executing.
        if (!Thread.currentThread().isAlive())
            throw new AssertionError("yield terminated the thread");

        System.out.println("PASS");
    }
}
