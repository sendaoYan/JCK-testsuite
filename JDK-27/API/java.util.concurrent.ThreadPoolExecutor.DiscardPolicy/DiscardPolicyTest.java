/*
 * @test
 * @summary java.util.concurrent.ThreadPoolExecutor$DiscardPolicy conformance: white-box structure + black-box behavior.
 * @run main DiscardPolicyTest
 */

import java.util.concurrent.*;

public class DiscardPolicyTest {
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
        Class<?> c = cls("java.util.concurrent.ThreadPoolExecutor$DiscardPolicy");
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
            ThreadPoolExecutor.DiscardPolicy policy = new ThreadPoolExecutor.DiscardPolicy();
            policy.rejectedExecution(() -> { ran[0] = true; }, exec);
            check(!ran[0], "DiscardPolicy silently discards without running");
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
