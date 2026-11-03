/*
 * @test
 * @summary java.nio.channels.IllegalBlockingModeException conformance: white-box structure + black-box behavior.
 * @run main IllegalBlockingModeExceptionTest
 */

import java.nio.channels.IllegalBlockingModeException;

public class IllegalBlockingModeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(IllegalBlockingModeException.class), "hierarchy: extends IllegalStateException");
        check(new IllegalBlockingModeException() != null, "no-arg constructor");
        check(new IllegalBlockingModeException().getMessage() == null, "no-arg message is null");
        check(new IllegalBlockingModeException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // using the socket adaptor stream while the channel is in non-blocking mode
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open();
             java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            sc.connect(ssc.getLocalAddress());
            try (java.nio.channels.SocketChannel peer = ssc.accept()) {
                sc.configureBlocking(false);
                try {
                    sc.socket().getInputStream().read();
                    check(false, "adaptor read in non-blocking mode should throw");
                } catch (IllegalBlockingModeException e) {
                    passed++;
                } catch (java.io.IOException io) {
                    check(false, "expected IllegalBlockingModeException, got " + io);
                }
                try {
                    sc.socket().getOutputStream().write(1);
                    check(false, "adaptor write in non-blocking mode should throw");
                } catch (IllegalBlockingModeException e) {
                    passed++;
                } catch (java.io.IOException io) {
                    check(false, "expected IllegalBlockingModeException (write), got " + io);
                }
            }
        }
        // adaptor accept on a non-blocking server channel
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(null);
            ssc.configureBlocking(false);
            try {
                ssc.socket().accept();
                check(false, "adaptor accept in non-blocking mode should throw");
            } catch (IllegalBlockingModeException e) {
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
