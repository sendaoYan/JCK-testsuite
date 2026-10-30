/*
 * @test
 * @summary JLS 17.4 (Java SE 8): a write to a volatile field happens-before any
 *          subsequent read of it, making the write visible across threads.
 * @run main VolatileVisibilityTest
 */

public class VolatileVisibilityTest {
    static volatile boolean stop = false;
    static int result = 0;

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(() -> {
            while (!stop) { /* spin */ }
            result = 42;
        });
        t.start();
        Thread.sleep(50);
        stop = true;
        t.join(2000);
        if (t.isAlive()) throw new AssertionError("worker did not observe volatile write");
        if (result != 42) throw new AssertionError("result after volatile visibility");
        System.out.println("PASS");
    }
}
