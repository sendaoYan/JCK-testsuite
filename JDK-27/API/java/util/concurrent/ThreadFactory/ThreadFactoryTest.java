/*
 * @test
 * @summary java.util.concurrent.ThreadFactory conformance: white-box structure + black-box
 *          behavior (newThread naming/daemon/not-started).
 * @run main ThreadFactoryTest
 */

import java.util.concurrent.Executors;
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

    // ---- Part 3: Executors.defaultThreadFactory product contract ----
    static void testDefaultThreadFactory() {
        ThreadFactory tf = Executors.defaultThreadFactory();
        Thread t1 = tf.newThread(() -> { });
        Thread t2 = tf.newThread(() -> { });
        check(t1 != null && t2 != null, "defaultThreadFactory returns threads");
        check(t1.getClass() == Thread.class, "product is a plain java.lang.Thread");
        check(!t1.isDaemon() && !t2.isDaemon(), "default factory threads are non-daemon");
        check(t1.getPriority() == Thread.NORM_PRIORITY, "default priority is NORM_PRIORITY");
        check(t1.getState() == Thread.State.NEW, "returned thread is not started");
        check(t1.getName().matches("pool-\\d+-thread-\\d+"),
              "name matches 'pool-N-thread-M' pattern: " + t1.getName());
        check(t2.getName().matches("pool-\\d+-thread-\\d+"), "second name matches the pattern");
        check(!t1.getName().equals(t2.getName()), "distinct products get distinct names");
        String[] p1 = t1.getName().split("-");
        String[] p2 = t2.getName().split("-");
        check(p1[1].equals(p2[1]) && Integer.parseInt(p2[3]) == Integer.parseInt(p1[3]) + 1,
              "same factory increments the thread number");
        // empirically-locked behaviour: a null Runnable is wrapped, not rejected
        Thread nullTask = tf.newThread(null);
        check(nullTask != null, "defaultThreadFactory().newThread(null) returns a Thread (实测)");
        ThreadFactory other = Executors.defaultThreadFactory();
        Thread t3 = other.newThread(() -> { });
        check(t3.getName().matches("pool-\\d+-thread-\\d+"), "second factory name pattern");
    }

    // ---- Part 4: annotation fact + factory decorator composition ----
    static void testAnnotationAndComposition() {
        check(!cls("java.util.concurrent.ThreadFactory").isAnnotationPresent(FunctionalInterface.class),
              "functional by shape only: no @FunctionalInterface annotation");
        final java.util.concurrent.atomic.AtomicInteger made =
            new java.util.concurrent.atomic.AtomicInteger();
        final ThreadFactory base = Executors.defaultThreadFactory();
        ThreadFactory counting = r -> {
            made.incrementAndGet();
            return base.newThread(r);
        };
        counting.newThread(() -> { });
        counting.newThread(() -> { });
        check(made.get() == 2, "decorating factory produced 2 threads");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDefaultThreadFactory();
        testAnnotationAndComposition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
