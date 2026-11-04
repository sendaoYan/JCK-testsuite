/*
 * @test
 * @summary java.lang.IllegalThreadStateException API conformance: restarting a dead thread.
 * @run main IllegalThreadStateExceptionTest
 */

public class IllegalThreadStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalThreadStateException() throws Exception {
        Thread t = new Thread(() -> {});
        t.start();
        t.join();
        try {
            t.start();
            check(false, "restarting a dead thread should throw");
        } catch (IllegalThreadStateException e) {
            check(true, "restarting a dead thread throws IllegalThreadStateException");
        }
    }

    public static void main(String[] args) throws Exception {
        testIllegalThreadStateException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
