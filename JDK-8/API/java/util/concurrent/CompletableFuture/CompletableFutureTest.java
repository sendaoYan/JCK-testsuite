/*
 * @test
 * @summary java.util.concurrent.CompletableFuture conformance: white-box structure + black-box
 *          behavior (supply/thenApply/thenCompose/thenCombine/thenAccept/thenRun/
 *          applyToEither/exceptionally/handle/whenComplete/join/allOf/anyOf/getNow/
 *          complete/obtrude/cancel/timed get).
 * @run main CompletableFutureTest
 */

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CompletableFutureTest {
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                    + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.CompletableFuture");
        check(!c.isInterface(), "CompletableFuture is a class");
        Class<?>[] ifs = c.getInterfaces();
        boolean future = false, stage = false;
        for (Class<?> i : ifs) {
            if (i == Future.class) future = true;
            if (i == CompletionStage.class) stage = true;
        }
        check(future, "implements Future");
        check(stage, "implements CompletionStage");
        hasMethod(c, "completedFuture", Object.class);
        hasMethod(c, "supplyAsync", java.util.function.Supplier.class);
        hasMethod(c, "runAsync", Runnable.class);
        hasMethod(c, "allOf", CompletableFuture[].class);
        hasMethod(c, "anyOf", CompletableFuture[].class);
        hasMethod(c, "thenApply", java.util.function.Function.class);
        hasMethod(c, "thenCompose", java.util.function.Function.class);
        hasMethod(c, "thenCombine", CompletionStage.class, java.util.function.BiFunction.class);
        hasMethod(c, "exceptionally", java.util.function.Function.class);
        hasMethod(c, "join");
        hasMethod(c, "getNow", Object.class);
        hasMethod(c, "complete", Object.class);
        hasMethod(c, "completeExceptionally", Throwable.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // synchronous pipeline: thenApply -> thenCompose -> thenCombine -> thenApply
        CompletableFuture<Integer> base = CompletableFuture.completedFuture(2);
        CompletableFuture<Integer> chain = base
                .thenApply(x -> x + 1)                    // 3
                .thenCompose(x -> CompletableFuture.completedFuture(x * 10)) // 30
                .thenCombine(CompletableFuture.completedFuture(5), Integer::sum) // 35
                .thenApply(x -> x * 2);                   // 70
        check(chain.join() == 70, "thenApply/thenCompose/thenCombine pipeline");

        // completedFuture / get / join / isDone / isCancelled
        CompletableFuture<Integer> done = CompletableFuture.completedFuture(42);
        check(done.isDone() && !done.isCancelled(), "completedFuture is done, not cancelled");
        check(done.get() == 42, "get");
        check(done.join() == 42, "join");

        // exceptionally recovery
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        CompletableFuture<Integer> recovered = failed.exceptionally(t -> 42);
        failed.completeExceptionally(new IllegalStateException("boom"));
        check(failed.isCompletedExceptionally(), "isCompletedExceptionally");
        check(recovered.join() == 42, "exceptionally recovers a value");

        // handle sees both value and exception paths
        CompletableFuture<Integer> h = CompletableFuture.completedFuture(10).handle((v, t) -> v * 3);
        check(h.join() == 30, "handle on success path");
        CompletableFuture<Object> fh = new CompletableFuture<>();
        CompletableFuture<String> h2 = fh.handle((v, t) -> t == null ? "none" : t.getClass().getSimpleName());
        fh.completeExceptionally(new IllegalStateException("x"));
        check("IllegalStateException".equals(h2.join()), "handle on failure path");

        // whenComplete observes the value
        AtomicReference<Integer> seen = new AtomicReference<>();
        CompletableFuture.completedFuture(7).whenComplete((v, t) -> seen.set(v));
        check(seen.get() == 7, "whenComplete observes the result");

        // thenAccept / thenRun side effects
        AtomicReference<Integer> accepted = new AtomicReference<>();
        CompletableFuture.completedFuture(5).thenAccept(accepted::set);
        check(accepted.get() == 5, "thenAccept observes the result");
        AtomicInteger ran = new AtomicInteger();
        CompletableFuture.completedFuture(1).thenRun(() -> ran.incrementAndGet());
        check(ran.get() == 1, "thenRun executes the Runnable");

        // applyToEither uses whichever completes first
        CompletableFuture<Integer> left = new CompletableFuture<>();
        CompletableFuture<Integer> right = new CompletableFuture<>();
        CompletableFuture<Integer> either = left.applyToEither(right, x -> x * 2);
        right.complete(21);
        check(either.join() == 42, "applyToEither uses first-completed input");

        // allOf / anyOf
        CompletableFuture<Integer> a = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> b = CompletableFuture.completedFuture(2);
        CompletableFuture<Void> all = CompletableFuture.allOf(a, b);
        check(all.isDone() && all.join() == null, "allOf completes when all inputs complete");
        CompletableFuture<Object> any = CompletableFuture.anyOf(a, b);
        check(any.join() != null, "anyOf completes with one input's value");

        // getNow / complete
        CompletableFuture<Integer> pending = new CompletableFuture<>();
        check(pending.getNow(-1) == -1, "getNow returns default while incomplete");
        check(pending.complete(99), "complete returns true and completes");
        check(pending.getNow(-1) == 99, "getNow returns value once complete");
        check(!pending.complete(100), "complete returns false when already complete");
        check(pending.join() == 99, "value unchanged by duplicate complete");

        // complete(null) is a valid completion: get returns null, isDone true
        CompletableFuture<Object> nullV = new CompletableFuture<>();
        check(nullV.complete(null), "complete(null) returns true");
        check(nullV.isDone() && nullV.join() == null, "complete(null) yields null result");

        // join on a failed future throws CompletionException
        CompletableFuture<Object> f2 = new CompletableFuture<>();
        f2.completeExceptionally(new IllegalStateException("kaboom"));
        try {
            f2.join();
            check(false, "join on failed future should throw");
        } catch (CompletionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "join wraps the cause in CompletionException");
        }

        // get on a failed future throws ExecutionException
        try {
            f2.get();
            check(false, "get on failed future should throw");
        } catch (ExecutionException expected) {
            check(expected.getCause() instanceof IllegalStateException, "get wraps the cause in ExecutionException");
        }

        // get(timeout) on an incomplete future throws TimeoutException
        CompletableFuture<Integer> stuck = new CompletableFuture<>();
        expectThrows(TimeoutException.class, () -> stuck.get(1, TimeUnit.MILLISECONDS),
                "get(timeout) throws TimeoutException");

        // obtrudeValue / obtrudeException override even completed results
        CompletableFuture<Integer> ob = CompletableFuture.completedFuture(1);
        ob.obtrudeValue(2);
        check(ob.join() == 2, "obtrudeValue overrides completed result");
        ob.obtrudeException(new IllegalStateException("over"));
        check(ob.isCompletedExceptionally(), "obtrudeException overrides value with exception");

        // cancel a pending future
        CompletableFuture<Integer> cxl = new CompletableFuture<>();
        check(cxl.cancel(true), "cancel returns true on pending");
        check(cxl.isCancelled() && cxl.isDone(), "cancelled and done");
        try {
            cxl.join();
            check(false, "join on cancelled should throw");
        } catch (CancellationException expected) {
            check(true, "join on cancelled throws CancellationException");
        }

        // supplyAsync with an explicit executor
        ExecutorService ex = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<Integer> sf = CompletableFuture.supplyAsync(() -> 6 * 7, ex);
            check(sf.join() == 42, "supplyAsync with explicit executor");
        } finally {
            ex.shutdown();
            ex.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
