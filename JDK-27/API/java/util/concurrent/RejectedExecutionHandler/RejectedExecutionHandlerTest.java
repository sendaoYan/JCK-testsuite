/*
 * @test
 * @summary java.util.concurrent.RejectedExecutionHandler conformance: white-box structure + black-box behavior.
 * @run main RejectedExecutionHandlerTest
 */

import java.util.concurrent.*;

public class RejectedExecutionHandlerTest {
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
        Class<?> c = cls("java.util.concurrent.RejectedExecutionHandler");
        check(c.isInterface(), "RejectedExecutionHandler is an interface");
        hasMethod(c, "rejectedExecution", Runnable.class, ThreadPoolExecutor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ThreadPoolExecutor exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            final Runnable[] seen = new Runnable[1];
            RejectedExecutionHandler handler = (r, e) -> seen[0] = r;
            Runnable task = () -> { };
            handler.rejectedExecution(task, exec);
            check(seen[0] == task, "custom handler receives the rejected task");
        } finally {
            exec.shutdown();
        }
        // AbortPolicy is the default handler and throws
        ThreadPoolExecutor exec2 = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1));
        try {
            boolean threw = false;
            try {
                new ThreadPoolExecutor.AbortPolicy().rejectedExecution(() -> { }, exec2);
            } catch (RejectedExecutionException expected) { threw = true; }
            check(threw, "AbortPolicy throws RejectedExecutionException");
        } finally {
            exec2.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
