/*
 * @test
 * @summary java.nio.channels.SocketChannel conformance: white-box structure + black-box behavior.
 * @run main SocketChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class SocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Daemon thread: accept connections on ssc and echo all bytes back until ssc is closed. */
    static Thread startEchoServer(ServerSocketChannel ssc) throws IOException {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    SocketChannel peer;
                    try {
                        peer = ssc.accept();
                    } catch (Exception e) {
                        return; // server closed
                    }
                    ByteBuffer buf = ByteBuffer.allocate(4096);
                    while (true) {
                        buf.clear();
                        int n = peer.read(buf);
                        if (n < 0) break;
                        buf.flip();
                        while (buf.hasRemaining()) peer.write(buf);
                    }
                    peer.close();
                }
            } catch (Exception ignore) { }
        }, "echo-server");
        t.setDaemon(true);
        t.start();
        return t;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.SocketChannel");
        check(Modifier.isAbstract(c.getModifiers()), "SocketChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        for (Class<?> i : new Class<?>[] {java.nio.channels.ByteChannel.class,
                java.nio.channels.ScatteringByteChannel.class,
                java.nio.channels.GatheringByteChannel.class,
                java.nio.channels.NetworkChannel.class})
            check(i.isAssignableFrom(c), "implements " + i.getSimpleName());
        Set<String> names = new java.util.HashSet<>();
        for (Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "bind", "connect", "finishConnect", "isConnected",
                "isConnectionPending", "socket", "shutdownInput", "shutdownOutput", "read", "write",
                "getRemoteAddress", "getLocalAddress", "setOption", "validOps"})
            check(names.contains(n), "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            check(addr.getPort() > 0, "ephemeral port bound");
            Thread echo = startEchoServer(ssc);

            // blocking connect + echo round trip
            try (SocketChannel sc = SocketChannel.open(addr)) {
                check(sc.isConnected(), "connected");
                check(!sc.isConnectionPending(), "no pending after connect");
                check(sc.finishConnect(), "finishConnect true on connected channel");
                check(sc.getRemoteAddress() != null, "remote address known");
                check(sc.getLocalAddress() != null, "local address known");

                byte[] payload = {1, 2, 3, 4, 5, 6, 7, 8};
                ByteBuffer w = ByteBuffer.wrap(payload);
                while (w.hasRemaining()) sc.write(w);
                ByteBuffer r = ByteBuffer.allocate(8);
                while (r.hasRemaining()) {
                    int n = sc.read(r);
                    check(n >= 0, "peer still open");
                }
                check(java.util.Arrays.equals(r.array(), payload), "echo round trip content");

                // gathering write echoed back, read via scatter
                ByteBuffer h1 = ByteBuffer.allocate(4);
                ByteBuffer h2 = ByteBuffer.allocate(4);
                h1.put(new byte[] {9, 9, 9, 9}).flip();
                h2.put(new byte[] {8, 8, 8, 8}).flip();
                long gw = sc.write(new ByteBuffer[] {h1, h2}, 0, 2);
                check(gw == 8, "gathering write 8 bytes");
                ByteBuffer a = ByteBuffer.allocate(4);
                ByteBuffer b = ByteBuffer.allocate(4);
                while (a.hasRemaining()) check(sc.read(a) >= 0, "read a");
                while (b.hasRemaining()) check(sc.read(b) >= 0, "read b");
                check(a.get(0) == 9 && a.get(3) == 9, "scatter part1 content");
                check(b.get(0) == 8 && b.get(3) == 8, "scatter part2 content");

                // shutdownOutput: server observes EOF; shutdownInput: local reads give EOF
                sc.shutdownOutput();
                ByteBuffer eof = ByteBuffer.allocate(1);
                sc.shutdownInput();
                int n = sc.read(eof);
                check(n == -1, "read after shutdownInput is EOF");

                // options via NetworkChannel contract
                sc.setOption(StandardSocketOptions.SO_SNDBUF, 32 * 1024);
                check(sc.getOption(StandardSocketOptions.SO_SNDBUF) >= 32 * 1024, "SO_SNDBUF set");
                check(!sc.supportedOptions().isEmpty(), "supportedOptions non-empty");

                // validOps bits
                int ops = sc.validOps();
                check((ops & (SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT)) != 0,
                      "validOps has READ|WRITE|CONNECT");
                check((ops & SelectionKey.OP_ACCEPT) == 0, "validOps lacks ACCEPT");

                // socket() adaptor reflects connection state
                check(sc.socket().isConnected(), "adaptor socket connected");
                check(sc.socket().getPort() == addr.getPort(), "adaptor port matches");
            }

            // non-blocking connect + finishConnect + selector readiness
            try (SocketChannel nb = SocketChannel.open()) {
                check(nb.isBlocking(), "new channel defaults to blocking");
                nb.configureBlocking(false);
                check(!nb.isBlocking(), "configureBlocking(false)");
                Object lock = new Object();
                check(nb.blockingLock() != null, "blockingLock non-null");
                boolean done = nb.connect(addr);
                if (!done) {
                    check(nb.isConnectionPending(), "connection pending");
                    boolean pendingThrew = false;
                    try { nb.connect(addr); }
                    catch (java.nio.channels.ConnectionPendingException e) { pendingThrew = true; }
                    check(pendingThrew, "second connect -> ConnectionPendingException");
                    try (Selector sel = Selector.open()) {
                        nb.register(sel, SelectionKey.OP_CONNECT);
                        int n = sel.select(5000);
                        check(n > 0, "OP_CONNECT selected");
                        check(nb.finishConnect(), "finishConnect after readiness");
                    }
                }
                check(nb.isConnected(), "non-blocking connect completed");
                check(nb.finishConnect(), "finishConnect idempotent on connected");
                nb.close();
            }

            // unconnected channel: operations rejected
            try (SocketChannel u = SocketChannel.open()) {
                boolean threw = false;
                try { u.write(ByteBuffer.wrap(new byte[] {1})); }
                catch (java.nio.channels.NotYetConnectedException e) { threw = true; }
                check(threw, "write unconnected -> NotYetConnectedException");
                threw = false;
                try { u.read(ByteBuffer.allocate(1)); }
                catch (java.nio.channels.NotYetConnectedException e) { threw = true; }
                check(threw, "read unconnected -> NotYetConnectedException");
                threw = false;
                try { u.finishConnect(); }
                catch (java.nio.channels.NoConnectionPendingException e) { threw = true; }
                check(threw, "finishConnect without connect -> NoConnectionPendingException");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
