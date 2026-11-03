/*
 * @test
 * @summary java.nio.channels.ShutdownChannelGroupException conformance: white-box structure + black-box behavior.
 * @run main ShutdownChannelGroupExceptionTest
 */

import java.nio.channels.ShutdownChannelGroupException;

public class ShutdownChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(ShutdownChannelGroupException.class), "hierarchy: extends IllegalStateException");
        check(new ShutdownChannelGroupException() != null, "no-arg constructor");
        check(new ShutdownChannelGroupException().getMessage() == null, "no-arg message is null");
        check(new ShutdownChannelGroupException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.nio.channels.AsynchronousChannelGroup group =
                java.nio.channels.AsynchronousChannelGroup.withFixedThreadPool(1, r -> {
                    Thread t = new Thread(r, "scge"); t.setDaemon(true); return t;
                });
        try {
            group.shutdownNow();
            try {
                java.nio.channels.AsynchronousServerSocketChannel.open(group);
                check(false, "open on shutdown group should throw");
            } catch (ShutdownChannelGroupException e) {
                passed++;
            }
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                    "group terminated");
        } finally {
            group.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
