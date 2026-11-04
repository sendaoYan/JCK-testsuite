/*
 * @test
 * @summary java.nio.channels.AsynchronousCloseException conformance: white-box structure + black-box behavior.
 * @run main AsynchronousCloseExceptionTest
 */

import java.nio.channels.AsynchronousCloseException;

public class AsynchronousCloseExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.nio.channels.ClosedChannelException.class.isAssignableFrom(AsynchronousCloseException.class), "hierarchy: extends java.nio.channels.ClosedChannelException");
        check(new AsynchronousCloseException() != null, "no-arg constructor");
        check(new AsynchronousCloseException().getMessage() == null, "no-arg message is null");
        check(new AsynchronousCloseException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // closing a pipe source while another thread is blocked reading it
        final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        final Exception[] caught = new Exception[1];
        Thread reader = new Thread(() -> {
            try {
                pipe.source().read(java.nio.ByteBuffer.allocate(1));
            } catch (Exception e) {
                caught[0] = e;
            }
        }, "async-close-reader");
        reader.setDaemon(true);
        reader.start();
        Thread.sleep(300); // let the reader block
        pipe.source().close();
        reader.join(10_000);
        check(!reader.isAlive(), "reader finished");
        check(caught[0] instanceof AsynchronousCloseException,
              "blocked read closed by another thread -> AsynchronousCloseException, got " + caught[0]);
        check(caught[0] instanceof java.nio.channels.ClosedChannelException,
              "AsynchronousCloseException is a ClosedChannelException");
        pipe.sink().close();
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
        AsynchronousCloseException e = new AsynchronousCloseException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("AsynchronousCloseException"), "toString names the class");
        check(e.toString().endsWith("AsynchronousCloseException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.nio.channels.ClosedChannelException.class.isAssignableFrom(AsynchronousCloseException.class), "hierarchy: extends java.nio.channels.ClosedChannelException");
        check(java.io.IOException.class.isAssignableFrom(AsynchronousCloseException.class), "hierarchy: java.io.IOException ancestor");
        AsynchronousCloseException other = new AsynchronousCloseException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (AsynchronousCloseException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(AsynchronousCloseException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(AsynchronousCloseException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == 6891178312432313966L,
              "serialVersionUID is fixed at 6891178312432313966L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new AsynchronousCloseException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == AsynchronousCloseException.class, "round-trip exact class");
        check(back instanceof java.nio.channels.ClosedChannelException, "round-trip keeps the hierarchy");
        check(((AsynchronousCloseException) back).getMessage() == null, "round-trip message null");
        check(((AsynchronousCloseException) back).getCause() == null, "round-trip cause null");
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
            try {
                client.connect(addr).get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel peer =
                        server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                // closing while a read is pending reports AsynchronousCloseException
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final Throwable[] err = new Throwable[1];
                client.read(java.nio.ByteBuffer.allocate(1), 30,
                        java.util.concurrent.TimeUnit.SECONDS, null,
                        new java.nio.channels.CompletionHandler<Integer, Object>() {
                            public void completed(Integer r, Object a) { done.countDown(); }
                            public void failed(Throwable t, Object a) { err[0] = t; done.countDown(); }
                        });
                client.close();
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS),
                      "pending read terminated by close");
                check(err[0] instanceof AsynchronousCloseException,
                      "close during read -> AsynchronousCloseException, got " + err[0]);
                peer.close();
            } finally {
                client.close();
            }
            // the same contract for a pending accept, surfaced through the Future
            java.util.concurrent.Future<java.nio.channels.AsynchronousSocketChannel> pending =
                    server.accept();
            check(!pending.isDone(), "accept pending");
            server.close();
            try {
                pending.get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(false, "pending accept must fail on close");
            } catch (java.util.concurrent.ExecutionException e) {
                check(e.getCause() instanceof AsynchronousCloseException,
                      "close during accept -> AsynchronousCloseException, got " + e.getCause());
            }
        } finally {
            server.close();
        }
    }

}
