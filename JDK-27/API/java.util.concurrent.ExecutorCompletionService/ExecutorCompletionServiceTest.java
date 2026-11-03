/*
 * @test
 * @summary java.util.concurrent.ExecutorCompletionService conformance: white-box structure + black-box behavior.
 * @run main ExecutorCompletionServiceTest
 */

import java.util.*;
import java.util.concurrent.*;

public class ExecutorCompletionServiceTest {
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
        Class<?> c = cls("java.util.concurrent.ExecutorCompletionService");
        check(!c.isInterface(), "class");
        check(CompletionService.class.isAssignableFrom(c), "implements CompletionService");
        check(c.getConstructor(Executor.class) != null, "ctor (Executor)");
        check(c.getConstructor(Executor.class, BlockingQueue.class) != null, "ctor (Executor, BlockingQueue)");
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "take");
        hasMethod(c, "poll");
        hasMethod(c, "poll", long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ExecutorCompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        try {
            cs.submit(() -> 9);
            check(cs.take().get() == 9, "take returns completed task");
            check(cs.poll() == null, "poll empty returns null");
            cs.submit(() -> { Thread.sleep(150); return 1; });
            cs.submit(() -> 2);
            check(cs.take().get() == 2, "take returns first completed (order by completion)");
            check(cs.take().get() == 1, "take returns remaining completed");
            cs.submit(() -> { }, 42);
            check(cs.take().get() == 42, "submit(Runnable, result) returns result");
        } finally {
            pool.shutdown();
        }
        check(pool.awaitTermination(10, TimeUnit.SECONDS), "executor terminated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
