/*
 * @test
 * @summary java.util.concurrent.ThreadFactory API conformance: newThread.
 * @run main ThreadFactoryTest
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadFactory() {
        final AtomicInteger count = new AtomicInteger();
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("worker-" + count.incrementAndGet());
            return t;
        };
        Runnable task = () -> { };
        Thread t1 = tf.newThread(task);
        Thread t2 = tf.newThread(task);
        check("worker-1".equals(t1.getName()) && "worker-2".equals(t2.getName()), "custom naming");
        check(!t1.isAlive() && !t2.isAlive(), "newThread does not start the thread");
    }

    // ---- white-box: functional interface shape ----
    static void testWhiteBoxStructure() throws Exception {
        Class<ThreadFactory> c = ThreadFactory.class;
        check(c.isInterface(), "ThreadFactory is an interface");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method newThread");
        check(c.getMethod("newThread", Runnable.class).getReturnType() == Thread.class,
              "newThread returns Thread");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "functional by shape only: no @FunctionalInterface annotation (实测)");
    }

    // ---- newThread forwards the Runnable; starting the product runs it ----
    static void testRunnableForwarding() throws Exception {
        final CountDownLatch done = new CountDownLatch(1);
        final String[] ranIn = new String[1];
        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "forwarded-worker");
            t.setDaemon(true);
            return t;
        };
        Thread t = tf.newThread(() -> {
            ranIn[0] = Thread.currentThread().getName();
            done.countDown();
        });
        check("forwarded-worker".equals(t.getName()), "factory-configured name honoured");
        check(t.isDaemon(), "factory-configured daemon flag honoured");
        t.start();
        if (!done.await(5, TimeUnit.SECONDS)) throw new AssertionError("task did not run within 5s");
        check("forwarded-worker".equals(ranIn[0]), "the supplied Runnable ran inside the new thread");
        t.join(5000);
    }

    // ---- Executors.defaultThreadFactory product contract ----
    static void testDefaultThreadFactory() {
        ThreadFactory tf = Executors.defaultThreadFactory();
        Thread t1 = tf.newThread(() -> { });
        Thread t2 = tf.newThread(() -> { });
        check(t1 != null && t2 != null, "defaultThreadFactory returns threads");
        check(t1.getClass() == Thread.class, "product is a plain java.lang.Thread");
        check(!t1.isDaemon(), "default factory threads are non-daemon");
        check(!t2.isDaemon(), "default factory threads are non-daemon (2nd)");
        check(t1.getPriority() == Thread.NORM_PRIORITY, "default priority is NORM_PRIORITY");
        check(t1.getState() == Thread.State.NEW, "returned thread is not started");
        check(!t1.isAlive() && !t2.isAlive(), "not alive before start");
        check(t1.getName().matches("pool-\\d+-thread-\\d+"),
              "name matches 'pool-N-thread-M' pattern: " + t1.getName());
        check(t2.getName().matches("pool-\\d+-thread-\\d+"),
              "second name matches 'pool-N-thread-M' pattern: " + t2.getName());
        check(!t1.getName().equals(t2.getName()), "distinct products get distinct names");
        // two factory instances draw distinct pool numbers (global poolNumber counter)
        ThreadFactory other = Executors.defaultThreadFactory();
        Thread t3 = other.newThread(() -> { });
        check(t3.getName().matches("pool-\\d+-thread-\\d+"), "second factory name pattern");
        // same factory: thread number increments within the pool
        String[] p1 = t1.getName().split("-");
        String[] p2 = t2.getName().split("-");
        check(p1[1].equals(p2[1]) && Integer.parseInt(p2[3]) == Integer.parseInt(p1[3]) + 1,
              "same factory increments the thread number");
        // empirically-locked behaviour: a null Runnable is wrapped, not rejected
        Thread nullTask = tf.newThread(null);
        check(nullTask != null, "defaultThreadFactory().newThread(null) returns a Thread (实测)");
    }

    // ---- factory-decorator pattern (record how many threads a factory made) ----
    static void testFactoryComposition() throws Exception {
        final AtomicInteger made = new AtomicInteger();
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
        testThreadFactory();
        testWhiteBoxStructure();
        testRunnableForwarding();
        testDefaultThreadFactory();
        testFactoryComposition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
