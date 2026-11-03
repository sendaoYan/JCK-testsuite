/*
 * @test
 * @summary java.lang.Runnable conformance: white-box structure + black-box behavior
 *          (run() executes synchronously on the calling thread and on a new thread).
 * @run main RunnableTest
 */

public class RunnableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Runnable");
        check(c.isInterface(), "Runnable is an interface");
        java.lang.reflect.Method run = c.getMethod("run");
        check(run.getReturnType() == void.class && run.getParameterCount() == 0,
              "run() takes no args and returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final boolean[] ran = {false};
        Runnable r = () -> ran[0] = true;
        r.run();
        check(ran[0], "run() executes synchronously on calling thread");

        final boolean[] threadRan = {false};
        Thread t = new Thread(() -> threadRan[0] = true);
        t.start();
        t.join();
        check(threadRan[0], "run() executes on started thread");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
