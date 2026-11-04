/*
 * @test
 * @summary java.nio.channels.NotYetConnectedException conformance: white-box structure + black-box behavior.
 * @run main NotYetConnectedExceptionTest
 */

import java.nio.channels.NotYetConnectedException;

public class NotYetConnectedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(NotYetConnectedException.class), "hierarchy: extends IllegalStateException");
        check(new NotYetConnectedException() != null, "no-arg constructor");
        check(new NotYetConnectedException().getMessage() == null, "no-arg message is null");
        check(new NotYetConnectedException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open()) {
            try {
                sc.write(java.nio.ByteBuffer.wrap(new byte[] {1}));
                check(false, "write before connect should throw");
            } catch (NotYetConnectedException e) {
                passed++;
            }
            try {
                sc.read(java.nio.ByteBuffer.allocate(1));
                check(false, "read before connect should throw");
            } catch (NotYetConnectedException e) {
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
