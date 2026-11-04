/*
 * @test
 * @summary JLS 17.2.1 / 17.2.2 (Java SE 8): invoking wait(), notify() or
 *          notifyAll() on an object whose monitor is not held by the current
 *          thread throws IllegalMonitorStateException.
 * @run main WaitIllegalMonitorState
 */

public class WaitIllegalMonitorState {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        boolean threw = false;
        try { lock.wait(); } catch (IllegalMonitorStateException e) { threw = true; }
        check(threw, "wait() without monitor throws IllegalMonitorStateException");

        threw = false;
        try { lock.notify(); } catch (IllegalMonitorStateException e) { threw = true; }
        check(threw, "notify() without monitor throws IllegalMonitorStateException");

        threw = false;
        try { lock.notifyAll(); } catch (IllegalMonitorStateException e) { threw = true; }
        check(threw, "notifyAll() without monitor throws IllegalMonitorStateException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
