/*
 * @test
 * @summary java.nio.channels.AsynchronousByteChannel conformance: interface hierarchy and methods.
 * @run main AsynchronousByteChannelTest
 */

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AsynchronousByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousByteChannel() {
        check(AsynchronousByteChannel.class.isInterface(), "is interface");
        check(AsynchronousChannel.class.isAssignableFrom(AsynchronousByteChannel.class), "extends AsynchronousChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : AsynchronousByteChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "read", "write" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousByteChannel();
        testSignatureContract();
        testBehaviourThroughInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== A7 batch: deepened contract coverage =====================

    static void testSignatureContract() throws Exception {
        java.lang.reflect.Method[] ms = AsynchronousByteChannel.class.getDeclaredMethods();
        check(ms.length == 4, "exactly four declared methods, got " + ms.length);
        int reads = 0, writes = 0;
        for (java.lang.reflect.Method m : ms) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " is abstract");
            if (m.getName().equals("read")) reads++;
            if (m.getName().equals("write")) writes++;
        }
        check(reads == 2 && writes == 2, "read and write each have two overloads");
        java.lang.reflect.Method readF = AsynchronousByteChannel.class.getMethod("read", java.nio.ByteBuffer.class);
        check(readF.getReturnType() == java.util.concurrent.Future.class,
              "read(ByteBuffer) returns Future<Integer>");
        java.lang.reflect.Method readH = AsynchronousByteChannel.class.getMethod(
                "read", java.nio.ByteBuffer.class, Object.class, java.nio.channels.CompletionHandler.class);
        check(readH.getReturnType() == void.class, "read(ByteBuffer, A, handler) returns void");
        java.lang.reflect.Method writeF = AsynchronousByteChannel.class.getMethod("write", java.nio.ByteBuffer.class);
        check(writeF.getReturnType() == java.util.concurrent.Future.class,
              "write(ByteBuffer) returns Future<Integer>");
        java.lang.reflect.Method writeH = AsynchronousByteChannel.class.getMethod(
                "write", java.nio.ByteBuffer.class, Object.class, java.nio.channels.CompletionHandler.class);
        check(writeH.getReturnType() == void.class, "write(ByteBuffer, A, handler) returns void");
        // implementors in the JDK
        check(java.nio.channels.AsynchronousSocketChannel.class
                .isAssignableFrom(AsynchronousByteChannel.class) == false, "relationship is the other way");
        check(AsynchronousByteChannel.class.isAssignableFrom(java.nio.channels.AsynchronousSocketChannel.class),
              "AsynchronousSocketChannel implements AsynchronousByteChannel");
        check(!AsynchronousByteChannel.class.isAssignableFrom(java.nio.channels.AsynchronousFileChannel.class),
              "AsynchronousFileChannel is positioned, not a byte channel");
        check(!AsynchronousByteChannel.class.isAssignableFrom(java.nio.channels.SocketChannel.class),
              "SocketChannel is synchronous, does not implement it");
    }

    static void testBehaviourThroughInterface() throws Exception {
        java.net.InetAddress lo = java.net.InetAddress.getLoopbackAddress();
        try (java.nio.channels.AsynchronousServerSocketChannel server =
                java.nio.channels.AsynchronousServerSocketChannel.open()) {
            server.bind(new java.net.InetSocketAddress(lo, 0));
            java.net.InetSocketAddress addr = (java.net.InetSocketAddress) server.getLocalAddress();
            try (java.nio.channels.AsynchronousSocketChannel client =
                    java.nio.channels.AsynchronousSocketChannel.open()) {
                client.connect(addr).get(10, java.util.concurrent.TimeUnit.SECONDS);
                java.nio.channels.AsynchronousSocketChannel peer = server.accept()
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                AsynchronousByteChannel face = client;
                // Future write through the interface type
                java.util.concurrent.Future<Integer> w = face.write(java.nio.ByteBuffer.wrap(new byte[] {6, 7}));
                check(w.get(10, java.util.concurrent.TimeUnit.SECONDS) == 2, "interface write 2 bytes");
                // handler write with attachment through the interface type
                final java.util.concurrent.CountDownLatch wd = new java.util.concurrent.CountDownLatch(1);
                final Integer[] got = new Integer[1];
                final Object[] att = new Object[1];
                face.write(java.nio.ByteBuffer.wrap(new byte[] {8}), "w",
                        new java.nio.channels.CompletionHandler<Integer, String>() {
                            public void completed(Integer r, String a) { got[0] = r; att[0] = a; wd.countDown(); }
                            public void failed(Throwable t, String a) { wd.countDown(); }
                        });
                check(wd.await(10, java.util.concurrent.TimeUnit.SECONDS), "interface handler write invoked");
                check(got[0] != null && got[0] == 1, "interface handler write result");
                check("w".equals(att[0]), "interface write attachment");
                // peer echoes everything back
                java.nio.ByteBuffer echo = java.nio.ByteBuffer.allocate(3);
                while (echo.position() < 3)
                    peer.read(echo).get(10, java.util.concurrent.TimeUnit.SECONDS);
                peer.write((java.nio.ByteBuffer) echo.flip()).get(10, java.util.concurrent.TimeUnit.SECONDS);
                // Future read through the interface type
                java.nio.ByteBuffer rb = java.nio.ByteBuffer.allocate(3);
                java.util.concurrent.Future<Integer> r = face.read(rb);
                check(r.get(10, java.util.concurrent.TimeUnit.SECONDS) == 3, "interface read 3 bytes");
                check(rb.get(0) == 6 && rb.get(1) == 7 && rb.get(2) == 8, "interface read content");
                // handler read through the interface type
                final java.util.concurrent.CountDownLatch rd = new java.util.concurrent.CountDownLatch(1);
                final Integer[] rgot = new Integer[1];
                java.nio.ByteBuffer rb2 = java.nio.ByteBuffer.allocate(1);
                peer.write(java.nio.ByteBuffer.wrap(new byte[] {9})).get(10, java.util.concurrent.TimeUnit.SECONDS);
                face.read(rb2, null, new java.nio.channels.CompletionHandler<Integer, Object>() {
                    public void completed(Integer v, Object a) { rgot[0] = v; rd.countDown(); }
                    public void failed(Throwable t, Object a) { rd.countDown(); }
                });
                check(rd.await(10, java.util.concurrent.TimeUnit.SECONDS), "interface handler read invoked");
                check(rgot[0] != null && rgot[0] == 1 && rb2.get(0) == 9, "interface handler read result");
                // close through the interface type closes the real channel
                face.close();
                check(!client.isOpen(), "interface close closes the channel");
                peer.close();
            }
        }
    }


}
