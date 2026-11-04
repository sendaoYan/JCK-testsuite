/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy conformance: white-box structure + black-box behavior.
 * @run main DiscardOldestPolicyTest
 */

import java.util.concurrent.*;

public class DiscardOldestPolicyTest {
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
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$DiscardOldestPolicy");
        check(!c.isInterface(), "class");
        check(RejectedExecutionHandler.class.isAssignableFrom(c), "implements RejectedExecutionHandler");
        check(c.getConstructor() != null, "no-arg ctor");
        hasMethod(c, "rejectedExecution", Runnable.class, ThreadPoolExecutor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(2));
        try {
            Runnable oldTask = () -> { };
            exec.getQueue().offer(oldTask);
            ThreadPoolExecutor.DiscardOldestPolicy policy = new ThreadPoolExecutor.DiscardOldestPolicy();
            policy.rejectedExecution(() -> { }, exec);
            check(!exec.getQueue().contains(oldTask), "oldest queued task discarded");
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
