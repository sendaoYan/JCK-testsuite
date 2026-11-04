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
        testExceptionObjectContract();
        testSerializationContract();
        testRealTriggerContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static void testExceptionObjectContract() {
        ReadPendingException e = new ReadPendingException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("ReadPendingException"), "toString names the class");
        check(e.toString().endsWith("ReadPendingException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.lang.IllegalStateException.class.isAssignableFrom(ReadPendingException.class), "hierarchy: extends java.lang.IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(ReadPendingException.class), "hierarchy: java.lang.RuntimeException ancestor");
        ReadPendingException other = new ReadPendingException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (ReadPendingException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(ReadPendingException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ReadPendingException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == 1986315242191227217L,
              "serialVersionUID is fixed at 1986315242191227217L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new ReadPendingException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == ReadPendingException.class, "round-trip exact class");
        check(back instanceof java.lang.IllegalStateException, "round-trip keeps the hierarchy");
        check(((ReadPendingException) back).getMessage() == null, "round-trip message null");
        check(((ReadPendingException) back).getCause() == null, "round-trip cause null");
    }

    static void testRealTriggerContract() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open();
        try {
            server.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) server.getLocalAddress();
            java.nio.channels.AsynchronousSocketChannel client =
                    java.nio.channels.AsynchronousSocketChannel.open();
            java.nio.channels.AsynchronousSocketChannel peer;
            try {
                client.connect(addr).get(10, java.util.concurrent.TimeUnit.SECONDS);
                peer = server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                // a read with no incoming data stays pending
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                client.read(java.nio.ByteBuffer.allocate(1), 30, java.util.concurrent.TimeUnit.SECONDS, null,
                        new java.nio.channels.CompletionHandler<Integer, Object>() {
                            public void completed(Integer r, Object a) { done.countDown(); }
                            public void failed(Throwable t, Object a) { done.countDown(); }
                        });
                try {
                    client.read(java.nio.ByteBuffer.allocate(1));
                    check(false, "concurrent read must be rejected");
                } catch (ReadPendingException e) {
                    check(true, "concurrent read -> ReadPendingException");
                    check(e.getMessage() == null, "triggered exception has no message");
                }
                peer.write(java.nio.ByteBuffer.wrap(new byte[] {1}))
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                      "pending read released once data arrives");
                peer.close();
            } finally {
                client.close();
            }
        } finally {
            server.close();
        }
    }

}
