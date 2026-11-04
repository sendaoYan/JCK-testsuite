/*
 * @test
 * @summary java.util.concurrent.CompletableFuture$AsynchronousCompletionTask conformance: white-box structure + black-box behavior.
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
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(CompletableFuture.AsynchronousCompletionTask.class.isInterface(), "is an interface");
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> 1, new MarkerExec());
        check(cf.join() == 1, "supplyAsync with a marked executor completes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static class MarkerExec implements Executor, CompletableFuture.AsynchronousCompletionTask {
        public void execute(Runnable r) { r.run(); }
    }}
