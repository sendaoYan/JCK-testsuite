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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
