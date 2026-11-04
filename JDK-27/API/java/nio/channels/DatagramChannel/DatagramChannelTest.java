/*
 * @test
 * @summary java.nio.channels.DatagramChannel conformance: white-box structure + black-box behavior.
 * @run main DatagramChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Set;

public class DatagramChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.DatagramChannel");
        check(Modifier.isAbstract(c.getModifiers()), "DatagramChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        for (Class<?> i : new Class<?>[] {java.nio.channels.ByteChannel.class,
                java.nio.channels.ScatteringByteChannel.class,
                java.nio.channels.GatheringByteChannel.class,
                java.nio.channels.MulticastChannel.class})
            check(i.isAssignableFrom(c), "implements " + i.getSimpleName());
        for (String n : new String[] {"open", "bind", "socket", "isConnected", "connect",
                "disconnect", "getRemoteAddress", "receive", "send", "read", "write",
                "getLocalAddress", "setOption", "validOps"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();

        try (DatagramChannel a = DatagramChannel.open();
             DatagramChannel b = DatagramChannel.open()) {
            check(a.isOpen(), "open");
            check(a.socket().isBound() == false, "not bound initially");

            // bind(null): ephemeral port on loopback
            a.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress aAddr = (InetSocketAddress) a.getLocalAddress();
            check(aAddr.getPort() > 0, "a bound to ephemeral port");
            check(a.socket().isBound(), "adaptor bound");
            b.bind(new InetSocketAddress(lo, 0));
            InetSocketAddress bAddr = (InetSocketAddress) b.getLocalAddress();

            // connectionless send/receive
            ByteBuffer out = ByteBuffer.wrap(new byte[] {1, 2, 3});
            int sent = b.send(out, aAddr);
            check(sent == 3, "sent 3 bytes");
            ByteBuffer in = ByteBuffer.allocate(16);
            SocketAddress from = a.receive(in);
            check(from != null, "received a packet");
            check(((InetSocketAddress) from).getPort() == bAddr.getPort(), "sender address matches");
            check(in.position() == 3 && in.get(0) == 1 && in.get(2) == 3, "packet content");

            // connect the pair: read/write stream semantics
            a.connect(bAddr);
            check(a.isConnected(), "a connected");
            b.connect(aAddr);
            ByteBuffer w = ByteBuffer.wrap(new byte[] {9, 9});
            while (w.hasRemaining()) a.write(w);
            ByteBuffer r = ByteBuffer.allocate(2);
            while (r.hasRemaining()) check(b.read(r) > 0, "connected read");
            check(r.get(0) == 9 && r.get(1) == 9, "connected content");
            check(a.getRemoteAddress() != null, "remote address after connect");
            check(a.socket().isConnected(), "adaptor connected");

            // scattering write / gathering read while connected
            ByteBuffer p1 = ByteBuffer.allocate(2);
            ByteBuffer p2 = ByteBuffer.allocate(2);
            p1.put(new byte[] {4, 4}).flip();
            p2.put(new byte[] {5, 5}).flip();
            long gw = a.write(new ByteBuffer[] {p1, p2}, 0, 2);
            check(gw == 4, "gathering write 4");
            ByteBuffer q1 = ByteBuffer.allocate(2);
            ByteBuffer q2 = ByteBuffer.allocate(2);
            long gr = b.read(new ByteBuffer[] {q1, q2}, 0, 2);
            check(gr == 4, "gathering read 4");
            check(q1.get(0) == 4 && q2.get(0) == 5, "gathered content");

            // disconnect returns to connectionless mode
            a.disconnect();
            check(!a.isConnected(), "disconnected");
            check(a.socket().isConnected() == false, "adaptor disconnected");

            // validOps
            int ops = a.validOps();
            check((ops & (java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE)) != 0,
                  "datagram validOps READ|WRITE");

            // options
            a.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 32 * 1024);
            check(a.getOption(java.net.StandardSocketOptions.SO_RCVBUF) >= 32 * 1024, "SO_RCVBUF set");
            check(!a.supportedOptions().isEmpty(), "supportedOptions non-empty");
        }

        // ProtocolFamily overload
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.bind(new InetSocketAddress(lo, 0));
            check(((InetSocketAddress) dc.getLocalAddress()).getPort() > 0, "INET family bound");
        }
        // UNIX domain is not a datagram family: rejected per spec
        boolean fam = false;
        try { DatagramChannel.open(java.net.StandardProtocolFamily.UNIX); }
        catch (UnsupportedOperationException e) { fam = true; }
        check(fam, "UNIX family datagram -> UnsupportedOperationException");

        // unconnected channel read/write rejected
        try (DatagramChannel u = DatagramChannel.open()) {
            boolean threw = false;
            try { u.read(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.NotYetConnectedException e) { threw = true; }
            check(threw, "read unconnected -> NotYetConnectedException");
            threw = false;
            try { u.write(ByteBuffer.wrap(new byte[] {1})); }
            catch (java.nio.channels.NotYetConnectedException e) { threw = true; }
            check(threw, "write unconnected -> NotYetConnectedException");
        }
    }

    // ===================== A6 deepening: send/receive semantics, connect/disconnect, options, close =====================

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

    static InetSocketAddress bindEphemeral(DatagramChannel dc) throws Exception {
        dc.bind(new InetSocketAddress(LO, 0));
        return (InetSocketAddress) dc.getLocalAddress();
    }

    static void testOpenState() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            check(dc.getLocalAddress() == null, "unbound: local address null");
            check(dc.getRemoteAddress() == null, "unconnected: remote address null");
            check(dc.socket() != null && !dc.socket().isBound(), "adaptor present, not bound");
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
            check(tx.socket().isBound(), "implicit bind visible in the adaptor");
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
            byte[] payload = {11, 21, 31, 41, 51, 61, 71, 81, 91, 101};
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
            try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
                a.register(sel, java.nio.channels.SelectionKey.OP_READ);
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
            // JDK-9+ NIO implementation: sending to a different address while connected throws
            // AlreadyConnectedException (the JDK-8 implementation threw IllegalArgumentException;
            // mirrored on the JDK-8 side of this suite).
            checkThrows(java.nio.channels.AlreadyConnectedException.class,
                        () -> a.send(ByteBuffer.wrap(new byte[] {1}),
                                     (InetSocketAddress) c.getLocalAddress()),
                        "connected send to other address -> AlreadyConnectedException (JDK-9+ impl)");
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
            check(a.disconnect() == a, "disconnect returns this");
            check(!a.isConnected(), "disconnected");
            check(a.getRemoteAddress() == null, "remote cleared by disconnect");
            check(a.disconnect() == a, "disconnect when not connected is a no-op");
            check(a.send(ByteBuffer.wrap(new byte[] {42}), aAddr) == 1,
                  "send anywhere allowed again after disconnect");
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

    static void testOptionDetails() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
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
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> dc.send(ByteBuffer.wrap(new byte[] {1}), anywhere),
                    "send after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> dc.receive(ByteBuffer.allocate(1)),
                    "receive after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> dc.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> dc.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> dc.connect(anywhere),
                    "connect after close -> ClosedChannelException");
        dc.close(); // idempotent
        check(!dc.isOpen(), "still closed after second close");
        check(dc.validOps() == (java.nio.channels.SelectionKey.OP_READ
                                | java.nio.channels.SelectionKey.OP_WRITE),
              "validOps still queryable after close");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOpenState();
        testSendAutoBinds();
        testLoopbackSendReceive();
        testNonBlockingReceiveNull();
        testReceiveTruncation();
        testConnectSemantics();
        testSocketBridge();
        testOptionDetails();
        testCloseSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
