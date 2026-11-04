/*
 * @test
 * @summary java.util.concurrent.Executor conformance: white-box structure + black-box behavior.
 * @run main ExecutorTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorTest {
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
        Class<?> c = cls("java.util.concurrent.Executor");
        check(c.isInterface(), "Executor is an interface");
        hasMethod(c, "execute", Runnable.class);
        // single abstract method -> functional interface
        check(c.getDeclaredMethods().length == 1, "exactly one abstract method (functional interface)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (a) a synchronous Executor implementation runs the task inline
        AtomicBoolean ran = new AtomicBoolean();
        Executor direct = new Executor() {
            public void execute(Runnable r) { r.run(); }
        };
        direct.execute(() -> ran.set(true));
        check(ran.get(), "execute runs the task");

        // (b) a real executor accepts a Runnable via execute() and runs it
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            AtomicBoolean done = new AtomicBoolean();
            Executor e = pool; // ExecutorService IS-A Executor
            e.execute(() -> done.set(true));
            pool.shutdown();
            check(pool.awaitTermination(10, TimeUnit.SECONDS), "executor terminates after shutdown");
            check(done.get(), "task executed by a real executor");
        } finally {
            pool.shutdownNow();
        }
    }

    // ---- Part 3: direct executor runs in the calling thread, preserving order ----
    static void testDirectExecutorContract() {
        final String mainName = Thread.currentThread().getName();
        final String[] runner = new String[1];
        Executor direct = new Executor() {
            public void execute(Runnable r) {
                runner[0] = Thread.currentThread().getName();
                r.run();
            }
        };
        direct.execute(() -> { });
        check(mainName.equals(runner[0]), "direct executor runs the task in the calling thread");

        java.util.List<Integer> order =
            java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>());
        Executor seq = Runnable::run;
        seq.execute(() -> order.add(1));
        seq.execute(() -> order.add(2));
        seq.execute(() -> order.add(3));
        check(order.size() == 3, "all three tasks executed");
        check(order.get(0) == 1 && order.get(1) == 2 && order.get(2) == 3,
              "direct executor preserves submission order");
    }

    // ---- Part 4: execute(null) throws NPE (spec + implementation) ----
    static void testExecuteNull() {
        java.util.concurrent.ThreadPoolExecutor pool = new java.util.concurrent.ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>());
        try {
            boolean npe = false;
            try { pool.execute(null); }
            catch (NullPointerException expected) { npe = true; }
            check(npe, "ThreadPoolExecutor.execute(null) throws NPE");
        } finally {
            pool.shutdownNow();
        }
        ExecutorService single = Executors.newSingleThreadExecutor();
        try {
            boolean npe = false;
            try { single.execute(null); }
            catch (NullPointerException expected) { npe = true; }
            check(npe, "ExecutorService.execute(null) throws NPE");
        } finally {
            single.shutdownNow();
        }
    }

    // ---- Part 5: hierarchy + annotation facts (实测) ----
    static void testHierarchyFacts() throws Exception {
        Class<?> c = cls("java.util.concurrent.Executor");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "functional by shape only: no @FunctionalInterface annotation");
        check(Executor.class.isAssignableFrom(java.util.concurrent.ScheduledExecutorService.class),
              "ScheduledExecutorService is a sub-interface of Executor");
        check(Executor.class.isAssignableFrom(java.util.concurrent.ForkJoinPool.class),
              "ForkJoinPool implements Executor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDirectExecutorContract();
        testExecuteNull();
        testHierarchyFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
