/*
 * @test
 * @summary java.nio.channels.ReadPendingException conformance: white-box structure + black-box behavior.
 * @run main ReadPendingExceptionTest
 */

import java.nio.channels.ReadPendingException;

public class ReadPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(ReadPendingException.class), "hierarchy: extends IllegalStateException");
        check(new ReadPendingException() != null, "no-arg constructor");
        check(new ReadPendingException().getMessage() == null, "no-arg message is null");
        check(new ReadPendingException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
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
                // start a long read that cannot complete, then a second read
                java.util.concurrent.CompletableFuture<Integer> pending = new java.util.concurrent.CompletableFuture<>();
                client.read(java.nio.ByteBuffer.allocate(1), 60, java.util.concurrent.TimeUnit.SECONDS,
                        null, new java.nio.channels.CompletionHandler<Integer, Object>() {
                            public void completed(Integer v, Object a) { pending.complete(v); }
                            public void failed(Throwable t, Object a) { pending.completeExceptionally(t); }
                        });
                try {
                    client.read(java.nio.ByteBuffer.allocate(1));
                    check(false, "concurrent read should throw");
                } catch (ReadPendingException e) {
                    passed++;
                }
                peer.write(java.nio.ByteBuffer.wrap(new byte[] {1}))
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(pending.get(10, java.util.concurrent.TimeUnit.SECONDS) == 1, "first read completed");
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
