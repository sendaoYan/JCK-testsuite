/*
 * @test
 * @summary java.nio.channels.AsynchronousChannel conformance: interface hierarchy and methods.
 * @run main AsynchronousChannelTest
 */

import java.nio.channels.AsynchronousChannel;
import java.nio.channels.Channel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AsynchronousChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousChannel() {
        check(AsynchronousChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(AsynchronousChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : AsynchronousChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousChannel();
        testSignatureContract();
        testCloseSemanticsThroughInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== A7 batch: deepened contract coverage =====================

    static void testSignatureContract() throws Exception {
        java.lang.reflect.Method[] ms = AsynchronousChannel.class.getDeclaredMethods();
        check(ms.length == 1, "declares exactly one method (close), got " + ms.length);
        java.lang.reflect.Method close = ms[0];
        check(close.getName().equals("close"), "the method is close");
        check(java.lang.reflect.Modifier.isAbstract(close.getModifiers()), "close is abstract");
        check(close.getReturnType() == void.class, "close returns void");
        check(close.getExceptionTypes().length == 1
              && close.getExceptionTypes()[0] == java.io.IOException.class,
              "close declares throws IOException");
        check(java.nio.channels.Channel.class.isAssignableFrom(AsynchronousChannel.class),
              "extends Channel");
        check(!java.nio.channels.InterruptibleChannel.class.isAssignableFrom(AsynchronousChannel.class),
              "asynchronous channels are not InterruptibleChannel");
        // JDK implementors
        check(AsynchronousChannel.class.isAssignableFrom(java.nio.channels.AsynchronousSocketChannel.class),
              "AsynchronousSocketChannel implements AsynchronousChannel");
        check(AsynchronousChannel.class.isAssignableFrom(java.nio.channels.AsynchronousServerSocketChannel.class),
              "AsynchronousServerSocketChannel implements AsynchronousChannel");
        check(AsynchronousChannel.class.isAssignableFrom(java.nio.channels.AsynchronousFileChannel.class),
              "AsynchronousFileChannel implements AsynchronousChannel");
        check(!AsynchronousChannel.class.isAssignableFrom(java.nio.channels.SocketChannel.class),
              "SocketChannel is only a Channel, not asynchronous");
    }

    static void testCloseSemanticsThroughInterface() throws Exception {
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck-ac");
        try {
            java.nio.file.Path f = dir.resolve("ac.bin");
            java.nio.file.Files.write(f, new byte[] {1, 2});
            AsynchronousChannel ch = java.nio.channels.AsynchronousFileChannel.open(
                    f, java.nio.file.StandardOpenOption.READ);
            check(ch instanceof java.nio.channels.Channel, "AsynchronousChannel is a Channel");
            check(((java.nio.channels.Channel) ch).isOpen(), "open via the Channel face");
            // close is the only operation and closes the underlying channel
            ch.close();
            check(!((java.nio.channels.Channel) ch).isOpen(), "closed via AsynchronousChannel.close");
            ch.close(); // idempotent per Channel contract
            check(true, "close is idempotent");
            // I/O after close fails
            try {
                ((java.nio.channels.AsynchronousFileChannel) ch)
                        .read(java.nio.ByteBuffer.allocate(1), 0)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(false, "read on closed channel must fail");
            } catch (java.util.concurrent.ExecutionException e) {
                check(e.getCause() instanceof java.nio.channels.ClosedChannelException,
                      "read after interface close -> ClosedChannelException, got " + e.getCause());
            }
            // same contract for an asynchronous socket channel
            AsynchronousSocketChannelFace();
        } finally {
            java.nio.file.Files.deleteIfExists(dir.resolve("ac.bin"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

    static void AsynchronousSocketChannelFace() throws Exception {
        AsynchronousChannel sock = java.nio.channels.AsynchronousSocketChannel.open();
        check(sock instanceof java.nio.channels.AsynchronousSocketChannel, "async socket channel");
        sock.close();
        check(!((java.nio.channels.Channel) sock).isOpen(), "async socket closed via interface");
        AsynchronousChannel server = java.nio.channels.AsynchronousServerSocketChannel.open();
        server.close();
        check(!((java.nio.channels.Channel) server).isOpen(), "async server closed via interface");
    }


}
