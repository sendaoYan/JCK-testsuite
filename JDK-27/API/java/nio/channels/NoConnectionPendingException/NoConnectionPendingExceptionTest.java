/*
 * @test
 * @summary java.nio.channels.NoConnectionPendingException conformance: white-box structure + black-box behavior.
 * @run main NoConnectionPendingExceptionTest
 */

import java.nio.channels.NoConnectionPendingException;

public class NoConnectionPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NoConnectionPendingException.class), "hierarchy: extends IllegalStateException");
        check(new NoConnectionPendingException() != null, "no-arg constructor");
        check(new NoConnectionPendingException().getMessage() == null, "no-arg message is null");
        check(new NoConnectionPendingException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            try {
                sc.finishConnect();
                check(false, "finishConnect without connect should throw");
            } catch (NoConnectionPendingException e) {
                passed++;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
