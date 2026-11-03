/*
 * @test
 * @summary java.util.concurrent.CompletionService conformance: white-box structure + black-box behavior.
 * @run main CompletionServiceTest
 */

import java.util.concurrent.*;

public class CompletionServiceTest {
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
        Class<?> c = cls("java.util.concurrent.CompletionService");
        check(c.isInterface(), "CompletionService is an interface");
        hasMethod(c, "submit", Callable.class);
        hasMethod(c, "submit", Runnable.class, Object.class);
        hasMethod(c, "take");
        hasMethod(c, "poll");
        hasMethod(c, "poll", long.class, TimeUnit.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CompletionService<Integer> cs = new ExecutorCompletionService<>(pool);
        try {
            cs.submit(() -> { Thread.sleep(150); return 1; });
            cs.submit(() -> 2);
            Future<Integer> first = cs.take();
            check(first.get() == 2, "take returns first completed task");
            Future<Integer> second = cs.take();
            check(second.get() == 1, "take returns second completed task");
            check(cs.poll() == null, "poll empty returns null");
            check(cs.poll(10, TimeUnit.MILLISECONDS) == null, "poll(timeout) empty returns null");
            cs.submit(() -> 3);
            Future<Integer> third = cs.poll(5, TimeUnit.SECONDS);
            check(third != null && third.get() == 3, "poll(timeout) returns completed task");
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
