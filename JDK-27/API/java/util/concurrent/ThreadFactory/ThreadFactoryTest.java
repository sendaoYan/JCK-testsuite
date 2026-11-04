/*
 * @test
 * @summary java.util.concurrent.ThreadFactory conformance: white-box structure + black-box
 *          behavior (newThread naming/daemon/not-started).
 * @run main ThreadFactoryTest
 */

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ThreadFactory");
        check(c.isInterface(), "ThreadFactory is an interface");
        check(c.getDeclaredMethods().length == 1, "single abstract method (functional interface)");
        hasMethod(c, "newThread", Runnable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final AtomicInteger count = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("worker-" + count.incrementAndGet());
            t.setDaemon(false);
            return t;
        };
        Runnable task = () -> { };
        Thread t1 = tf.newThread(task);
        Thread t2 = tf.newThread(task);
        check("worker-1".equals(t1.getName()) && "worker-2".equals(t2.getName()), "custom naming sequence");
        check(!t1.isAlive() && !t2.isAlive(), "newThread does not start the thread");
        check(!t1.isDaemon(), "factory-configured daemon flag honoured");

        // newThread must pass the runnable through so starting it runs the task
        final boolean[] ran = new boolean[1];
        Thread t3 = tf.newThread(() -> ran[0] = true);
        t3.start();
        try {
            t3.join(5000);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        check(ran[0], "starting the returned thread runs the supplied Runnable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
