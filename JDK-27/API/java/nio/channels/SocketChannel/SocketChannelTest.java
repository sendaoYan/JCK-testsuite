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

    // ===================== A6 deepening: state machine / IO contracts / options / version-exact negatives =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    /** Throwing action so lambdas may call methods that declare checked IOException. */
    interface Op { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static void testOpenAndBindState() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            check(sc.isOpen(), "open");
            check(!sc.isConnected(), "not connected");
            check(!sc.isConnectionPending(), "no pending connection");
            check(sc.isBlocking(), "default blocking mode");
            check(sc.getLocalAddress() == null, "unbound: local address null");
            check(sc.getRemoteAddress() == null, "unconnected: remote address null");
            check(sc.socket() != null, "socket adaptor available");
            check(sc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            check(sc.bind(null) == sc, "bind(null) returns this");
            InetSocketAddress local = (InetSocketAddress) sc.getLocalAddress();
            check(local != null && local.getPort() > 0, "bound to ephemeral port");
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> sc.bind(null),
                        "second bind -> AlreadyBoundException");
            check(sc.socket().isBound(), "adaptor sees the binding");
        }
    }

    static void testUnconnectedOperations() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            checkThrows(java.nio.channels.NotYetConnectedException.class,
                        () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                        "blocking write unconnected -> NotYetConnectedException");
            sc.configureBlocking(false);
            checkThrows(java.nio.channels.NotYetConnectedException.class,
                        () -> sc.read(ByteBuffer.allocate(1)),
                        "non-blocking read unconnected -> NotYetConnectedException");
            checkThrows(java.nio.channels.NoConnectionPendingException.class,
                        () -> sc.finishConnect(),
                        "finishConnect without connect -> NoConnectionPendingException");
        }
    }

    static void testConnectFlows() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                checkThrows(java.nio.channels.AlreadyConnectedException.class,
                            () -> sc.connect(addr),
                            "connect while connected -> AlreadyConnectedException");
                InetSocketAddress remote = (InetSocketAddress) sc.getRemoteAddress();
                check(remote != null && remote.getPort() == addr.getPort(),
                      "remote address is the server");
                check(((InetSocketAddress) sc.getLocalAddress()).getPort() > 0,
                      "local address assigned");
                check(peer.getRemoteAddress() != null, "peer sees the client");
            }
            try (SocketChannel nb = SocketChannel.open()) {
                nb.configureBlocking(false);
                check(!nb.isConnected() && !nb.isConnectionPending(), "clean pre-connect state");
                boolean completedNow = nb.connect(addr);
                check(nb.isConnectionPending() != completedNow,
                      "pending iff connect did not complete immediately (completed=" + completedNow + ")");
                if (!completedNow) {
                    checkThrows(java.nio.channels.ConnectionPendingException.class,
                                () -> nb.connect(addr),
                                "connect while pending -> ConnectionPendingException");
                }
                nb.close();
            }
        }
    }

    static void testReadWriteExact() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                ByteBuffer w = ByteBuffer.wrap(new byte[] {8, 7, 6, 5, 4, 3, 2, 1});
                while (w.hasRemaining()) sc.write(w);
                ByteBuffer r = ByteBuffer.allocate(8);
                while (r.hasRemaining()) check(peer.read(r) >= 0, "peer readable");
                for (int i = 0; i < 8; i++)
                    check(r.get(i) == (byte) (8 - i), "byte " + i + " exact");
                byte[] big = new byte[64 * 1024];
                for (int i = 0; i < big.length; i++) big[i] = (byte) (i * 13 + 5);
                ByteBuffer bw = ByteBuffer.wrap(big);
                long total = 0;
                while (bw.hasRemaining()) total += sc.write(bw);
                check(total == big.length, "all 64 KiB written, total=" + total);
                ByteBuffer echo = ByteBuffer.allocate(big.length);
                while (echo.hasRemaining()) {
                    int n = peer.read(echo);
                    check(n >= 0, "peer still open while reading");
                }
                boolean same = true;
                for (int i = 0; i < big.length; i++)
                    if (echo.get(i) != big[i]) { same = false; break; }
                check(same, "64 KiB content byte-exact at the peer");
                echo.flip();
                while (echo.hasRemaining()) peer.write(echo);
                ByteBuffer fin = ByteBuffer.allocate(big.length);
                while (fin.hasRemaining()) {
                    int n = sc.read(fin);
                    check(n >= 0, "client still open while reading");
                }
                same = true;
                for (int i = 0; i < big.length; i++)
                    if (fin.get(i) != big[i]) { same = false; break; }
                check(same, "64 KiB echo byte-exact at the client");
            }
        }
    }

    static void testNonBlockingWriteProgress() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            try (SocketChannel sc = SocketChannel.open(
                    (InetSocketAddress) ssc.getLocalAddress());
                 SocketChannel peer = ssc.accept()) {
                sc.configureBlocking(false);
                peer.configureBlocking(false);
                ByteBuffer big = ByteBuffer.allocate(256 * 1024);
                ByteBuffer sink = ByteBuffer.allocate(64 * 1024);
                long total = 0;
                int spins = 0;
                while (big.hasRemaining() && spins < 200_000) {
                    int n = sc.write(big);
                    total += n;
                    if (n == 0) {
                        spins++;
                        while (peer.read(sink) > 0) sink.clear();
                    }
                }
                check(total == 256 * 1024, "non-blocking writes complete the buffer, total=" + total);
                check(!big.hasRemaining(), "buffer fully drained");
                check(spins < 200_000, "write loop bounded (spins=" + spins + ")");
            }
        }
    }

    static void testReadEofOnPeerClose() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            try (SocketChannel sc = SocketChannel.open(
                    (InetSocketAddress) ssc.getLocalAddress());
                 SocketChannel peer = ssc.accept()) {
                sc.configureBlocking(false);
                try (Selector sel = Selector.open()) {
                    sc.register(sel, SelectionKey.OP_READ);
                    check(sel.selectNow() == 0, "not readable before peer close");
                    peer.close(); // graceful close
                    check(sel.select(5_000) == 1, "readable after peer close");
                    ByteBuffer b = ByteBuffer.allocate(4);
                    check(sc.read(b) == -1, "read returns EOF (-1)");
                    check(sc.read(b) == -1, "EOF is sticky");
                    check(sc.isConnected(), "still marked connected (half-closed view)");
                }
            }
        }
    }

    static void testShutdownSemantics() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                check(!sc.socket().isOutputShutdown(), "output not shut down yet");
                check(sc.shutdownOutput() == sc, "shutdownOutput returns this");
                check(sc.socket().isOutputShutdown(), "adaptor sees output shutdown");
                // JDK-13+ NIO implementation: writing after shutdownOutput throws exactly
                // AsynchronousCloseException; the JDK-8 side of this suite mirrors this case
                // with ClosedChannelException (both are ClosedChannelException family).
                checkThrows(java.nio.channels.AsynchronousCloseException.class,
                            () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                            "write after shutdownOutput -> AsynchronousCloseException (JDK-13+ impl)");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(peer.read(b) == -1, "peer reads EOF after remote shutdownOutput");
                check(sc.isOpen(), "channel still open after shutdownOutput");
                // shutdownInput side
                check(sc.shutdownInput() == sc, "shutdownInput returns this");
                check(sc.socket().isInputShutdown(), "adaptor sees input shutdown");
                check(sc.read(ByteBuffer.allocate(4)) == -1, "read after shutdownInput is EOF");
                check(sc.read(ByteBuffer.allocate(4)) == -1, "EOF sticky after shutdownInput");
                check(sc.isOpen(), "channel still open after shutdownInput");
            }
        }
    }

    static void testConnectRefused() throws Exception {
        int port;
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            port = ((InetSocketAddress) ssc.getLocalAddress()).getPort();
        } // port now free
        try (SocketChannel sc = SocketChannel.open()) {
            checkThrows(java.net.ConnectException.class,
                        () -> sc.connect(new InetSocketAddress(LO, port)),
                        "blocking connect to refused port -> ConnectException");
        }
    }

    static void testCloseSemantics() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.close();
        check(!sc.isOpen(), "closed");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> sc.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> sc.connect(new InetSocketAddress(LO, 1)),
                    "connect after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> sc.finishConnect(),
                    "finishConnect after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> sc.bind(null),
                    "bind after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> sc.getOption(StandardSocketOptions.SO_SNDBUF),
                    "getOption after close -> ClosedChannelException");
        sc.close(); // idempotent
        check(!sc.isOpen(), "still closed after second close");
    }

    static void testSocketAdaptorBridge() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                java.net.Socket sock = sc.socket();
                check(sock.getChannel() == sc, "adaptor getChannel identity");
                check(sock.getPort() == addr.getPort(), "adaptor remote port matches");
                check(((InetSocketAddress) sock.getLocalSocketAddress()).getPort()
                      == ((InetSocketAddress) sc.getLocalAddress()).getPort(),
                      "adaptor local port matches");
                // blocking streams round-trip one byte
                peer.write(ByteBuffer.wrap(new byte[] {0x5A}));
                check(sock.getInputStream().read() == 0x5A, "stream read sees channel data");
                sock.getOutputStream().write(0xA5);
                ByteBuffer b = ByteBuffer.allocate(1);
                while (b.hasRemaining()) check(peer.read(b) >= 0, "peer readable");
                check(b.get(0) == (byte) 0xA5, "stream write reaches channel");
                // streams refuse to work in non-blocking mode
                sc.configureBlocking(false);
                checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                            () -> sc.socket().getInputStream().read(),
                            "stream read on non-blocking channel -> IllegalBlockingModeException");
                checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                            () -> sc.socket().getOutputStream().write(1),
                            "stream write on non-blocking channel -> IllegalBlockingModeException");
            }
        }
    }

    static void testOptionDetails() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            check(sc.setOption(StandardSocketOptions.TCP_NODELAY, true) == sc,
                  "setOption returns this");
            check(sc.getOption(StandardSocketOptions.TCP_NODELAY), "TCP_NODELAY true");
            sc.setOption(StandardSocketOptions.TCP_NODELAY, false);
            check(!sc.getOption(StandardSocketOptions.TCP_NODELAY), "TCP_NODELAY false");
            sc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            check(sc.getOption(StandardSocketOptions.SO_KEEPALIVE), "SO_KEEPALIVE true");
            sc.setOption(StandardSocketOptions.SO_KEEPALIVE, false);
            check(!sc.getOption(StandardSocketOptions.SO_KEEPALIVE), "SO_KEEPALIVE false");
            check(sc.supportedOptions().contains(StandardSocketOptions.SO_SNDBUF)
                  && sc.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF)
                  && sc.supportedOptions().contains(StandardSocketOptions.TCP_NODELAY),
                  "supportedOptions contains the core socket options");
            checkThrows(UnsupportedOperationException.class,
                        () -> sc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1),
                        "setOption(IP_MULTICAST_TTL) on stream socket -> UnsupportedOperationException");
        }
    }

    static void testBufferContracts() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            checkThrows(NullPointerException.class, () -> sc.write((ByteBuffer) null),
                        "write(null) -> NullPointerException");
            checkThrows(NullPointerException.class, () -> sc.read((ByteBuffer) null),
                        "read(null) -> NullPointerException");
        }
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            try (SocketChannel sc = SocketChannel.open(
                    (InetSocketAddress) ssc.getLocalAddress());
                 SocketChannel peer = ssc.accept()) {
                ByteBuffer w = ByteBuffer.allocate(4).put(new byte[] {1, 2, 3, 4}).flip();
                check(sc.write(w.asReadOnlyBuffer()) == 4, "write FROM a read-only view works");
                ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
                checkThrows(IllegalArgumentException.class, () -> sc.read(ro),
                            "read INTO read-only buffer -> IllegalArgumentException");
                peer.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOpenAndBindState();
        testUnconnectedOperations();
        testConnectFlows();
        testReadWriteExact();
        testNonBlockingWriteProgress();
        testReadEofOnPeerClose();
        testShutdownSemantics();
        testConnectRefused();
        testCloseSemantics();
        testSocketAdaptorBridge();
        testOptionDetails();
        testBufferContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
