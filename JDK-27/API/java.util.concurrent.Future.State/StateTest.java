/*
 * @test
 * @summary java.util.concurrent.Future$State conformance: white-box structure + black-box behavior.
 * @run main StateTest
 */

import java.util.concurrent.*;

public class StateTest {
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
        Class<?> c = cls("java.util.concurrent.Future$State");
        check(c.isEnum(), "Future.State is an enum");
        hasMethod(c, "values");
        hasMethod(c, "valueOf", String.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Future.State.values().length == 4, "four states");
        check(Future.State.valueOf("RUNNING") == Future.State.RUNNING, "valueOf RUNNING");
        check(Future.State.valueOf("SUCCESS") == Future.State.SUCCESS, "valueOf SUCCESS");
        check(Future.State.valueOf("FAILED") == Future.State.FAILED, "valueOf FAILED");
        check(Future.State.valueOf("CANCELLED") == Future.State.CANCELLED, "valueOf CANCELLED");
        CompletableFuture<Integer> running = new CompletableFuture<>();
        check(running.state() == Future.State.RUNNING, "incomplete future is RUNNING");
        check(CompletableFuture.completedFuture(1).state() == Future.State.SUCCESS, "completed is SUCCESS");
        check(CompletableFuture.failedFuture(new Exception()).state() == Future.State.FAILED, "failed is FAILED");
        CompletableFuture<Integer> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);
        check(cancelled.state() == Future.State.CANCELLED, "cancelled is CANCELLED");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
