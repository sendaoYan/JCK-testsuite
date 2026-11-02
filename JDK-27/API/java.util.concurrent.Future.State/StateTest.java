/*
 * @test
 * @summary java.util.concurrent.Future.State conformance: enum constants and
 *          CompletableFuture state() reporting.
 * @run main StateTest
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Future.State.values().length == 4, "four states");
        check(Future.State.valueOf("RUNNING") == Future.State.RUNNING, "valueOf RUNNING");
        check(Future.State.valueOf("SUCCESS") == Future.State.SUCCESS, "valueOf SUCCESS");
        check(Future.State.valueOf("FAILED") == Future.State.FAILED, "valueOf FAILED");
        check(Future.State.valueOf("CANCELLED") == Future.State.CANCELLED, "valueOf CANCELLED");

        CompletableFuture<Integer> running = new CompletableFuture<>();
        check(running.state() == Future.State.RUNNING, "incomplete future is RUNNING");

        CompletableFuture<Integer> success = CompletableFuture.completedFuture(1);
        check(success.state() == Future.State.SUCCESS, "completed future is SUCCESS");

        CompletableFuture<Integer> failed = CompletableFuture.failedFuture(new Exception());
        check(failed.state() == Future.State.FAILED, "failed future is FAILED");

        CompletableFuture<Integer> cancelled = new CompletableFuture<>();
        cancelled.cancel(true);
        check(cancelled.state() == Future.State.CANCELLED, "cancelled future is CANCELLED");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
