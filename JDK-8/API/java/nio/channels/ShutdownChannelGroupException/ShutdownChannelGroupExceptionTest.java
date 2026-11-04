/*
 * @test
 * @summary java.nio.channels.ShutdownChannelGroupException conformance: hierarchy and constructor.
 * @run main ShutdownChannelGroupExceptionTest
 */

import java.nio.channels.ShutdownChannelGroupException;

public class ShutdownChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testShutdownChannelGroupException() {
        check(IllegalStateException.class.isAssignableFrom(ShutdownChannelGroupException.class), "extends IllegalStateException");
        ShutdownChannelGroupException e = new ShutdownChannelGroupException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testShutdownChannelGroupException();
        testExceptionObjectContract();
        testSerializationContract();
        testRealTriggerContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static void testExceptionObjectContract() {
        ShutdownChannelGroupException e = new ShutdownChannelGroupException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("ShutdownChannelGroupException"), "toString names the class");
        check(e.toString().endsWith("ShutdownChannelGroupException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.lang.IllegalStateException.class.isAssignableFrom(ShutdownChannelGroupException.class), "hierarchy: extends java.lang.IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(ShutdownChannelGroupException.class), "hierarchy: java.lang.RuntimeException ancestor");
        ShutdownChannelGroupException other = new ShutdownChannelGroupException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (ShutdownChannelGroupException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(ShutdownChannelGroupException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(ShutdownChannelGroupException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == -3903801676350154157L,
              "serialVersionUID is fixed at -3903801676350154157L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new ShutdownChannelGroupException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == ShutdownChannelGroupException.class, "round-trip exact class");
        check(back instanceof java.lang.IllegalStateException, "round-trip keeps the hierarchy");
        check(((ShutdownChannelGroupException) back).getMessage() == null, "round-trip message null");
        check(((ShutdownChannelGroupException) back).getCause() == null, "round-trip cause null");
    }

    static void testRealTriggerContract() throws Exception {
        java.util.concurrent.ThreadFactory daemon = new java.util.concurrent.ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "a7-scge");
                t.setDaemon(true);
                return t;
            }
        };
        java.nio.channels.AsynchronousChannelGroup group =
                java.nio.channels.AsynchronousChannelGroup.withFixedThreadPool(1, daemon);
        try {
            group.shutdownNow();
            check(group.isShutdown(), "group is shutdown");
            try {
                java.nio.channels.AsynchronousServerSocketChannel.open(group);
                check(false, "open on a shutdown group must fail");
            } catch (ShutdownChannelGroupException e) {
                check(true, "open server on shutdown group -> ShutdownChannelGroupException");
                check(e.getMessage() == null, "triggered exception has no message");
            }
            try {
                java.nio.channels.AsynchronousSocketChannel.open(group);
                check(false, "open socket on a shutdown group must fail");
            } catch (ShutdownChannelGroupException e) {
                check(true, "open socket on shutdown group -> ShutdownChannelGroupException");
            }
            check(group.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS),
                  "group terminated after shutdownNow");
        } finally {
            group.shutdownNow();
        }
    }

}
