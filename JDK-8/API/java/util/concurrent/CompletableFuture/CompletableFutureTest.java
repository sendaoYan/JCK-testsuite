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
import java.util.concurrent.CountDownLatch;
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

    // ---- Part 3: contract deep-dive (state matrix / completion idempotence / cancel /
    //      join-vs-get wrapping / callback contracts / executor identity / either family) ----
    static ExecutorService newPool(final String prefix) {
        return Executors.newFixedThreadPool(2, new java.util.concurrent.ThreadFactory() {
            final AtomicInteger n = new AtomicInteger();
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, prefix + "-" + n.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
    }

    // four states x isDone/isCancelled/isCompletedExceptionally + terminal access behavior
    static void testStateMatrix() throws Exception {
        CompletableFuture<Integer> pending = new CompletableFuture<>();
        check(!pending.isDone() && !pending.isCancelled() && !pending.isCompletedExceptionally(),
                "pending: !isDone && !isCancelled && !isCompletedExceptionally");
        check(pending.getNow(-1) == -1, "pending getNow returns fallback");
        check(pending.getNumberOfDependents() == 0, "pending has no dependents");

        CompletableFuture<Integer> normal = CompletableFuture.completedFuture(7);
        check(normal.isDone() && !normal.isCancelled() && !normal.isCompletedExceptionally(),
                "normal: isDone && !isCancelled && !isCompletedExceptionally");
        check(normal.get().intValue() == 7 && normal.join().intValue() == 7
                && normal.getNow(-1).intValue() == 7, "normal: get/join/getNow all yield value");
        check(normal.get(1, TimeUnit.MILLISECONDS).intValue() == 7, "normal: timed get returns immediately");

        CompletableFuture<Integer> exceptional = new CompletableFuture<>();
        exceptional.completeExceptionally(new ArithmeticException("div0"));
        check(exceptional.isDone() && !exceptional.isCancelled() && exceptional.isCompletedExceptionally(),
                "exceptional: isDone && !isCancelled && isCompletedExceptionally");
        try {
            exceptional.getNow(-1);
            check(false, "exceptional getNow should throw CompletionException");
        } catch (CompletionException ce) {
            check(ce.getCause() instanceof ArithmeticException, "exceptional getNow wraps the cause");
        }

        CompletableFuture<Integer> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);
        check(cancelled.isDone() && cancelled.isCancelled() && cancelled.isCompletedExceptionally(),
                "cancelled: isDone && isCancelled && isCompletedExceptionally");
    }

    static void testCompletionIdempotence() {
        CompletableFuture<Integer> f = new CompletableFuture<>();
        check(f.complete(1), "first complete(v) returns true");
        check(!f.complete(2), "second complete(v) returns false");
        check(f.join().intValue() == 1, "value not replaced by second complete");

        CompletableFuture<Integer> g = new CompletableFuture<>();
        check(g.completeExceptionally(new IllegalStateException("e1")), "first completeExceptionally returns true");
        check(!g.completeExceptionally(new IllegalStateException("e2")), "second completeExceptionally returns false");
        check(!g.complete(9), "complete(v) after exceptional completion returns false");
        check(g.isCompletedExceptionally(), "state remains exceptional");

        // obtrude overrides everything, even completed results
        CompletableFuture<Integer> ob = CompletableFuture.completedFuture(1);
        ob.obtrudeValue(2);
        check(ob.join().intValue() == 2, "obtrudeValue replaces completed value");
        ob.obtrudeValue(3);
        check(ob.join().intValue() == 3, "obtrudeValue applies repeatedly");
        ArithmeticException ax = new ArithmeticException("ob");
        ob.obtrudeException(ax);
        check(ob.isCompletedExceptionally(), "obtrudeException overrides value completion");
        try {
            ob.join();
            check(false, "join after obtrudeException should throw");
        } catch (CompletionException ce) {
            check(ce.getCause() == ax, "obtrudeException exception object identity");
        }
        ob.obtrudeValue(4);
        check(ob.join().intValue() == 4 && !ob.isCompletedExceptionally(),
                "obtrudeValue overrides exceptional completion back to normal");
    }

    static void testCancelContract() throws Exception {
        CompletableFuture<Integer> c = new CompletableFuture<>();
        check(c.cancel(true), "cancel(true) on pending returns true");
        check(c.cancel(true), "repeat cancel(true) returns true (CF-specific contract)");
        check(c.cancel(false), "cancel(false) on cancelled returns true (CF-specific contract)");
        check(c.isCancelled() && c.isDone() && c.isCompletedExceptionally(),
                "cancelled state flags");
        try {
            c.get();
            check(false, "get on cancelled should throw");
        } catch (CancellationException expected) {
            check(expected.getMessage() == null, "CancellationException message is null on JDK 8");
        }
        try {
            c.join();
            check(false, "join on cancelled should throw");
        } catch (CancellationException expected) {
            check(true, "join on cancelled throws raw CancellationException (unwrapped)");
        }
        try {
            c.getNow(5);
            check(false, "getNow on cancelled should throw");
        } catch (CancellationException raw) {
            check(true, "getNow on cancelled throws raw CancellationException (not wrapped)");
        }

        // dependents observe CancellationException from the cancelled source
        CompletableFuture<Integer> src = new CompletableFuture<>();
        final Throwable[] seen = new Throwable[1];
        src.handle((v, t) -> { seen[0] = t; return 0; });
        src.cancel(true);
        check(seen[0] instanceof CancellationException, "dependent handle receives CancellationException");
        CompletableFuture<Integer> rec = src.exceptionally(t -> 11);
        check(rec.join().intValue() == 11, "exceptionally can recover a cancelled source");

        // cancel on already-completed / already-failed futures has no effect
        CompletableFuture<Integer> done = CompletableFuture.completedFuture(1);
        check(!done.cancel(true), "cancel on completed future returns false");
        check(done.join().intValue() == 1, "completed value survives cancel attempt");
        check(!done.isCancelled(), "completed future not flagged cancelled");
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        failed.completeExceptionally(new IllegalStateException("f"));
        check(!failed.cancel(true), "cancel on failed future returns false");
        check(!failed.isCancelled() && failed.isCompletedExceptionally(),
                "failed future stays exceptional, not cancelled");
    }

    static void testJoinVsGetWrapping() throws Exception {
        IllegalStateException orig = new IllegalStateException("orig");
        CompletableFuture<Object> f = new CompletableFuture<>();
        f.completeExceptionally(orig);
        try {
            f.join();
            check(false, "join should throw");
        } catch (CompletionException ce) {
            check(ce.getCause() == orig, "join wraps in CompletionException with cause == original object");
        }
        try {
            f.get();
            check(false, "get should throw");
        } catch (ExecutionException ee) {
            check(ee.getCause() == orig, "get wraps in ExecutionException with cause == original object");
        }
        try {
            f.get(1, TimeUnit.MILLISECONDS);
            check(false, "timed get should throw");
        } catch (ExecutionException ee) {
            check(ee.getCause() == orig, "timed get wraps in ExecutionException with same cause");
        }
        check(expectCE2(f), "getNow on exceptionally-completed throws CompletionException (not fallback)");
    }

    static boolean expectCE2(CompletableFuture<Object> f) {
        try {
            f.getNow("fb");
            return false;
        } catch (CompletionException ce) {
            return ce.getCause() instanceof IllegalStateException;
        }
    }

    static void testCallbackContracts() {
        // exceptionally: not invoked on the normal path; receives the exact exception object
        final AtomicInteger calls = new AtomicInteger();
        CompletableFuture.completedFuture(3).exceptionally(t -> { calls.incrementAndGet(); return 0; });
        check(calls.get() == 0, "exceptionally callback not invoked on normal completion");
        IllegalStateException boom = new IllegalStateException("cb");
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        final Throwable[] got = new Throwable[1];
        CompletableFuture<Integer> recovered = failed.exceptionally(t -> { got[0] = t; return 42; });
        failed.completeExceptionally(boom);
        check(got[0] == boom, "exceptionally receives the identical exception object");
        check(recovered.join().intValue() == 42 && !recovered.isCompletedExceptionally(),
                "exceptionally recovery value + normal completion of dependent");

        // handle: both branches receive exact inputs
        CompletableFuture<Integer> h1 = CompletableFuture.completedFuture(10);
        check(h1.handle((v, t) -> v != null && v == 10 && t == null ? 1 : 0).join().intValue() == 1,
                "handle success branch: value present, exception null");
        CompletableFuture<Integer> h2 = new CompletableFuture<>();
        IllegalStateException h2ex = new IllegalStateException("h2");
        CompletableFuture<Integer> h2r = h2.handle((v, t) -> v == null && t == h2ex ? 2 : 0);
        h2.completeExceptionally(h2ex);
        check(h2r.join().intValue() == 2, "handle failure branch: value null, exception identical");

        // whenComplete: observes both; callback throwing does not alter the result
        final Object[] wcSeen = new Object[2];
        CompletableFuture<Integer> wc1 = new CompletableFuture<>();
        wc1.whenComplete((v, t) -> { wcSeen[0] = v; wcSeen[1] = t; });
        wc1.complete(5);
        check(wcSeen[0] != null && (Integer) wcSeen[0] == 5 && wcSeen[1] == null,
                "whenComplete normal branch sees value, null exception");
        CompletableFuture<Integer> wc2 = new CompletableFuture<>();
        IllegalStateException wc2ex = new IllegalStateException("wc2");
        CompletableFuture<Integer> wc2r = wc2.whenComplete((v, t) -> { throw new RuntimeException("listenerBoom"); });
        wc2.completeExceptionally(wc2ex);
        check(wc2r.isCompletedExceptionally(), "whenComplete whose callback throws relays the ORIGINAL failure");
        try {
            wc2r.join();
            check(false, "expected CompletionException");
        } catch (CompletionException ce) {
            check(ce.getCause() == wc2ex, "whenComplete callback exception is discarded, original cause kept");
        }
        CompletableFuture<Integer> wc3 = new CompletableFuture<>();
        final RuntimeException cbEx = new RuntimeException("wc3cb");
        CompletableFuture<Integer> wc3r = wc3.whenComplete((v, t) -> { throw cbEx; });
        wc3.complete(6);
        try {
            wc3r.join();
            check(false, "callback exception must complete the dependent exceptionally");
        } catch (CompletionException ce) {
            check(ce.getCause() == cbEx, "whenComplete callback exception propagates when source was normal");
        }
        check(wc3.join().intValue() == 6, "source result unaffected by whenComplete callback exception");
    }

    static void testAsyncExecutorIdentity() throws Exception {
        ExecutorService pool = newPool("cft-async");
        try {
            final AtomicReference<String> applyThread = new AtomicReference<>();
            Integer v = CompletableFuture.completedFuture(1)
                    .thenApplyAsync(x -> { applyThread.set(Thread.currentThread().getName()); return x + 1; }, pool)
                    .join();
            check(v.intValue() == 2, "thenApplyAsync value passes through executor");
            check(applyThread.get() != null && applyThread.get().startsWith("cft-async"),
                    "thenApplyAsync callback ran on the supplied executor thread: " + applyThread.get());

            final AtomicReference<String> acceptThread = new AtomicReference<>();
            CompletableFuture.completedFuture(2)
                    .thenAcceptAsync(x -> acceptThread.set(Thread.currentThread().getName()), pool)
                    .join();
            check(acceptThread.get() != null && acceptThread.get().startsWith("cft-async"),
                    "thenAcceptAsync callback ran on the supplied executor thread");

            final AtomicReference<String> runThread = new AtomicReference<>();
            CompletableFuture.completedFuture(3)
                    .thenRunAsync(() -> runThread.set(Thread.currentThread().getName()), pool)
                    .join();
            check(runThread.get() != null && runThread.get().startsWith("cft-async"),
                    "thenRunAsync callback ran on the supplied executor thread");

            // three-layer async chain keeps exact values
            Integer deep = CompletableFuture.completedFuture(1)
                    .thenApplyAsync(x -> x + 1, pool)
                    .thenApplyAsync(x -> x * 10, pool)
                    .thenApplyAsync(x -> x - 2, pool)
                    .join();
            check(deep.intValue() == 18, "three-layer thenApplyAsync chain value 1+1*10-2 == 18");
        } finally {
            pool.shutdown();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminated");
        }
    }

    static void testSupplyAsyncControlled() throws Exception {
        ExecutorService pool = newPool("cft-supply");
        try {
            final CountDownLatch hold = new CountDownLatch(1);
            CompletableFuture<Integer> stuck = CompletableFuture.supplyAsync(() -> {
                try { hold.await(); } catch (InterruptedException ie) { /* bounded */ }
                return 42;
            }, pool);
            try {
                stuck.get(20, TimeUnit.MILLISECONDS);
                check(false, "held supplier must time out");
            } catch (TimeoutException te) {
                check(true, "get(timeout) on incomplete future throws TimeoutException");
            }
            check(!stuck.isDone(), "still incomplete while latch held");
            hold.countDown();
            check(stuck.get(5, TimeUnit.SECONDS).intValue() == 42, "value exact after release");

            // supplier throwing -> ExecutionException with identical cause
            IllegalStateException sex = new IllegalStateException("supplierBoom");
            CompletableFuture<Integer> throwing = CompletableFuture.supplyAsync(() -> { throw sex; }, pool);
            try {
                throwing.get(5, TimeUnit.SECONDS);
                check(false, "throwing supplier must fail");
            } catch (ExecutionException ee) {
                check(ee.getCause() == sex, "supplier exception object identity through ExecutionException");
            }

            CompletableFuture<Void> ran = CompletableFuture.runAsync(() -> { }, pool);
            check(ran.get(5, TimeUnit.SECONDS) == null, "runAsync result is null");
            check(ran.isDone(), "runAsync done");
        } finally {
            pool.shutdown();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminated");
        }
    }

    static void testEitherFamily() {
        // A already completed, B pending: A must win deterministically
        CompletableFuture<Integer> a = CompletableFuture.completedFuture(10);
        CompletableFuture<Integer> b = new CompletableFuture<>();
        check(a.applyToEither(b, x -> x + 1).join().intValue() == 11, "applyToEither takes completed input");
        final AtomicInteger got = new AtomicInteger();
        a.acceptEither(b, x -> got.set(x * 2)).join();
        check(got.get() == 20, "acceptEither takes completed input");
        check(a.runAfterEither(b, () -> { }).join() == null, "runAfterEither fires for completed input");
        // symmetric: pending A vs completed B
        CompletableFuture<Integer> p = new CompletableFuture<>();
        CompletableFuture<Integer> qb = CompletableFuture.completedFuture(3);
        check(p.applyToEither(qb, x -> x * 100).join().intValue() == 300,
                "applyToEither picks the completed side regardless of position");
    }

    static void testCombineFamily() {
        CompletableFuture<Integer> x = CompletableFuture.completedFuture(6);
        CompletableFuture<Integer> y = CompletableFuture.completedFuture(7);
        check(x.thenCombine(y, Integer::sum).join().intValue() == 13, "thenCombine merges both values");
        final java.util.List<Integer> both = new java.util.ArrayList<>();
        x.thenAcceptBoth(y, (m, n) -> { both.add(m); both.add(n); }).join();
        check(both.size() == 2 && both.get(0) == 6 && both.get(1) == 7,
                "thenAcceptBoth receives both values in order");
        check(x.runAfterBoth(y, () -> { }).join() == null, "runAfterBoth completes after both");
        // composed future flattening with a pending inner future completed later
        CompletableFuture<Integer> inner = new CompletableFuture<>();
        CompletableFuture<Integer> composed = CompletableFuture.completedFuture(1)
                .thenCompose(v -> inner);
        check(!composed.isDone(), "thenCompose dependent incomplete while inner incomplete");
        inner.complete(41);
        check(composed.join().intValue() == 41, "thenCompose adopts the inner future's result");
    }

    static void testAllOfAnyOf() throws Exception {
        CompletableFuture<Integer> a = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> b = CompletableFuture.completedFuture(2);
        CompletableFuture<Void> all = CompletableFuture.allOf(a, b);
        check(all.isDone() && all.join() == null, "allOf done and joins to null");
        check(CompletableFuture.allOf().isDone(), "allOf() with no inputs is already complete");

        IllegalStateException allEx = new IllegalStateException("allEx");
        CompletableFuture<Object> bad = new CompletableFuture<>();
        CompletableFuture<Void> allBad = CompletableFuture.allOf(CompletableFuture.completedFuture(1), bad);
        check(!allBad.isDone(), "allOf incomplete while one input incomplete");
        bad.completeExceptionally(allEx);
        check(allBad.isDone() && allBad.isCompletedExceptionally(), "allOf completes exceptionally");
        try {
            allBad.join();
            check(false, "allOf join should throw");
        } catch (CompletionException ce) {
            check(ce.getCause() == allEx, "allOf join cause identity");
        }

        // anyOf: completed input wins over pending input deterministically
        CompletableFuture<Object> pending = new CompletableFuture<>();
        CompletableFuture<Object> any = CompletableFuture.anyOf(CompletableFuture.completedFuture("A"), pending);
        check(any.isDone() && "A".equals(any.join()), "anyOf takes the completed input");
        check(!CompletableFuture.anyOf().isDone(), "anyOf() with no inputs never completes");
    }

    static void testToStringAndMisc() {
        String pend = new CompletableFuture<Integer>().toString();
        check(pend.contains("[Not completed]"), "pending toString contains [Not completed]: " + pend);
        String norm = CompletableFuture.completedFuture(42).toString();
        check(norm.contains("[Completed normally]"), "normal toString contains [Completed normally]: " + norm);
        CompletableFuture<Object> f = new CompletableFuture<>();
        f.completeExceptionally(new IllegalStateException("ts"));
        check(f.toString().contains("[Completed exceptionally"),
                "failed toString contains [Completed exceptionally]: " + f);
        check(f.toString().startsWith("java.util.concurrent.CompletableFuture@"),
                "toString prefixed by class@identity");

        CompletableFuture<Integer> self = new CompletableFuture<>();
        check(self.toCompletableFuture() == self, "toCompletableFuture returns this");
        CompletableFuture<Integer> dep = new CompletableFuture<>();
        dep.thenApply(v -> v);
        dep.thenApply(v -> v);
        check(dep.getNumberOfDependents() == 2, "getNumberOfDependents counts two stages");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStateMatrix();
        testCompletionIdempotence();
        testCancelContract();
        testJoinVsGetWrapping();
        testCallbackContracts();
        testAsyncExecutorIdentity();
        testSupplyAsyncControlled();
        testEitherFamily();
        testCombineFamily();
        testAllOfAnyOf();
        testToStringAndMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
