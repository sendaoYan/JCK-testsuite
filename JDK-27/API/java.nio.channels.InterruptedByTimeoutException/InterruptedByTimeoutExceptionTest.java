/*
 * @test
 * @summary java.nio.channels.InterruptedByTimeoutException conformance: white-box structure + black-box behavior.
 * @run main InterruptedByTimeoutExceptionTest
 */

import java.nio.channels.InterruptedByTimeoutException;

public class InterruptedByTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.io.IOException.class.isAssignableFrom(InterruptedByTimeoutException.class), "hierarchy: extends java.io.IOException");
        check(new InterruptedByTimeoutException() != null, "no-arg constructor");
        check(new InterruptedByTimeoutException().getMessage() == null, "no-arg message is null");
        check(new InterruptedByTimeoutException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // read with a timeout that expires before any data arrives
        try (java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open()) {
            server.bind(new java.net.InetSocketAddress(
                    java.net.InetAddress.getLoopbackAddress(), 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) server.getLocalAddress();
            try (java.nio.channels.AsynchronousSocketChannel client =
                    java.nio.channels.AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel peer =
                        server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.util.concurrent.CompletableFuture<Throwable> got = new java.util.concurrent.CompletableFuture<>();
                client.read(java.nio.ByteBuffer.allocate(1), 100, java.util.concurrent.TimeUnit.MILLISECONDS,
                        null, new java.nio.channels.CompletionHandler<Integer, Object>() {
                            public void completed(Integer v, Object a) {
                                got.complete(new AssertionError("no data expected"));
                            }
                            public void failed(Throwable t, Object a) { got.complete(t); }
                        });
                check(got.get(10, java.util.concurrent.TimeUnit.SECONDS)
                      instanceof InterruptedByTimeoutException,
                      "expired read timeout -> InterruptedByTimeoutException");
                peer.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
