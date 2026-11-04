/*
 * @test
 * @summary JVMS 6.5: monitorexit throws IllegalMonitorStateException when the
 *          object's monitor is not held, and monitorenter throws
 *          NullPointerException when the object is null.
 * @compile MonitorException.jasm
 * @run main MonitorExceptionTest
 */

public class MonitorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        try {
            MonitorException.exitWithoutEnter(new Object());
            throw new AssertionError("monitorexit without monitorenter did not throw");
        } catch (IllegalMonitorStateException e) { }
        check(true, "monitorexit IllegalMonitorStateException");

        try {
            MonitorException.enterNull();
            throw new AssertionError("monitorenter null did not throw");
        } catch (NullPointerException e) { }
        check(true, "monitorenter null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
