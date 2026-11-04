/*
 * @test
 * @summary java.util.concurrent.CompletionStage conformance: white-box structure + black-box
 *          behavior (thenApply/thenCompose/thenCombine/exceptionally/handle/whenComplete).
 * @run main CompletionStageTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
        hasMethod(c, "thenAccept", java.util.function.Consumer.class);
        hasMethod(c, "thenRun", Runnable.class);
        hasMethod(c, "thenCombine", CompletionStage.class, java.util.function.BiFunction.class);
        hasMethod(c, "thenCompose", java.util.function.Function.class);
        hasMethod(c, "handle", java.util.function.BiFunction.class);
        hasMethod(c, "whenComplete", java.util.function.BiConsumer.class);
        hasMethod(c, "exceptionally", java.util.function.Function.class);
        hasMethod(c, "toCompletableFuture");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
