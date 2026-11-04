/*
 * @test
 * @summary java.nio.channels.AcceptPendingException conformance: white-box structure + black-box behavior.
 * @run main AcceptPendingExceptionTest
 */

import java.nio.channels.AcceptPendingException;

public class AcceptPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(AcceptPendingException.class), "hierarchy: extends IllegalStateException");
        check(new AcceptPendingException() != null, "no-arg constructor");
        check(new AcceptPendingException().getMessage() == null, "no-arg message is null");
        check(new AcceptPendingException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // a second accept() while one is already pending is rejected
        try (java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open()) {
            server.bind(null);
            java.util.concurrent.Future<java.nio.channels.AsynchronousSocketChannel> first = server.accept();
            try {
                server.accept();
                check(false, "second accept should throw");
            } catch (AcceptPendingException e) {
                passed++;
                check(e.getMessage() == null, "no detail message");
            }
            first.cancel(false);
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
        AcceptPendingException e = new AcceptPendingException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("AcceptPendingException"), "toString names the class");
        check(e.toString().endsWith("AcceptPendingException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.lang.IllegalStateException.class.isAssignableFrom(AcceptPendingException.class), "hierarchy: extends java.lang.IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(AcceptPendingException.class), "hierarchy: java.lang.RuntimeException ancestor");
        AcceptPendingException other = new AcceptPendingException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (AcceptPendingException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(AcceptPendingException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(AcceptPendingException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == 2721339977965416421L,
              "serialVersionUID is fixed at 2721339977965416421L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new AcceptPendingException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == AcceptPendingException.class, "round-trip exact class");
        check(back instanceof java.lang.IllegalStateException, "round-trip keeps the hierarchy");
        check(((AcceptPendingException) back).getMessage() == null, "round-trip message null");
        check(((AcceptPendingException) back).getCause() == null, "round-trip cause null");
    }

    static void testRealTriggerContract() throws Exception {
        java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open();
        try {
            server.bind(new java.net.InetSocketAddress(
                    java.net.InetAddress.getLoopbackAddress(), 0));
            // accept with no connection stays pending
            java.util.concurrent.Future<java.nio.channels.AsynchronousSocketChannel> pending =
                    server.accept();
            check(!pending.isDone(), "accept pending with no client");
            try {
                server.accept();
                check(false, "concurrent accept must be rejected");
            } catch (AcceptPendingException e) {
                check(true, "concurrent accept -> AcceptPendingException");
                check(e.getMessage() == null, "triggered exception has no message");
            }
            // the handler form is rejected the same way
            try {
                server.accept(null, new java.nio.channels.CompletionHandler<java.nio.channels.AsynchronousSocketChannel, Object>() {
                    public void completed(java.nio.channels.AsynchronousSocketChannel r, Object a) { }
                    public void failed(Throwable t, Object a) { }
                });
                check(false, "concurrent handler accept must be rejected");
            } catch (AcceptPendingException e) {
                check(true, "concurrent handler accept -> AcceptPendingException");
            }
            pending.cancel(false);
            check(pending.isCancelled(), "pending accept cancelled");
        } finally {
            server.close();
        }
    }

}
