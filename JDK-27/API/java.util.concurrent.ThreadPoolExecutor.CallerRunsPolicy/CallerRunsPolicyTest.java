/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy conformance: white-box structure + black-box behavior.
 * @run main CallerRunsPolicyTest
 */

import java.util.concurrent.*;

public class CallerRunsPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$CallerRunsPolicy");
        check(!c.isInterface(), "class");
        check(RejectedExecutionHandler.class.isAssignableFrom(c), "implements RejectedExecutionHandler");
        check(c.getConstructor() != null, "no-arg ctor");
        hasMethod(c, "rejectedExecution", Runnable.class, ThreadPoolExecutor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            final boolean[] ran = new boolean[1];
            ThreadPoolExecutor.CallerRunsPolicy policy = new ThreadPoolExecutor.CallerRunsPolicy();
            policy.rejectedExecution(() -> { ran[0] = true; }, exec);
            check(ran[0], "CallerRunsPolicy runs the task in the caller thread");
        } finally {
            exec.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
