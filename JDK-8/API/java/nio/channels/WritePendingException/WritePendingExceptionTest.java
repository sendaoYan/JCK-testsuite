/*
 * @test
 * @summary java.nio.channels.WritePendingException conformance: hierarchy and constructor.
 * @run main WritePendingExceptionTest
 */

import java.nio.channels.WritePendingException;

public class WritePendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWritePendingException() {
        check(IllegalStateException.class.isAssignableFrom(WritePendingException.class), "extends IllegalStateException");
        WritePendingException e = new WritePendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testWritePendingException();
        testExceptionObjectContract();
        testSerializationContract();
        testRealTriggerContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static void testExceptionObjectContract() {
        WritePendingException e = new WritePendingException();
        check(e.getMessage() == null, "no-arg message is null");
        check(e.getCause() == null, "no cause by default");
        check(e.getLocalizedMessage() == null, "localized message null by default");
        check(e.toString().contains("WritePendingException"), "toString names the class");
        check(e.toString().endsWith("WritePendingException"), "toString has no message suffix");
        check(e.fillInStackTrace() == e, "fillInStackTrace returns this");
        check(e.getStackTrace() != null, "stack trace array present");
        check(e.getStackTrace().length > 0, "captured frames recorded");
        check(e.getSuppressed().length == 0, "no suppressed exceptions by default");
        check(java.lang.IllegalStateException.class.isAssignableFrom(WritePendingException.class), "hierarchy: extends java.lang.IllegalStateException");
        check(java.lang.RuntimeException.class.isAssignableFrom(WritePendingException.class), "hierarchy: java.lang.RuntimeException ancestor");
        WritePendingException other = new WritePendingException();
        check(other != e, "independent instances");
        try {
            throw e;
        } catch (WritePendingException again) {
            check(again == e, "rethrow preserves identity");
        }
        check(new Object[] { e, other }.length == 2, "usable as ordinary values");
    }

    static void testSerializationContract() throws Exception {
        check(java.io.Serializable.class.isAssignableFrom(WritePendingException.class), "Serializable");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(WritePendingException.class);
        check(osc != null, "ObjectStreamClass lookup succeeds");
        check(osc.getSerialVersionUID() == 7031871839266032276L,
              "serialVersionUID is fixed at 7031871839266032276L");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(new WritePendingException());
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Object back = ois.readObject();
        ois.close();
        check(back != null, "round-trip produced an object");
        check(back.getClass() == WritePendingException.class, "round-trip exact class");
        check(back instanceof java.lang.IllegalStateException, "round-trip keeps the hierarchy");
        check(((WritePendingException) back).getMessage() == null, "round-trip message null");
        check(((WritePendingException) back).getCause() == null, "round-trip cause null");
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
                client.setOption(java.net.StandardSocketOptions.SO_SNDBUF, 1024);
                client.connect(addr).get(10, java.util.concurrent.TimeUnit.SECONDS);
                peer = server.accept().get(10, java.util.concurrent.TimeUnit.SECONDS);
                peer.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 1024);
                // saturate the kernel buffers until one write stays pending
                java.util.concurrent.Future<Integer> parked = null;
                for (int i = 0; i < 32; i++) {
                    java.util.concurrent.Future<Integer> f =
                            client.write(java.nio.ByteBuffer.allocate(256 * 1024));
                    if (!f.isDone()) { parked = f; break; }
                }
                check(parked != null, "a pending write was produced");
                try {
                    client.write(java.nio.ByteBuffer.allocate(1));
                    check(false, "concurrent write must be rejected");
                } catch (WritePendingException e) {
                    check(true, "concurrent write -> WritePendingException");
                    check(e.getMessage() == null, "triggered exception has no message");
                }
                // draining the peer completes the parked write
                java.nio.ByteBuffer drain = java.nio.ByteBuffer.allocate(65536);
                while (!parked.isDone()) {
                    java.util.concurrent.Future<Integer> rf = peer.read(drain);
                    Integer n;
                    try {
                        n = rf.get(2, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (java.util.concurrent.TimeoutException te) {
                        break;
                    }
                    if (n == null || n <= 0) break;
                    drain.clear();
                }
                check(parked.get(10, java.util.concurrent.TimeUnit.SECONDS) > 0,
                      "parked write completed after draining");
                peer.close();
            } finally {
                client.close();
            }
        } finally {
            server.close();
        }
    }

}
