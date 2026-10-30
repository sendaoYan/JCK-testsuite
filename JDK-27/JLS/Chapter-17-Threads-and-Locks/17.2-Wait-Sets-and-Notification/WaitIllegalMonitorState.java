/*
 * @test
 * @summary JLS 17.2.1 / 17.2.2: invoking wait() or notify() on an object whose
 *          monitor is not held throws IllegalMonitorStateException.
 * @run main WaitIllegalMonitorState
 */

public class WaitIllegalMonitorState {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        boolean threw = false;
        try {
            lock.wait();              // no monitor held
        } catch (IllegalMonitorStateException e) {
            threw = true;
        }
        if (!threw) throw new AssertionError("wait() without monitor did not throw");

        threw = false;
        try {
            lock.notify();            // no monitor held
        } catch (IllegalMonitorStateException e) {
            threw = true;
        }
        if (!threw) throw new AssertionError("notify() without monitor did not throw");

        System.out.println("PASS");
    }
}
