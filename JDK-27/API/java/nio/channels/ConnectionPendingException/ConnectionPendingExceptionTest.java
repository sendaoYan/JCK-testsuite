/*
 * @test
 * @summary java.nio.channels.ConnectionPendingException conformance: white-box structure + black-box behavior.
 * @run main ConnectionPendingExceptionTest
 */

import java.nio.channels.ConnectionPendingException;

public class ConnectionPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(ConnectionPendingException.class), "hierarchy: extends IllegalStateException");
        check(new ConnectionPendingException() != null, "no-arg constructor");
        check(new ConnectionPendingException().getMessage() == null, "no-arg message is null");
        check(new ConnectionPendingException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
                sc.configureBlocking(false);
                sc.connect(addr);
                check(sc.isConnectionPending(), "connection pending");
                try {
                    sc.connect(addr);
                    check(false, "second connect should throw");
                } catch (ConnectionPendingException e) {
                    passed++;
                }
                sc.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
