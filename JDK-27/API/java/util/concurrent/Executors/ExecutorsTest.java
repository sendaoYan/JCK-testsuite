/*
 * @test
 * @summary java.util.concurrent.Executors conformance: white-box structure + black-box behavior
 *          (factory-created pools, ThreadFactory, Callable adapters, unconfigurable wrappers).
 * @run main ExecutorsTest
 */

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ExecutorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasStatic(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + n + " on " + c.getName(), e); }
        passed++;
    }

    static void runOne(ExecutorService ex, int expected) throws Exception {
        try {
            check(ex.submit((Callable<Integer>) () -> expected).get() == expected, "task result via " + ex.getClass().getSimpleName());
        } finally {
            ex.shutdown();
        }
        check(ex.awaitTermination(20, TimeUnit.SECONDS), "terminates: " + ex.getClass().getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.Executors");
        check(!c.isInterface() && java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Executors is a final class");
        check(c.getConstructors().length == 0, "no public constructor");
        hasStatic(c, "newFixedThreadPool", int.class);
        hasStatic(c, "newSingleThreadExecutor");
        hasStatic(c, "newCachedThreadPool");
        hasStatic(c, "newScheduledThreadPool", int.class);
        hasStatic(c, "newSingleThreadScheduledExecutor");
        hasStatic(c, "newWorkStealingPool");
        hasStatic(c, "defaultThreadFactory");
        hasStatic(c, "callable", Runnable.class);
        hasStatic(c, "callable", Runnable.class, Object.class);
        hasStatic(c, "unconfigurableExecutorService", ExecutorService.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        runOne(Executors.newSingleThreadExecutor(), 1);
        runOne(Executors.newFixedThreadPool(2), 2);
        runOne(Executors.newCachedThreadPool(), 3);
        runOne(Executors.newWorkStealingPool(), 4);

        // virtual-thread-per-task executor
        ExecutorService v = Executors.newVirtualThreadPerTaskExecutor();
        runOne(v, 5);

        // scheduled executor factory
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        try {
            check(sched.schedule((Callable<Integer>) () -> 99, 0, TimeUnit.MILLISECONDS).get() == 99, "scheduled result");
        } finally {
            sched.shutdown();
        }
        check(sched.awaitTermination(20, TimeUnit.SECONDS), "scheduled terminates");

        // defaultThreadFactory creates named, unstarted threads
        ThreadFactory tf = Executors.defaultThreadFactory();
        Thread t = tf.newThread(() -> { });
        check(t != null && t.getName() != null && t.getName().length() > 0, "default thread has a name");
        check(!t.isAlive(), "newThread does not start the thread");

        // Callable adapters
        final boolean[] ran = new boolean[1];
        Callable<Object> cv = Executors.callable((Runnable) () -> ran[0] = true);
        check(cv.call() == null, "callable(Runnable) returns null");
        check(ran[0], "callable(Runnable) runs the runnable");
        Callable<String> cr = Executors.callable(() -> { }, "hello");
        check("hello".equals(cr.call()), "callable(Runnable, T) returns T");

        // unconfigurable wrapper delegates execution
        ExecutorService inner = Executors.newSingleThreadExecutor();
        ExecutorService wrapped = Executors.unconfigurableExecutorService(inner);
        try {
            check(wrapped.submit((Callable<Integer>) () -> 8).get() == 8, "unconfigurable wrapper delegates");
        } finally {
            wrapped.shutdown();
        }
        check(wrapped.awaitTermination(20, TimeUnit.SECONDS), "wrapped terminates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
