/*
 * @test
 * @summary java.util.concurrent.Executor API conformance: execute runs a task.
 * @run main ExecutorTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExecutorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testExecutor() {
        AtomicBoolean ran = new AtomicBoolean();
        Executor e = new Executor() {
            public void execute(Runnable r) {
                r.run();
            }
        };
        e.execute(() -> ran.set(true));
        check(ran.get(), "execute runs the task");
    }

    // ---- white-box: interface shape and sub-interface hierarchy ----
    static void testWhiteBoxStructure() throws Exception {
        Class<Executor> c = Executor.class;
        check(c.isInterface(), "Executor is an interface");
        check(c.getDeclaredMethods().length == 1,
              "exactly one declared method (no default methods in java.util.concurrent.Executor)");
        check(c.getMethod("execute", Runnable.class).getReturnType() == void.class,
              "execute(Runnable) returns void");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
              "functional by shape only: no @FunctionalInterface annotation (实测)");
        check(Executor.class.isAssignableFrom(ExecutorService.class),
              "ExecutorService is a sub-interface of Executor");
        check(Executor.class.isAssignableFrom(ScheduledExecutorService.class),
              "ScheduledExecutorService is a sub-interface of Executor");
        check(Executor.class.isAssignableFrom(ThreadPoolExecutor.class),
              "ThreadPoolExecutor implements Executor");
        check(Executor.class.isAssignableFrom(java.util.concurrent.ForkJoinPool.class),
              "ForkJoinPool implements Executor");
    }

    // ---- direct (caller-thread) executor probe ----
    static void testDirectExecutorRunsInCallerThread() {
        final String mainName = Thread.currentThread().getName();
        final String[] runner = new String[1];
        Executor direct = new Executor() {
            public void execute(Runnable r) {
                runner[0] = Thread.currentThread().getName();
                r.run();
            }
        };
        AtomicBoolean ran = new AtomicBoolean();
        direct.execute(() -> ran.set(true));
        check(ran.get(), "direct execute runs the task");
        check(mainName.equals(runner[0]),
              "direct executor runs the task in the calling thread");
    }

    // ---- ordering: a direct executor preserves submission order ----
    static void testDirectExecutorOrdering() {
        final java.util.List<Integer> order =
            java.util.Collections.synchronizedList(new java.util.ArrayList<Integer>());
        Executor direct = Runnable::run;
        direct.execute(() -> order.add(1));
        direct.execute(() -> order.add(2));
        direct.execute(() -> order.add(3));
        check(order.size() == 3, "all three tasks executed");
        check(order.get(0) == 1 && order.get(1) == 2 && order.get(2) == 3,
              "direct executor preserves submission order");
    }

    // ---- execute(null) throws NPE (spec + implementation) ----
    static void testExecuteNull() {
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
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

    // ---- lambda as the Executor implementation ----
    static void testLambdaExecutor() {
        final int[] count = new int[1];
        // Executor is a functional interface: an Executor accepting the task
        Executor counting = r -> { count[0]++; r.run(); };
        counting.execute(() -> { });
        check(count[0] == 1, "lambda Executor implementation invoked once");
    }

    // ---- a real pool still honours the Executor contract ----
    static void testPoolViaExecutorInterface() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            final AtomicBoolean done = new AtomicBoolean();
            Executor e = pool; // ExecutorService IS-A Executor
            e.execute(() -> done.set(true));
            pool.shutdown();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminates after shutdown");
            check(done.get(), "task executed by the real executor");
        } finally {
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testExecutor();
        testWhiteBoxStructure();
        testDirectExecutorRunsInCallerThread();
        testDirectExecutorOrdering();
        testExecuteNull();
        testLambdaExecutor();
        testPoolViaExecutorInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
