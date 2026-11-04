/*
 * @test
 * @summary java.util.concurrent.CompletableFuture.AsynchronousCompletionTask conformance:
 *          marker interface (structure, zero method surface, instanceof, usable with async factories).
 * @run main AsynchronousCompletionTaskTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousCompletionTaskTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MarkerExec implements Executor, CompletableFuture.AsynchronousCompletionTask {
        final AtomicInteger executions = new AtomicInteger();
        public void execute(Runnable r) {
            executions.incrementAndGet();
            r.run();
        }
    }

    /** a CompletableFuture subclass implementing the marker interface: legal, purely a tag */
    static class MarkedFuture<T> extends CompletableFuture<T>
            implements CompletableFuture.AsynchronousCompletionTask { }

    static void testMarker() throws Exception {
        check(CompletableFuture.AsynchronousCompletionTask.class.isInterface(), "is an interface");
        check(CompletableFuture.AsynchronousCompletionTask.class.getMethods().length == 0,
                "marker interface declares no methods");
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 1, new MarkerExec());
        check(cf.join().intValue() == 1, "supplyAsync with a marked executor");
    }

    static void testMarkerSemantics() throws Exception {
        // instanceof tagging of a user executor carrying the marker
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

        // the marker changes no behavior: tasks still run, results still flow
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> 21, marked);
        check(f.get(5, TimeUnit.SECONDS).intValue() == 21, "result via marked executor");
        check(marked.executions.get() >= 1, "execute() was invoked by supplyAsync");

        // runAsync uses the same executor path
        CompletableFuture<Void> r = CompletableFuture.runAsync(() -> { }, marked);
        check(r.get(5, TimeUnit.SECONDS) == null, "runAsync via marked executor yields null");
        check(!f.isCompletedExceptionally(), "no exceptional completion introduced by the marker");

        // a CompletableFuture subclass may implement the marker; CompletableFuture
        // behavior is unaffected (completion, dependent stages, join)
        MarkedFuture<Integer> mf = new MarkedFuture<>();
        check(mf instanceof CompletableFuture, "marked subclass is still a CompletableFuture");
        check(mf instanceof CompletableFuture.AsynchronousCompletionTask, "carries the marker");
        check(!mf.isDone(), "marked subclass starts incomplete");
        check(mf.complete(9), "marked subclass completes normally");
        check(mf.join().intValue() == 9, "marked subclass join");
        check(mf.thenApply(x -> x + 1).join().intValue() == 10,
                "dependent stages work on marked subclass");

        // the marker interface is assignable to plain interfaces only; it adds no
        // obligations - implementing it on an unrelated class compiles and runs
        check(CompletableFuture.AsynchronousCompletionTask.class.isAssignableFrom(MarkerExec.class),
                "isAssignableFrom marked executor");
        check(!CompletableFuture.AsynchronousCompletionTask.class.isAssignableFrom(String.class),
                "not assignable from untagged class");
    }

    public static void main(String[] args) throws Exception {
        testMarker();
        testMarkerSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
