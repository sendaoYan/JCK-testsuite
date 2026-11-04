/*
 * @test
 * @summary java.nio.channels.SocketChannel conformance: open/validOps/state.
 * @run main SocketChannelTest
 */

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static void testSocketChannel() throws Exception {
        SocketChannel ch = SocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE |
                SelectionKey.OP_CONNECT), "validOps");
        check(!ch.isConnected(), "not connected before connect");
        check(ch.isBlocking(), "initially blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    // ===================== A6 deepening: state machine / connect flows / IO contracts / options =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.SocketChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SocketChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c), "implements NetworkChannel");
        check(java.nio.channels.ByteChannel.class.isAssignableFrom(c), "implements ByteChannel");
        check(java.nio.channels.ScatteringByteChannel.class.isAssignableFrom(c),
              "implements ScatteringByteChannel");
        check(java.nio.channels.GatheringByteChannel.class.isAssignableFrom(c),
              "implements GatheringByteChannel");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "bind", "connect", "finishConnect", "isConnected",
                "isConnectionPending", "socket", "shutdownInput", "shutdownOutput", "read", "write",
                "getRemoteAddress", "getLocalAddress", "setOption", "getOption",
                "supportedOptions", "validOps"})
            check(names.contains(n), "has method " + n);
    }

    static void testOpenState() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            check(sc.isOpen(), "open");
            check(!sc.isConnected(), "not connected");
            check(!sc.isConnectionPending(), "no pending connection");
            check(sc.isBlocking(), "default blocking mode");
            check(sc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE
                                    | SelectionKey.OP_CONNECT), "validOps mask");
            check(sc.getLocalAddress() == null, "unbound: local address null");
            check(sc.getRemoteAddress() == null, "unconnected: remote address null");
            check(!sc.isRegistered(), "not registered");
            check(sc.socket() != null, "socket adaptor available");
            check(sc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
        }
    }

    static void testUnconnectedOperations() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            checkThrows(NotYetConnectedException.class,
                        () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                        "blocking write unconnected -> NotYetConnectedException");
            checkThrows(NotYetConnectedException.class, () -> sc.read(ByteBuffer.allocate(1)),
                        "blocking read unconnected -> NotYetConnectedException");
            sc.configureBlocking(false);
            checkThrows(NotYetConnectedException.class, () -> sc.read(ByteBuffer.allocate(1)),
                        "non-blocking read unconnected -> NotYetConnectedException");
            checkThrows(NoConnectionPendingException.class, () -> sc.finishConnect(),
                        "finishConnect without connect -> NoConnectionPendingException");
        }
    }

    static void testBindSemantics() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            check(sc.bind(null) == sc, "bind(null) returns this");
            InetSocketAddress local = (InetSocketAddress) sc.getLocalAddress();
            check(local != null && local.getPort() > 0, "bound to ephemeral port");
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> sc.bind(null),
                        "second bind -> AlreadyBoundException");
            check(sc.socket().isBound(), "adaptor sees the binding");
        }
    }

    static void testBlockingConnectFlow() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                check(sc.isConnected(), "blocking connect completes synchronously");
                check(!sc.isConnectionPending(), "no pending after connect");
                checkThrows(AlreadyConnectedException.class, () -> sc.connect(addr),
                            "connect while connected -> AlreadyConnectedException");
                InetSocketAddress remote = (InetSocketAddress) sc.getRemoteAddress();
                check(remote != null && remote.getPort() == addr.getPort(),
                      "remote address is the server");
                InetSocketAddress local = (InetSocketAddress) sc.getLocalAddress();
                check(local != null && local.getPort() > 0, "local address assigned");
                check(peer.getRemoteAddress() != null, "peer sees the client");
                check(sc.finishConnect(), "finishConnect true on connected channel");
            }
        }
    }

    static void testNonBlockingConnectFlow() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(); Selector sel = Selector.open()) {
                sc.configureBlocking(false);
                check(!sc.isConnected() && !sc.isConnectionPending(), "clean pre-connect state");
                boolean completedNow = sc.connect(addr);
                check(sc.isConnectionPending() != completedNow,
                      "pending iff connect did not complete immediately (completed=" + completedNow + ")");
                if (!completedNow) {
                    checkThrows(ConnectionPendingException.class, () -> sc.connect(addr),
                                "connect while pending -> ConnectionPendingException");
                    SelectionKey key = sc.register(sel, SelectionKey.OP_CONNECT);
                    check(sel.select(5_000) == 1, "OP_CONNECT readiness");
                    check(key.isConnectable(), "isConnectable");
                    check(sc.finishConnect(), "finishConnect completes");
                }
                check(sc.isConnected(), "connected after finishConnect");
                check(!sc.isConnectionPending(), "pending cleared");
                check(sc.finishConnect(), "finishConnect idempotent when connected");
                checkThrows(AlreadyConnectedException.class, () -> sc.connect(addr),
                            "connect after connected -> AlreadyConnectedException");
            }
        }
    }

    static void testReadWriteExact() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                // small payload, client -> peer
                ByteBuffer w = ByteBuffer.wrap(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
                while (w.hasRemaining()) sc.write(w);
                ByteBuffer r = ByteBuffer.allocate(8);
                while (r.hasRemaining()) check(peer.read(r) >= 0, "peer readable");
                for (int i = 0; i < 8; i++)
                    check(r.get(i) == (byte) (i + 1), "byte " + i + " exact");
                // 64 KiB pattern, client -> peer, verified at the peer
                byte[] big = new byte[64 * 1024];
                for (int i = 0; i < big.length; i++) big[i] = (byte) (i * 31 + 7);
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
                // and the same pattern echoed back, peer -> client
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

    static void testGatheringScattering() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            try (SocketChannel sc = SocketChannel.open(
                    (InetSocketAddress) ssc.getLocalAddress());
                 SocketChannel peer = ssc.accept()) {
                ByteBuffer a = ByteBuffer.allocate(2);
                ByteBuffer b = ByteBuffer.allocate(2);
                a.put(new byte[] {0x0A, 0x0B}).flip();
                b.put(new byte[] {0x0C, 0x0D}).flip();
                long gw = sc.write(new ByteBuffer[] {a, b});
                check(gw == 4, "gathering write of two buffers = 4, got " + gw);
                ByteBuffer x = ByteBuffer.allocate(3);
                ByteBuffer y = ByteBuffer.allocate(1);
                long total = 0;
                while (x.hasRemaining() || y.hasRemaining())
                    total += peer.read(new ByteBuffer[] {x, y});
                check(total == 4, "scattering read total 4, got " + total);
                check(x.get(0) == 0x0A && x.get(1) == 0x0B && x.get(2) == 0x0C
                      && y.get(0) == 0x0D, "scattered order preserved");
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
                        // free the kernel buffers so progress continues (bounded per spin)
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
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
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

    static void testShutdownOutput() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                check(sc.socket().isOutputShutdown() == false, "output not shut down yet");
                check(sc.shutdownOutput() == sc, "shutdownOutput returns this");
                check(sc.socket().isOutputShutdown(), "adaptor sees output shutdown");
                // JDK-8: writing after shutdownOutput throws the exact ClosedChannelException
                // (JDK-13+ NIO rewrite throws AsynchronousCloseException instead; mirrored on JDK-27 side)
                checkThrows(ClosedChannelException.class,
                            () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                            "write after shutdownOutput -> ClosedChannelException (JDK-8 impl)");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(peer.read(b) == -1, "peer reads EOF after remote shutdownOutput");
                check(sc.isOpen(), "channel still open after shutdownOutput");
            }
        }
    }

    static void testShutdownInput() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel sc = SocketChannel.open(addr); SocketChannel peer = ssc.accept()) {
                ByteBuffer w = ByteBuffer.wrap(new byte[] {(byte) 0xFE});
                while (w.hasRemaining()) peer.write(w);
                ByteBuffer warm = ByteBuffer.allocate(1);
                while (warm.hasRemaining()) check(sc.read(warm) >= 0, "warm byte readable");
                check(sc.socket().isInputShutdown() == false, "input not shut down yet");
                check(sc.shutdownInput() == sc, "shutdownInput returns this");
                check(sc.socket().isInputShutdown(), "adaptor sees input shutdown");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(sc.read(b) == -1, "read after shutdownInput is EOF");
                check(sc.read(b) == -1, "EOF sticky after shutdownInput");
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
            checkThrows(ConnectException.class,
                        () -> sc.connect(new InetSocketAddress(LO, port)),
                        "blocking connect to refused port -> ConnectException");
        }
    }

    static void testCloseSemantics() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.close();
        check(!sc.isOpen(), "closed");
        checkThrows(ClosedChannelException.class, () -> sc.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> sc.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class,
                    () -> sc.connect(new InetSocketAddress(LO, 1)),
                    "connect after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> sc.finishConnect(),
                    "finishConnect after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> sc.bind(null),
                    "bind after close -> ClosedChannelException");
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
                check(sock.isConnected(), "adaptor connected");
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
                checkThrows(IllegalBlockingModeException.class,
                            () -> sc.socket().getInputStream().read(),
                            "stream read on non-blocking channel -> IllegalBlockingModeException");
                checkThrows(IllegalBlockingModeException.class,
                            () -> sc.socket().getOutputStream().write(1),
                            "stream write on non-blocking channel -> IllegalBlockingModeException");
            }
        }
    }

    static void testOptions() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            check(sc.setOption(StandardSocketOptions.SO_SNDBUF, 32 * 1024) == sc,
                  "setOption returns this");
            check(sc.getOption(StandardSocketOptions.SO_SNDBUF) >= 32 * 1024,
                  "SO_SNDBUF readback >= requested");
            sc.setOption(StandardSocketOptions.SO_RCVBUF, 32 * 1024);
            check(sc.getOption(StandardSocketOptions.SO_RCVBUF) >= 32 * 1024,
                  "SO_RCVBUF readback >= requested");
            sc.setOption(StandardSocketOptions.TCP_NODELAY, true);
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
                ByteBuffer w = ByteBuffer.allocate(4);
                w.put(new byte[] {1, 2, 3, 4}).flip(); // Buffer.flip() returns Buffer before JDK-9
                check(sc.write(w.asReadOnlyBuffer()) == 4, "write FROM a read-only view works");
                ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
                checkThrows(IllegalArgumentException.class, () -> sc.read(ro),
                            "read INTO read-only buffer -> IllegalArgumentException");
                peer.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testSocketChannel();
        testWhiteBoxStructure();
        testOpenState();
        testUnconnectedOperations();
        testBindSemantics();
        testBlockingConnectFlow();
        testNonBlockingConnectFlow();
        testReadWriteExact();
        testGatheringScattering();
        testNonBlockingWriteProgress();
        testReadEofOnPeerClose();
        testShutdownOutput();
        testShutdownInput();
        testConnectRefused();
        testCloseSemantics();
        testSocketAdaptorBridge();
        testOptions();
        testBufferContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
