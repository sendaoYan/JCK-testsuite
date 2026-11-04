/*
 * @test
 * @summary java.nio.channels.AcceptPendingException conformance: white-box structure + black-box behavior.
 * @run main AcceptPendingExceptionTest
 */

import java.nio.channels.AcceptPendingException;

public class AcceptPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(AcceptPendingException.class), "hierarchy: extends IllegalStateException");
        check(new AcceptPendingException() != null, "no-arg constructor");
        check(new AcceptPendingException().getMessage() == null, "no-arg message is null");
        check(new AcceptPendingException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // a second accept() while one is already pending is rejected
        try (java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open()) {
            server.bind(null);
            java.util.concurrent.Future<java.nio.channels.AsynchronousSocketChannel> first = server.accept();
            try {
                server.accept();
                check(false, "second accept should throw");
            } catch (AcceptPendingException e) {
                passed++;
                check(e.getMessage() == null, "no detail message");
            }
            first.cancel(false);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
