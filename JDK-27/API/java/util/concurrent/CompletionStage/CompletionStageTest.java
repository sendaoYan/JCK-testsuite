/*
 * @test
 * @summary java.util.concurrent.CompletionStage conformance: white-box structure + black-box
 *          behavior (thenApply/thenCompose/thenCombine/exceptionally/handle/whenComplete).
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

    // ---- Part 1: white-box (structural) ----
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
        // Java 9+ default methods on the interface
        hasMethod(c, "exceptionallyAsync", java.util.function.Function.class);
        hasMethod(c, "exceptionallyAsync", java.util.function.Function.class, java.util.concurrent.Executor.class);
        hasMethod(c, "exceptionallyCompose", java.util.function.Function.class);
        hasMethod(c, "exceptionallyComposeAsync", java.util.function.Function.class);
        hasMethod(c, "exceptionallyComposeAsync", java.util.function.Function.class, java.util.concurrent.Executor.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CompletionStage<Integer> stage = CompletableFuture.completedFuture(21);

        // thenApply transforms the value
        CompletionStage<String> mapped = stage.thenApply(x -> "v" + (x * 2));
        check("v42".equals(mapped.toCompletableFuture().get()), "thenApply transforms the value");

        // thenCompose flattens a nested stage
        CompletionStage<Integer> composed = stage.thenCompose(x -> CompletableFuture.completedFuture(x + 1));
        check(composed.toCompletableFuture().get() == 22, "thenCompose flattens");

        // thenCombine merges two stages
        CompletionStage<Integer> combined = stage.thenCombine(
                CompletableFuture.completedFuture(9), Integer::sum);
        check(combined.toCompletableFuture().get() == 30, "thenCombine merges");

        // thenAccept consumes the value (returns a Void stage)
        AtomicReference<String> accepted = new AtomicReference<>();
        CompletionStage<Void> v = stage.thenAccept(x -> accepted.set("got-" + x));
        check(v.toCompletableFuture().get() == null, "thenAccept returns null-valued stage");
        check("got-21".equals(accepted.get()), "thenAccept consumed the value");

        // thenRun runs a Runnable after completion
        AtomicReference<Boolean> ran = new AtomicReference<>(false);
        stage.thenRun(() -> ran.set(true)).toCompletableFuture().get();
        check(ran.get(), "thenRun runs the Runnable");

        // exceptionally recovers a failed stage
        CompletionStage<Integer> failed = new CompletableFuture<>();
        CompletionStage<Integer> recovered = failed.exceptionally(t -> 99);
        ((CompletableFuture<Integer>) failed).completeExceptionally(new IllegalStateException("x"));
        check(recovered.toCompletableFuture().get() == 99, "exceptionally recovers");

        // handle sees the value/exception
        CompletionStage<String> handled = stage.handle((value, error) -> value == 21 ? "ok" : "bad");
        check("ok".equals(handled.toCompletableFuture().get()), "handle observes the value");

        // whenComplete observes value
        AtomicReference<Integer> wc = new AtomicReference<>();
        stage.whenComplete((value, error) -> wc.set(value)).toCompletableFuture().get();
        check(wc.get() == 21, "whenComplete observes the value");
    }

    // ---- Part 3: contract deep-dive ----
    static void testInterfaceDriving() throws Exception {
        CompletionStage<Integer> stage = CompletableFuture.completedFuture(21);
        check(stage.toCompletableFuture() instanceof CompletableFuture,
                "toCompletableFuture() yields a CompletableFuture");
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

        check(s.thenCompose(x -> CompletableFuture.completedFuture(x + 2))
                  .toCompletableFuture().get().intValue() == 12, "thenCompose flattens");
        check(s.thenCombine(CompletableFuture.completedFuture(4), Integer::sum)
                  .toCompletableFuture().get().intValue() == 14, "thenCombine merges");

        final java.util.List<Integer> both = new java.util.ArrayList<>();
        s.thenAcceptBoth(CompletableFuture.completedFuture(1), (a, b) -> { both.add(a); both.add(b); })
                .toCompletableFuture().get();
        check(both.size() == 2 && both.get(0) == 10 && both.get(1) == 1, "thenAcceptBoth gets both values");

        final AtomicInteger ran = new AtomicInteger();
        s.runAfterBoth(CompletableFuture.completedFuture(2), ran::incrementAndGet)
                .toCompletableFuture().get();
        check(ran.get() == 1, "runAfterBoth runs after both stages complete");

        CompletionStage<Integer> pending = new CompletableFuture<Integer>();
        CompletionStage<Integer> done = CompletableFuture.completedFuture(3);
        check(done.applyToEither(pending, x -> x * 10).toCompletableFuture().join().intValue() == 30,
                "applyToEither picks the completed stage");
        final AtomicInteger eitherSeen = new AtomicInteger();
        done.acceptEither(pending, x -> eitherSeen.set(x + 1)).toCompletableFuture().get();
        check(eitherSeen.get() == 4, "acceptEither picks the completed stage");
        check(done.runAfterEither(pending, () -> { }).toCompletableFuture().get() == null,
                "runAfterEither fires for the completed stage");

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

        CompletionStage<Integer> hs = CompletableFuture.completedFuture(8)
                .handle((v, t) -> v != null && t == null ? v + 1 : 0);
        check(hs.toCompletableFuture().get().intValue() == 9, "handle success branch");
        CompletableFuture<Integer> hsrc = new CompletableFuture<>();
        CompletionStage<Integer> hf2 = hsrc.handle((v, t) -> v == null && t instanceof ArithmeticException ? 5 : 0);
        hsrc.completeExceptionally(new ArithmeticException("ae"));
        check(hf2.toCompletableFuture().get().intValue() == 5, "handle failure branch sees the exception type");

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

        try {
            failedStage.toCompletableFuture().join();
            check(false, "join on failed stage should throw");
        } catch (CompletionException ce) {
            check(ce.getCause() == boom, "stage join wraps cause identically");
        }
    }

    // ---- Part 4: Java 9+ default methods (JDK-27 specifics) ----
    static void testJava9DefaultMethods() throws Exception {
        // exceptionallyAsync: recovery on an async path
        CompletableFuture<Integer> exf = new CompletableFuture<>();
        exf.completeExceptionally(new IllegalStateException("ea"));
        CompletionStage<Integer> ea = exf.exceptionallyAsync(t -> 5);
        check(ea.toCompletableFuture().get(5, TimeUnit.SECONDS).intValue() == 5,
                "exceptionallyAsync recovers");

        // with a custom executor the recovery runs on that executor
        ExecutorService pool = Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r, "cst-j9");
            t.setDaemon(true);
            return t;
        });
        try {
            final AtomicReference<String> tn = new AtomicReference<>();
            CompletableFuture<Integer> exf2 = new CompletableFuture<>();
            exf2.completeExceptionally(new IllegalStateException("ea2"));
            exf2.exceptionallyAsync(t -> { tn.set(Thread.currentThread().getName()); return 6; }, pool)
                .toCompletableFuture().get(5, TimeUnit.SECONDS);
            check(tn.get() != null && tn.get().startsWith("cst-j9"),
                    "exceptionallyAsync(fn, executor) recovery runs on the executor");

            // default methods are NOT invoked on the normal path (counter)
            final AtomicInteger calls = new AtomicInteger();
            CompletableFuture.completedFuture(1)
                    .exceptionallyAsync(t -> { calls.incrementAndGet(); return 0; }, pool)
                    .toCompletableFuture().join();
            check(calls.get() == 0, "exceptionallyAsync not invoked on normal completion");
        } finally {
            pool.shutdown();
            check(pool.awaitTermination(5, TimeUnit.SECONDS), "pool terminated");
        }

        // exceptionallyCompose: flattens a recovery stage
        CompletableFuture<Integer> exc = new CompletableFuture<>();
        exc.completeExceptionally(new IllegalStateException("ec"));
        check(exc.exceptionallyCompose(t -> CompletableFuture.completedFuture(6))
                  .toCompletableFuture().join().intValue() == 6, "exceptionallyCompose flattens");

        // exceptionallyComposeAsync via the interface type
        CompletableFuture<Integer> exc2 = new CompletableFuture<>();
        exc2.completeExceptionally(new IllegalStateException("ec2"));
        CompletionStage<Integer> eca = exc2.exceptionallyComposeAsync(t -> CompletableFuture.completedFuture(7));
        check(eca.toCompletableFuture().get(5, TimeUnit.SECONDS).intValue() == 7,
                "exceptionallyComposeAsync flattens");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceDriving();
        testSyncAsyncEquivalence();
        testCombinators();
        testExceptionPath();
        testJava9DefaultMethods();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
