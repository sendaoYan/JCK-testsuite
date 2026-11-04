/*
 * @test
 * @summary java.nio.channels.NotYetBoundException conformance: white-box structure + black-box behavior.
 * @run main NotYetBoundExceptionTest
 */

import java.nio.channels.NotYetBoundException;

public class NotYetBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NotYetBoundException.class), "hierarchy: extends IllegalStateException");
        check(new NotYetBoundException() != null, "no-arg constructor");
        check(new NotYetBoundException().getMessage() == null, "no-arg message is null");
        check(new NotYetBoundException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            try {
                ssc.accept();
                check(false, "accept before bind should throw");
            } catch (NotYetBoundException e) {
                passed++;
            }
            ssc.bind(null);
            // with a client connecting, accept on the bound channel succeeds
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.SocketChannel c =
                    java.nio.channels.SocketChannel.open(addr)) {
                java.nio.channels.SocketChannel accepted = ssc.accept();
                check(accepted != null, "accept works once bound");
                accepted.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
