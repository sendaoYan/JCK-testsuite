/*
 * @test
 * @summary java.nio.channels.AlreadyConnectedException conformance: white-box structure + black-box behavior.
 * @run main AlreadyConnectedExceptionTest
 */

import java.nio.channels.AlreadyConnectedException;

public class AlreadyConnectedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyConnectedException.class), "hierarchy: extends IllegalStateException");
        check(new AlreadyConnectedException() != null, "no-arg constructor");
        check(new AlreadyConnectedException().getMessage() == null, "no-arg message is null");
        check(new AlreadyConnectedException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open(addr)) {
                check(sc.isConnected(), "connected once");
                try {
                    sc.connect(addr);
                    check(false, "second connect should throw");
                } catch (AlreadyConnectedException e) {
                    passed++;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
