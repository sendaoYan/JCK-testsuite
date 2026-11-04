/*
 * @test
 * @summary java.util.concurrent.CompletionStage API conformance: white-box method face +
 *          black-box behavior (all combinators driven through the interface type,
 *          backed by CompletableFuture; sync/async equivalence; toCompletableFuture).
 * @run main CompletionStageTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CompletionStageTest {
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

    // ---- Part 1: white-box method face (per javap of JDK 8) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.CompletionStage");
        check(c.isInterface(), "CompletionStage is an interface");
        hasMethod(c, "thenApply", java.util.function.Function.class);
        hasMethod(c, "thenApplyAsync", java.util.function.Function.class);
        hasMethod(c, "thenApplyAsync", java.util.function.Function.class, java.util.concurrent.Executor.class);
        hasMethod(c, "thenAccept", java.util.function.Consumer.class);
        hasMethod(c, "thenRun", Runnable.class);
        hasMethod(c, "thenCombine", CompletionStage.class, java.util.function.BiFunction.class);
        hasMethod(c, "thenAcceptBoth", CompletionStage.class, java.util.function.BiConsumer.class);
        hasMethod(c, "runAfterBoth", CompletionStage.class, Runnable.class);
        hasMethod(c, "applyToEither", CompletionStage.class, java.util.function.Function.class);
        hasMethod(c, "acceptEither", CompletionStage.class, java.util.function.Consumer.class);
        hasMethod(c, "runAfterEither", CompletionStage.class, Runnable.class);
        hasMethod(c, "thenCompose", java.util.function.Function.class);
        hasMethod(c, "thenComposeAsync", java.util.function.Function.class, java.util.concurrent.Executor.class);
        hasMethod(c, "handle", java.util.function.BiFunction.class);
        hasMethod(c, "handleAsync", java.util.function.BiFunction.class, java.util.concurrent.Executor.class);
        hasMethod(c, "whenComplete", java.util.function.BiConsumer.class);
        hasMethod(c, "whenCompleteAsync", java.util.function.BiConsumer.class);
        hasMethod(c, "exceptionally", java.util.function.Function.class);
        hasMethod(c, "toCompletableFuture");
    }

    // ---- Part 2: black-box (behavioral, all through the interface type) ----
    static void testInterfaceDriving() throws Exception {
        CompletionStage<Integer> stage = CompletableFuture.completedFuture(21);
        CompletionStage<String> mapped = stage.thenApply(x -> "v" + (x * 2));
        check("v42".equals(mapped.toCompletableFuture().get()), "thenApply transforms the value");
        check(mapped.toCompletableFuture() instanceof CompletableFuture,
                "toCompletableFuture() on a CompletableFuture-backed stage yields a CompletableFuture");

        // toCompletableFuture() on the SAME CompletableFuture returns this
        CompletableFuture<Integer> cf = new CompletableFuture<>();
        check(cf.toCompletableFuture() == cf, "toCompletableFuture returns the same instance");
        check(!cf.isDone(), "stage incomplete before completion");
        cf.complete(1);
        check(cf.toCompletableFuture().join().intValue() == 1, "stage value after completion");
    }

    static void testSyncAsyncEquivalence() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1, new java.util.concurrent.ThreadFactory() {
            final AtomicInteger n = new AtomicInteger();
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "cst-eq-" + n.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
        try {
            CompletionStage<Integer> src = CompletableFuture.completedFuture(5);
            Integer sync = src.thenApply(x -> x * 3).toCompletableFuture().join();
            Integer async = src.thenApplyAsync(x -> x * 3, pool).toCompletableFuture().join();
            Integer asyncNoExec = src.thenApplyAsync(x -> x * 3).toCompletableFuture().join();
            check(sync.intValue() == 15 && async.intValue() == 15 && asyncNoExec.intValue() == 15,
                    "thenApply / thenApplyAsync(executor) / thenApplyAsync are result-equivalent");

            // async combinator provably runs on the supplied executor
            final AtomicReference<String> thread = new AtomicReference<>();
            src.thenApplyAsync(x -> { thread.set(Thread.currentThread().getName()); return x; }, pool)
               .toCompletableFuture().join();
            check(thread.get() != null && thread.get().startsWith("cst-eq"),
                    "thenApplyAsync(stage, executor) runs on the executor thread: " + thread.get());
        } finally {
            pool.shutdown();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminated");
        }
    }

    static void testCombinators() throws Exception {
        CompletionStage<Integer> s = CompletableFuture.completedFuture(10);

        CompletionStage<Integer> composed = s.thenCompose(x -> CompletableFuture.completedFuture(x + 2));
        check(composed.toCompletableFuture().get().intValue() == 12, "thenCompose flattens a nested stage");

        CompletionStage<Integer> combined = s.thenCombine(CompletableFuture.completedFuture(4), Integer::sum);
        check(combined.toCompletableFuture().get().intValue() == 14, "thenCombine merges two stages");

        final java.util.List<Integer> both = new java.util.ArrayList<>();
        s.thenAcceptBoth(CompletableFuture.completedFuture(1), (a, b) -> { both.add(a); both.add(b); })
                .toCompletableFuture().get();
        check(both.size() == 2 && both.get(0) == 10 && both.get(1) == 1, "thenAcceptBoth gets both values");

        final AtomicInteger ran = new AtomicInteger();
        s.runAfterBoth(CompletableFuture.completedFuture(2), ran::incrementAndGet)
                .toCompletableFuture().get();
        check(ran.get() == 1, "runAfterBoth runs after both stages complete");

        final AtomicReference<Integer> accepted = new AtomicReference<>();
        s.thenAccept(accepted::set).toCompletableFuture().get();
        check(accepted.get() != null && accepted.get() == 10, "thenAccept consumes the value");

        final AtomicInteger ran2 = new AtomicInteger();
        s.thenRun(ran2::incrementAndGet).toCompletableFuture().get();
        check(ran2.get() == 1, "thenRun runs the Runnable");

        // either-family: completed vs pending -> completed wins deterministically
        CompletionStage<Integer> pending = new CompletableFuture<Integer>();
        CompletionStage<Integer> done = CompletableFuture.completedFuture(3);
        check(done.applyToEither(pending, x -> x * 10).toCompletableFuture().join().intValue() == 30,
                "applyToEither picks the completed stage");
        final AtomicInteger eitherSeen = new AtomicInteger();
        done.acceptEither(pending, x -> eitherSeen.set(x + 1)).toCompletableFuture().get();
        check(eitherSeen.get() == 4, "acceptEither picks the completed stage");
        check(done.runAfterEither(pending, () -> { }).toCompletableFuture().get() == null,
                "runAfterEither fires for the completed stage");

        // chained default-method pipeline keeps interface typing
        CompletionStage<String> chained = CompletableFuture.completedFuture(1)
                .thenApply(x -> x + 1)
                .thenApply(x -> x * 5)
                .thenCompose(x -> CompletableFuture.completedFuture("r" + x));
        check("r10".equals(chained.toCompletableFuture().get()), "chained interface pipeline");
    }

    static void testExceptionPath() throws Exception {
        IllegalStateException boom = new IllegalStateException("stageBoom");
        CompletableFuture<Integer> failed = new CompletableFuture<>();
        CompletionStage<Integer> failedStage = failed;

        CompletionStage<Integer> recovered = failedStage.exceptionally(t -> 99);
        failed.completeExceptionally(boom);
        check(recovered.toCompletableFuture().get().intValue() == 99, "exceptionally recovers a failed stage");

        final AtomicReference<Throwable> seen = new AtomicReference<>();
        CompletableFuture<Integer> f2 = new CompletableFuture<>();
        f2.exceptionally(t -> { seen.set(t); return 0; });
        f2.completeExceptionally(boom);
        check(seen.get() == boom, "exceptionally receives the identical exception object");

        // handle sees both branches with exact inputs
        CompletionStage<Integer> hs = CompletableFuture.completedFuture(8)
                .handle((v, t) -> v != null && t == null ? v + 1 : 0);
        check(hs.toCompletableFuture().get().intValue() == 9, "handle success branch");
        CompletableFuture<Integer> hsrc = new CompletableFuture<>();
        CompletionStage<Integer> hf2 = hsrc.handle((v, t) -> v == null && t instanceof ArithmeticException ? 5 : 0);
        hsrc.completeExceptionally(new ArithmeticException("ae"));
        check(hf2.toCompletableFuture().get().intValue() == 5, "handle failure branch sees the exception type");

        // whenComplete observes value and exception without transforming
        final Object[] wcSeen = new Object[2];
        CompletableFuture<Integer> wc1 = new CompletableFuture<>();
        wc1.whenComplete((v, t) -> { wcSeen[0] = v; wcSeen[1] = t; });
        wc1.complete(7);
        check(wcSeen[0] != null && (Integer) wcSeen[0] == 7 && wcSeen[1] == null,
                "whenComplete normal branch");
        CompletableFuture<Integer> wc2 = new CompletableFuture<>();
        final Throwable[] wc2Seen = new Throwable[1];
        CompletionStage<Integer> wc2r = wc2.whenComplete((v, t) -> wc2Seen[0] = t);
        wc2.completeExceptionally(boom);
        check(wc2Seen[0] == boom, "whenComplete failure branch identity");
        check(wc2r.toCompletableFuture().isCompletedExceptionally(),
                "whenComplete relays failure unchanged");

        // a failed stage's exception surfaces through join as CompletionException(cause)
        try {
            failedStage.toCompletableFuture().join();
            check(false, "join on failed stage should throw");
        } catch (CompletionException ce) {
            check(ce.getCause() == boom, "stage join wraps cause identically");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testInterfaceDriving();
        testSyncAsyncEquivalence();
        testCombinators();
        testExceptionPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
