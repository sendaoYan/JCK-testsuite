/*
 * @test
 * @summary java.nio.channels.WritePendingException conformance: white-box structure + black-box behavior.
 * @run main WritePendingExceptionTest
 */

import java.nio.channels.WritePendingException;

public class WritePendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(WritePendingException.class), "hierarchy: extends IllegalStateException");
        check(new WritePendingException() != null, "no-arg constructor");
        check(new WritePendingException().getMessage() == null, "no-arg message is null");
        check(new WritePendingException().getCause() == null, "no cause by default");
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
                // Fill the connection: chain writes until the peer (which never reads)
                // stops draining, leaving one write permanently pending.
                peer.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 1);
                final java.util.concurrent.atomic.AtomicInteger completed =
                        new java.util.concurrent.atomic.AtomicInteger();
                final java.util.concurrent.atomic.AtomicReference<Throwable> failure =
                        new java.util.concurrent.atomic.AtomicReference<>();
                java.nio.channels.CompletionHandler<Integer, Object> chain =
                        new java.nio.channels.CompletionHandler<Integer, Object>() {
                    public void completed(Integer v, Object a) {
                        completed.incrementAndGet();
                        client.write(java.nio.ByteBuffer.allocate(64 * 1024), null, this);
                    }
                    public void failed(Throwable t, Object a) { failure.set(t); }
                };
                client.write(java.nio.ByteBuffer.allocate(64 * 1024), null, chain);
                int prev = completed.get();
                boolean stable = false;
                for (int i = 0; i < 30 && failure.get() == null; i++) {
                    Thread.sleep(200); // pauses until completions stop advancing
                    int now = completed.get();
                    if (now == prev) { stable = true; break; }
                    prev = now;
                }
                check(stable || failure.get() != null, "write backlog saturated");
                if (stable) {
                    try {
                        client.write(java.nio.ByteBuffer.allocate(1));
                        check(false, "concurrent write should throw once one is pending");
                    } catch (WritePendingException e) {
                        passed++;
                    }
                }
                peer.close(); // releases the pending write
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
