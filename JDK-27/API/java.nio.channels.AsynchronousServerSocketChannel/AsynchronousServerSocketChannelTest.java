/*
 * @test
 * @summary java.nio.channels.AsynchronousServerSocketChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousServerSocketChannelTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsynchronousServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousServerSocketChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousServerSocketChannel is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(java.nio.channels.AsynchronousChannel.class.isAssignableFrom(c),
              "implements AsynchronousChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "implements NetworkChannel");
        for (String n : new String[] {"open", "bind", "setOption", "accept", "getLocalAddress", "provider"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        var group = AsynchronousChannelGroup.withFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "assc"); t.setDaemon(true); return t;
        });
        try {
            try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group)) {
                check(server.provider() == group.provider(), "provider from group");
                check(!server.isOpen() == false, "server open");

                // accept before bind
                boolean threw = false;
                try { server.accept(); }
                catch (java.nio.channels.NotYetBoundException e) { threw = true; }
                check(threw, "accept before bind -> NotYetBoundException");

                // bind(null) + local address
                AsynchronousServerSocketChannel ret = server.bind(null, 16);
                check(ret == server, "bind returns this");
                InetSocketAddress addr = (InetSocketAddress) server.getLocalAddress();
                check(addr.getPort() > 0, "ephemeral port");
                check(!server.supportedOptions().isEmpty(), "supported options");

                threw = false;
                try { server.bind(null); }
                catch (java.nio.channels.AlreadyBoundException e) { threw = true; }
                check(threw, "double bind -> AlreadyBoundException");

                // Future accept: client connects first
                try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                    Future<Void> cf = client.connect(addr);
                    check(cf.get(10, TimeUnit.SECONDS) == null, "client connected");
                    Future<AsynchronousSocketChannel> af = server.accept();
                    AsynchronousSocketChannel accepted = af.get(10, TimeUnit.SECONDS);
                    check(accepted != null, "accepted via Future");
                    check(accepted.getRemoteAddress() != null, "accepted remote address");
                    // echo one byte: client writes, accepted echoes back
                    client.write(ByteBuffer.wrap(new byte[] {7})).get(10, TimeUnit.SECONDS);
                    ByteBuffer buf = ByteBuffer.allocate(1);
                    check(accepted.read(buf).get(10, TimeUnit.SECONDS) == 1, "accepted read 1");
                    accepted.write((ByteBuffer) buf.flip()).get(10, TimeUnit.SECONDS);
                    ByteBuffer back = ByteBuffer.allocate(1);
                    check(client.read(back).get(10, TimeUnit.SECONDS) == 1, "client got echo");
                    check(back.get(0) == 7, "echo content");
                    accepted.close();
                }

                // CompletionHandler accept
                try (AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
                    client.connect(addr).get(10, TimeUnit.SECONDS);
                    CompletableFuture<AsynchronousSocketChannel> got = new CompletableFuture<>();
                    server.accept("attach", new CompletionHandler<AsynchronousSocketChannel, String>() {
                        public void completed(AsynchronousSocketChannel ch, String a) {
                            check("attach".equals(a), "attachment delivered");
                            got.complete(ch);
                        }
                        public void failed(Throwable t, String a) { got.completeExceptionally(t); }
                    });
                    AsynchronousSocketChannel accepted = got.get(10, TimeUnit.SECONDS);
                    check(accepted != null && accepted.isOpen(), "accepted via handler");
                    accepted.close();
                }

                // second concurrent accept -> AcceptPendingException
                Future<AsynchronousSocketChannel> pending = server.accept();
                try {
                    server.accept();
                    check(false, "second accept should throw AcceptPendingException");
                } catch (java.nio.channels.AcceptPendingException e) {
                    passed++;
                }
                pending.cancel(false);
            }
        } finally {
            group.shutdownNow();
        }

        // default-group open variant + option chaining
        try (AsynchronousServerSocketChannel s2 = AsynchronousServerSocketChannel.open()) {
            s2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 10);
            s2.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
            check(s2.getOption(StandardSocketOptions.SO_RCVBUF) >= 16 * 1024, "SO_RCVBUF");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
