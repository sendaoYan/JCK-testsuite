/*
 * @test
 * @summary java.lang.IllegalMonitorStateException API conformance: wait/notify without holding the monitor.
 * @run main IllegalMonitorStateExceptionTest
 */

public class IllegalMonitorStateExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalMonitorStateException() throws Exception {
        Object lock = new Object();
        try {
            lock.wait(1);
            check(false, "wait outside synchronized should throw");
        } catch (IllegalMonitorStateException e) {
            check(true, "wait outside synchronized throws IllegalMonitorStateException");
        }
        try {
            lock.notify();
            check(false, "notify outside synchronized should throw");
        } catch (IllegalMonitorStateException e) {
            check(true, "notify outside synchronized throws IllegalMonitorStateException");
        }
    }

    public static void main(String[] args) throws Exception {
        testIllegalMonitorStateException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
