/*
 * @test
 * @summary java.util.concurrent.CompletableFuture$AsynchronousCompletionTask conformance:
 *          marker interface (structure, zero method surface, instanceof, usable with async factories).
 * @run main AsynchronousCompletionTaskTest
 */

import java.util.concurrent.*;

public class AsynchronousCompletionTaskTest {
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
        Class<?> c = cls("java.util.concurrent.CompletableFuture$AsynchronousCompletionTask");
        check(c.isInterface(), "AsynchronousCompletionTask is an interface");
        check(c.getMethods().length == 0, "marker interface has no methods");
        check(c.getInterfaces().length == 0, "marker interface extends nothing");
    }

    static class MarkerExec implements Executor, CompletableFuture.AsynchronousCompletionTask {
        final java.util.concurrent.atomic.AtomicInteger executions = new java.util.concurrent.atomic.AtomicInteger();
        public void execute(Runnable r) {
            executions.incrementAndGet();
            r.run();
        }
    }

    /** a CompletableFuture subclass implementing the marker interface: legal, purely a tag */
    static class MarkedFuture<T> extends CompletableFuture<T>
            implements CompletableFuture.AsynchronousCompletionTask { }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(CompletableFuture.AsynchronousCompletionTask.class.isInterface(), "is an interface");
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 1, new MarkerExec());
        check(cf.join() == 1, "supplyAsync with a marked executor completes");
    }

    // ---- Part 3: contract deep-dive ----
    static void testMarkerSemantics() throws Exception {
        MarkerExec marked = new MarkerExec();
        check(marked instanceof CompletableFuture.AsynchronousCompletionTask,
                "marked executor instanceof AsynchronousCompletionTask");
        Object plain = Executors.newSingleThreadExecutor();
        try {
            check(!(plain instanceof CompletableFuture.AsynchronousCompletionTask),
                    "plain executor does not carry the marker");
        } finally {
            ((ExecutorService) plain).shutdown();
        }

        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 21, marked);
        check(f.get(5, TimeUnit.SECONDS).intValue() == 21, "result via marked executor");
        check(marked.executions.get() >= 1, "execute() was invoked by supplyAsync");

        CompletableFuture<Void> r = CompletableFuture.runAsync(() -> { }, marked);
        check(r.get(5, TimeUnit.SECONDS) == null, "runAsync via marked executor yields null");
        check(!f.isCompletedExceptionally(), "no exceptional completion introduced by the marker");

        MarkedFuture<Integer> mf = new MarkedFuture<>();
        check(mf instanceof CompletableFuture, "marked subclass is still a CompletableFuture");
        check(mf instanceof CompletableFuture.AsynchronousCompletionTask, "carries the marker");
        check(!mf.isDone(), "marked subclass starts incomplete");
        check(mf.complete(9), "marked subclass completes normally");
        check(mf.join().intValue() == 9, "marked subclass join");
        check(mf.thenApply(x -> x + 1).join().intValue() == 10,
                "dependent stages work on marked subclass");

        check(CompletableFuture.AsynchronousCompletionTask.class.isAssignableFrom(MarkerExec.class),
                "isAssignableFrom marked executor");
        check(!CompletableFuture.AsynchronousCompletionTask.class.isAssignableFrom(String.class),
                "not assignable from untagged class");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
