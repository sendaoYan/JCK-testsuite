/*
 * @test
 * @summary java.nio.channels.DatagramChannel conformance: open/bind/state.
 * @run main DatagramChannelTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class DatagramChannelTest {
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

    static void testDatagramChannel() throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE), "validOps");
        check(!ch.isConnected(), "not connected");
        ch.bind(new InetSocketAddress(0));
        check(ch.getLocalAddress() != null, "getLocalAddress");
        check(ch.isBlocking(), "initially blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    // ===================== A6 deepening: send/receive semantics, connect/disconnect, options, close =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    static InetSocketAddress bindEphemeral(DatagramChannel dc) throws Exception {
        dc.bind(new InetSocketAddress(LO, 0));
        return (InetSocketAddress) dc.getLocalAddress();
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.DatagramChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "DatagramChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.MulticastChannel.class.isAssignableFrom(c),
              "implements MulticastChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "implements NetworkChannel");
        check(java.nio.channels.ByteChannel.class.isAssignableFrom(c), "implements ByteChannel");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "bind", "socket", "isConnected", "connect",
                "disconnect", "getRemoteAddress", "receive", "send", "read", "write",
                "getLocalAddress", "setOption", "getOption", "supportedOptions", "validOps"})
            check(names.contains(n), "has method " + n);
    }

    static void testOpenState() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            check(dc.isOpen(), "open");
            check(!dc.isConnected(), "not connected");
            check(dc.getLocalAddress() == null, "unbound: local address null");
            check(dc.getRemoteAddress() == null, "unconnected: remote address null");
            check(dc.socket() != null && !dc.socket().isBound(), "adaptor present, not bound");
            check(dc.isBlocking(), "default blocking");
            check(dc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE), "validOps");
            check(dc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            check(dc.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF),
                  "SO_RCVBUF supported");
            check(dc.supportedOptions().contains(StandardSocketOptions.IP_MULTICAST_TTL),
                  "IP_MULTICAST_TTL supported");
            check(dc.getOption(StandardSocketOptions.SO_RCVBUF) > 0, "default SO_RCVBUF positive");
        }
    }

    static void testSendAutoBinds() throws Exception {
        try (DatagramChannel rx = DatagramChannel.open();
             DatagramChannel tx = DatagramChannel.open()) {
            InetSocketAddress rxA = bindEphemeral(rx);
            check(tx.getLocalAddress() == null, "sender unbound before send");
            int n = tx.send(ByteBuffer.wrap(new byte[] {1, 2, 3}), rxA);
            check(n == 3, "send returns the datagram length");
            InetSocketAddress local = (InetSocketAddress) tx.getLocalAddress();
            check(local != null && local.getPort() > 0, "send implicitly binds an ephemeral port");
            check(local.getAddress().isAnyLocalAddress(), "implicit bind uses the wildcard address");
        }
    }

    static void testLoopbackSendReceive() throws Exception {
        try (DatagramChannel a = DatagramChannel.open();
             DatagramChannel b = DatagramChannel.open()) {
            InetSocketAddress aAddr = bindEphemeral(a);
            InetSocketAddress bAddr = bindEphemeral(b);
            byte[] payload = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
            ByteBuffer out = ByteBuffer.wrap(payload);
            check(b.send(out, aAddr) == payload.length, "all 10 bytes sent in one datagram");
            check(!out.hasRemaining(), "send consumed the whole buffer");
            ByteBuffer in = ByteBuffer.allocate(64);
            SocketAddress from = a.receive(in);
            check(from != null, "receive returns the sender address");
            check(from instanceof InetSocketAddress, "sender address is an InetSocketAddress");
            InetSocketAddress sa = (InetSocketAddress) from;
            check(sa.getAddress().equals(LO), "sender address is loopback");
            check(sa.getPort() == bAddr.getPort(), "sender port matches the bound sender port");
            check(in.position() == payload.length, "received the full datagram");
            boolean same = true;
            for (int i = 0; i < payload.length; i++)
                if (in.get(i) != payload[i]) { same = false; break; }
            check(same, "received payload byte-exact");
        }
    }

    static void testNonBlockingReceiveNull() throws Exception {
        try (DatagramChannel a = DatagramChannel.open()) {
            bindEphemeral(a);
            a.configureBlocking(false);
            check(a.receive(ByteBuffer.allocate(4)) == null,
                  "non-blocking receive with no packet returns null");
            check(a.receive(ByteBuffer.allocate(4)) == null, "still null on second call");
        }
    }

    static void testReceiveTruncation() throws Exception {
        try (DatagramChannel a = DatagramChannel.open();
             DatagramChannel b = DatagramChannel.open()) {
            InetSocketAddress aAddr = bindEphemeral(a);
            a.configureBlocking(false);
            try (Selector sel = Selector.open()) {
                a.register(sel, SelectionKey.OP_READ);
                check(sel.selectNow() == 0, "not ready before send");
                b.send(ByteBuffer.wrap(new byte[10]), aAddr);
                check(sel.select(5_000) == 1, "readable after send");
                ByteBuffer small = ByteBuffer.allocate(4);
                SocketAddress from = a.receive(small);
                check(from != null, "truncated datagram still reports its sender");
                check(small.position() == 4, "only 4 bytes fit, rest of the datagram discarded");
                check(sel.selectNow() == 0, "truncated datagram consumed completely");
            }
        }
    }

    static void testUnconnectedReadWrite() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            checkThrows(NotYetConnectedException.class, () -> dc.read(ByteBuffer.allocate(1)),
                        "blocking read unconnected -> NotYetConnectedException");
            checkThrows(NotYetConnectedException.class,
                        () -> dc.write(ByteBuffer.wrap(new byte[] {1})),
                        "blocking write unconnected -> NotYetConnectedException");
            dc.configureBlocking(false);
            checkThrows(NotYetConnectedException.class, () -> dc.read(ByteBuffer.allocate(1)),
                        "non-blocking read unconnected -> NotYetConnectedException");
        }
    }

    static void testConnectSemantics() throws Exception {
        try (DatagramChannel a = DatagramChannel.open();
             DatagramChannel b = DatagramChannel.open();
             DatagramChannel c = DatagramChannel.open()) {
            InetSocketAddress aAddr = bindEphemeral(a);
            InetSocketAddress bAddr = bindEphemeral(b);
            bindEphemeral(c);
            checkThrows(NullPointerException.class, () -> a.connect(null),
                        "connect(null) -> NullPointerException");
            check(a.connect(bAddr) == a, "connect returns this");
            check(a.isConnected(), "connected");
            InetSocketAddress remote = (InetSocketAddress) a.getRemoteAddress();
            check(remote != null && remote.getPort() == bAddr.getPort(), "remote is the peer");
            check(a.socket().isConnected(), "adaptor sees the connection");
            // JDK-8 impl: sending to a different address while connected -> IllegalArgumentException
            // (JDK-9+ NIO rewrite throws AlreadyConnectedException instead; mirrored on JDK-27 side)
            checkThrows(IllegalArgumentException.class,
                        () -> a.send(ByteBuffer.wrap(new byte[] {1}),
                                     (InetSocketAddress) c.getLocalAddress()),
                        "connected send to other address -> IllegalArgumentException (JDK-8 impl)");
            // connected read/write behave like a byte stream between exactly the two peers
            b.connect(aAddr);
            check(b.isConnected(), "peer connected back");
            ByteBuffer w = ByteBuffer.wrap(new byte[] {9, 8, 7, 6, 5});
            while (w.hasRemaining()) a.write(w);
            ByteBuffer r = ByteBuffer.allocate(5);
            while (r.hasRemaining()) check(b.read(r) >= 0, "peer readable");
            boolean same = true;
            for (int i = 0; i < 5; i++)
                if (r.get(i) != (byte) (9 - i)) { same = false; break; }
            check(same, "connected stream content exact");
            // disconnect returns to connectionless mode
            check(a.disconnect() == a, "disconnect returns this");
            check(!a.isConnected(), "disconnected");
            check(a.getRemoteAddress() == null, "remote cleared by disconnect");
            check(a.disconnect() == a, "disconnect when not connected is a no-op");
            int n = a.send(ByteBuffer.wrap(new byte[] {42}), aAddr);
            check(n == 1, "send anywhere allowed again after disconnect");
        }
    }

    static void testSocketBridge() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            java.net.DatagramSocket sock = dc.socket();
            check(sock.getChannel() == dc, "adaptor getChannel identity");
            check(!sock.isBound(), "adaptor not bound");
            InetSocketAddress a = bindEphemeral(dc);
            check(sock.isBound(), "adaptor bound after channel bind");
            check(sock.getLocalPort() == a.getPort(), "adaptor port matches channel port");
            check(!sock.isConnected(), "adaptor not connected");
            dc.connect(new InetSocketAddress(LO, a.getPort()));
            check(sock.isConnected(), "adaptor connected after channel connect");
            dc.disconnect();
            check(!sock.isConnected(), "adaptor disconnected after channel disconnect");
        }
    }

    static void testOptions() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            check(dc.setOption(StandardSocketOptions.SO_RCVBUF, 32 * 1024) == dc,
                  "setOption returns this");
            check(dc.getOption(StandardSocketOptions.SO_RCVBUF) >= 32 * 1024,
                  "SO_RCVBUF readback >= requested");
            check(dc.setOption(StandardSocketOptions.SO_REUSEADDR, true) == dc,
                  "SO_REUSEADDR settable");
            check(dc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback true");
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, false);
            check(!dc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback false");
            dc.setOption(StandardSocketOptions.SO_BROADCAST, true);
            check(dc.getOption(StandardSocketOptions.SO_BROADCAST), "SO_BROADCAST readback true");
            checkThrows(UnsupportedOperationException.class,
                        () -> dc.setOption(StandardSocketOptions.SO_KEEPALIVE, true),
                        "setOption(SO_KEEPALIVE) on datagram -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                        () -> dc.getOption(StandardSocketOptions.SO_KEEPALIVE),
                        "getOption(SO_KEEPALIVE) on datagram -> UnsupportedOperationException");
        }
    }

    static void testCloseSemantics() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        bindEphemeral(dc);
        dc.close();
        check(!dc.isOpen(), "closed");
        InetSocketAddress anywhere = new InetSocketAddress(LO, 9);
        checkThrows(ClosedChannelException.class,
                    () -> dc.send(ByteBuffer.wrap(new byte[] {1}), anywhere),
                    "send after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> dc.receive(ByteBuffer.allocate(1)),
                    "receive after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> dc.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class,
                    () -> dc.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> dc.connect(anywhere),
                    "connect after close -> ClosedChannelException");
        dc.close(); // idempotent
        check(!dc.isOpen(), "still closed after second close");
        check(dc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE),
              "validOps still queryable after close");
    }

    public static void main(String[] args) throws Exception {
        testDatagramChannel();
        testWhiteBoxStructure();
        testOpenState();
        testSendAutoBinds();
        testLoopbackSendReceive();
        testNonBlockingReceiveNull();
        testReceiveTruncation();
        testUnconnectedReadWrite();
        testConnectSemantics();
        testSocketBridge();
        testOptions();
        testCloseSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
