/*
 * @test
 * @summary java.nio.channels.InterruptedByTimeoutException conformance: hierarchy and constructor.
 * @run main InterruptedByTimeoutExceptionTest
 */

import java.nio.channels.InterruptedByTimeoutException;
import java.io.IOException;

public class InterruptedByTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInterruptedByTimeoutException() {
        check(IOException.class.isAssignableFrom(InterruptedByTimeoutException.class), "extends IOException");
        InterruptedByTimeoutException e = new InterruptedByTimeoutException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IOException, "instanceof IOException");
    }

    public static void main(String[] args) throws Exception {
        testInterruptedByTimeoutException();
        testExceptionObjectContract();
        testSerializationContract();
        testRealTriggerContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static void testExceptionObjectContract() {
        InterruptedByTimeoutException e = new InterruptedByTimeoutException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("InterruptedByTimeoutException"), "toString names the class");
        check(e.toString().endsWith("InterruptedByTimeoutException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.io.IOException.class.isAssignableFrom(InterruptedByTimeoutException.class), "hierarchy: extends java.io.IOException");
        check(java.lang.Exception.class.isAssignableFrom(InterruptedByTimeoutException.class), "hierarchy: java.lang.Exception ancestor");
        InterruptedByTimeoutException other = new InterruptedByTimeoutException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (InterruptedByTimeoutException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(InterruptedByTimeoutException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(InterruptedByTimeoutException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == -4268008601014042947L,
              "serialVersionUID is fixed at -4268008601014042947L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new InterruptedByTimeoutException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == InterruptedByTimeoutException.class, "round-trip exact class");
        check(back instanceof java.io.IOException, "round-trip keeps the hierarchy");
        check(((InterruptedByTimeoutException) back).getMessage() == null, "round-trip message null");
        check(((InterruptedByTimeoutException) back).getCause() == null, "round-trip cause null");
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
                // short timeout with guaranteed no data expires deterministically
                final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
                final Throwable[] err = new Throwable[1];
                final Object[] att = new Object[1];
                client.read(java.nio.ByteBuffer.allocate(1), 60,
                        java.util.concurrent.TimeUnit.MILLISECONDS, "tok",
                        new java.nio.channels.CompletionHandler<Integer, Object>() {
                            public void completed(Integer r, Object a) { done.countDown(); }
                            public void failed(Throwable t, Object a) { err[0] = t; att[0] = a; done.countDown(); }
                        });
                check(done.await(10, java.util.concurrent.TimeUnit.SECONDS), "timed-out read reported");
                check(err[0] instanceof InterruptedByTimeoutException,
                      "expired timeout -> InterruptedByTimeoutException, got " + err[0]);
                check("tok".equals(att[0]), "failed handler receives the attachment");
                peer.close();
            } finally {
                client.close();
            }
        } finally {
            server.close();
        }
    }

}
