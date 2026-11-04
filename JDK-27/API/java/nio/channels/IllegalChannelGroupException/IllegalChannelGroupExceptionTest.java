/*
 * @test
 * @summary java.nio.channels.IllegalChannelGroupException conformance: white-box structure + black-box behavior.
 * @run main IllegalChannelGroupExceptionTest
 */

import java.nio.channels.IllegalChannelGroupException;

public class IllegalChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalChannelGroupException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new IllegalChannelGroupException() != null, "no-arg constructor");
        check(new IllegalChannelGroupException().getMessage() == null, "no-arg message is null");
        check(new IllegalChannelGroupException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalChannelGroupException e = new IllegalChannelGroupException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        check(e.toString().contains("IllegalChannelGroupException"), "toString names class");
        // no-arg ctor usable in catch-and-rethrow chains
        try {
            throw e;
        } catch (IllegalChannelGroupException again) {
            check(again == e, "rethrow preserves identity");
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
        IllegalChannelGroupException e = new IllegalChannelGroupException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("IllegalChannelGroupException"), "toString names the class");
        check(e.toString().endsWith("IllegalChannelGroupException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalChannelGroupException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(java.lang.RuntimeException.class.isAssignableFrom(IllegalChannelGroupException.class), "hierarchy: java.lang.RuntimeException ancestor");
        IllegalChannelGroupException other = new IllegalChannelGroupException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (IllegalChannelGroupException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(IllegalChannelGroupException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(IllegalChannelGroupException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == -2495041211157744253L,
              "serialVersionUID is fixed at -2495041211157744253L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new IllegalChannelGroupException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == IllegalChannelGroupException.class, "round-trip exact class");
        check(back instanceof java.lang.IllegalArgumentException, "round-trip keeps the hierarchy");
        check(((IllegalChannelGroupException) back).getMessage() == null, "round-trip message null");
        check(((IllegalChannelGroupException) back).getCause() == null, "round-trip cause null");
    }

    static void testRealTriggerContract() throws Exception {
        final java.nio.channels.spi.AsynchronousChannelProvider def =
                java.nio.channels.spi.AsynchronousChannelProvider.provider();
        class ForeignProvider extends java.nio.channels.spi.AsynchronousChannelProvider {
            public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                    int n, java.util.concurrent.ThreadFactory f) throws java.io.IOException {
                return def.openAsynchronousChannelGroup(n, f);
            }
            public java.nio.channels.AsynchronousChannelGroup openAsynchronousChannelGroup(
                    java.util.concurrent.ExecutorService e, int s) throws java.io.IOException {
                return def.openAsynchronousChannelGroup(e, s);
            }
            public java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(
                    java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
                return def.openAsynchronousServerSocketChannel(g);
            }
            public java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(
                    java.nio.channels.AsynchronousChannelGroup g) throws java.io.IOException {
                return def.openAsynchronousSocketChannel(g);
            }
        }
        final ForeignProvider foreign = new ForeignProvider();
        class ForeignGroup extends java.nio.channels.AsynchronousChannelGroup {
            ForeignGroup() { super(foreign); }
            public boolean isShutdown() { return false; }
            public boolean isTerminated() { return false; }
            public void shutdown() { }
            public void shutdownNow() { }
            public boolean awaitTermination(long t, java.util.concurrent.TimeUnit u) { return true; }
        }
        java.nio.channels.AsynchronousChannelGroup g = new ForeignGroup();
        check(g.provider() == foreign, "foreign group exposes its provider");
        try {
            java.nio.channels.AsynchronousSocketChannel.open(g);
            check(false, "open with a foreign group must fail");
        } catch (IllegalChannelGroupException e) {
            check(true, "open socket with foreign group -> IllegalChannelGroupException");
            check(e.getMessage() == null, "triggered exception has no message");
        }
        try {
            java.nio.channels.AsynchronousServerSocketChannel.open(g);
            check(false, "open server with a foreign group must fail");
        } catch (IllegalChannelGroupException e) {
            check(true, "open server with foreign group -> IllegalChannelGroupException");
        }
    }

}
