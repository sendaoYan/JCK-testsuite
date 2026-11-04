/*
 * @test
 * @summary java.nio.channels.UnresolvedAddressException conformance: white-box structure + black-box behavior.
 * @run main UnresolvedAddressExceptionTest
 */

import java.nio.channels.UnresolvedAddressException;

public class UnresolvedAddressExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(UnresolvedAddressException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new UnresolvedAddressException() != null, "no-arg constructor");
        check(new UnresolvedAddressException().getMessage() == null, "no-arg message is null");
        check(new UnresolvedAddressException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // connect to an unresolved address: no DNS lookup needed with createUnresolved
        java.net.InetSocketAddress unresolved =
                java.net.InetSocketAddress.createUnresolved("nonexistent.invalid", 80);
        check(unresolved.isUnresolved(), "address is unresolved");
        try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            try {
                sc.connect(unresolved);
                check(false, "connect to unresolved should throw");
            } catch (UnresolvedAddressException e) {
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
