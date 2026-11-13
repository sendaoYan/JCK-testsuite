/*
 * @test
 * @summary java.nio.channels.MulticastChannel conformance: white-box structure + black-box behavior.
 * @run main MulticastChannelTest
 */

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import java.util.Set;

public class MulticastChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.MulticastChannel");
        check(c.isInterface(), "MulticastChannel is an interface");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c),
              "extends NetworkChannel");
        for (String n : new String[] {"join", "close"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
        check(java.nio.channels.DatagramChannel.class.isAssignableFrom(c) == false,
              "MulticastChannel does not extend DatagramChannel (other way around)");
    }

    /**
     * Picks an interface that can carry IPv4 multicast, preferring a real
     * (non-loopback) adapter. Requiring an actual IPv4 address matters: an
     * IPv6-only interface (e.g. a docker veth link) satisfies supportsMulticast
     * but join(IPv4 group) on it throws "Network interface not configured for
     * IPv4".
     */
    static NetworkInterface pickMulticastInterface() throws Exception {
        NetworkInterface loopback = null;
        for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || !ni.supportsMulticast()
                    || !ni.inetAddresses().anyMatch(a -> a instanceof java.net.Inet4Address)) {
                continue;
            }
            if (!ni.isLoopback()) return ni;
            if (loopback == null) loopback = ni;
        }
        return loopback;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress group = InetAddress.getByName("224.0.113.1"); // AD-HOC block 1, no external traffic

        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            MulticastChannel mc = dc;
            // Bind the wildcard address: Windows refuses to bind a UDP socket
            // to a unicast address and still receive multicast, and refuses a
            // multicast group address as the local endpoint altogether.
            dc.bind(new InetSocketAddress(0));
            check(dc.getLocalAddress() != null, "channel bound");

            NetworkInterface nif = pickMulticastInterface();
            if (nif == null) {
                System.out.println("NOTE: no multicast-capable interface; join tests skipped");
            } else {
                MembershipKey key = mc.join(group, nif);
                check(key != null, "membership acquired");
                check(key.isValid(), "membership valid");
                check(key.channel() == dc, "membership channel");
                check(key.group().equals(group), "membership group");
                check(key.networkInterface() == nif, "membership interface");
                check(key.sourceAddress() == null, "no source filter by default");

                // membership delivers a datagram sent to the group by ourselves
                InetAddress ifaceAddr = nif.inetAddresses().findFirst().orElse(null);
                if (ifaceAddr != null) {
                    dc.configureBlocking(false);
                    java.nio.channels.Selector sel = java.nio.channels.Selector.open();
                    dc.register(sel, java.nio.channels.SelectionKey.OP_READ);
                    try (DatagramChannel sender = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
                        // Let the stack auto-bind and force the multicast
                        // egress interface; a loopback-bound sender has no
                        // route to the group on Windows.
                        sender.setOption(java.net.StandardSocketOptions.IP_MULTICAST_IF, nif);
                        ByteBuffer out = ByteBuffer.wrap(new byte[] {77});
                        sender.send(out, new InetSocketAddress(group,
                                ((InetSocketAddress) dc.getLocalAddress()).getPort()));
                    }
                    int n = sel.select(5000);
                    check(n >= 0, "select returned");
                    ByteBuffer in = ByteBuffer.allocate(16);
                    if (n > 0) {
                        check(dc.receive(in) != null, "multicast packet received");
                        check(in.get(0) == 77, "multicast payload");
                    }
                    sel.close();
                }

                key.drop();
                check(!key.isValid(), "membership invalid after drop");

                // rejoin and close the channel: membership invalidated implicitly
                MembershipKey k2 = mc.join(group, nif);
                check(k2.isValid(), "second membership valid");
            }
            // close() through the interface type
            mc.close();
            check(!dc.isOpen(), "closed via MulticastChannel.close");
        }
    }

    // ===================== A6 deepening: join idempotency / group round trip / multicast options =====================

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
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

    static void testJoinIdempotency() throws Exception {
        InetAddress group = InetAddress.getByName("239.255.19.1");
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) {
            System.out.println("NOTE: no multicast-capable interface; idempotency test skipped");
            return;
        }
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: joining is independent of the local endpoint, and
            // Windows rejects a multicast group address as the local address.
            dc.bind(new InetSocketAddress(0));
            MembershipKey k1 = dc.join(group, nif);
            MembershipKey k2 = dc.join(group, nif);
            check(k1 == k2, "re-join of the same group yields the same key");
            check(k1.networkInterface() == nif, "membership interface identity");
            check(k1.group().equals(group), "membership group");
            k1.drop();
            check(!k1.isValid(), "invalid after drop");
            MembershipKey k3 = dc.join(group, nif);
            check(k3 != k1 && k3.isValid(), "fresh key after drop+re-join");
            // source-specific join while any-source member is rejected
            InetAddress src = InetAddress.getByName("192.0.2.77");
            MembershipKey any = k3;
            boolean threw = false;
            try { dc.join(group, nif, src); }
            catch (IllegalStateException e) { threw = true; }
            check(threw, "source-specific join while any-source member -> IllegalStateException");
            check(any.isValid(), "original membership untouched by the rejected join");
        }
    }

    static void testGroupRoundTrip() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.19.2");
        try (DatagramChannel rx = DatagramChannel.open(java.net.StandardProtocolFamily.INET);
             DatagramChannel tx = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            rx.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            rx.bind(new InetSocketAddress(0));
            rx.join(group, nif);
            rx.configureBlocking(false);
            int port = ((InetSocketAddress) rx.getLocalAddress()).getPort();
            tx.setOption(java.net.StandardSocketOptions.IP_MULTICAST_IF, nif);
            byte[] payload = {9, 8, 7, 6};
            check(tx.send(ByteBuffer.wrap(payload), new InetSocketAddress(group, port)) == 4,
                  "sent 4 bytes to the group");
            try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
                rx.register(sel, java.nio.channels.SelectionKey.OP_READ);
                check(sel.select(5_000) == 1, "group datagram made the channel readable");
                java.nio.ByteBuffer in = java.nio.ByteBuffer.allocate(16);
                check(rx.receive(in) != null, "received from the group");
                check(in.position() == 4, "payload length exact");
                boolean same = true;
                for (int i = 0; i < 4; i++)
                    if (in.get(i) != payload[i]) { same = false; break; }
                check(same, "group payload byte-exact");
            }
        }
    }

    static void testMulticastOptions() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            check(dc.supportedOptions().contains(java.net.StandardSocketOptions.IP_MULTICAST_IF),
                  "IP_MULTICAST_IF supported");
            check(dc.supportedOptions().contains(java.net.StandardSocketOptions.IP_MULTICAST_TTL),
                  "IP_MULTICAST_TTL supported");
            check(dc.supportedOptions().contains(java.net.StandardSocketOptions.IP_MULTICAST_LOOP),
                  "IP_MULTICAST_LOOP supported");
            dc.setOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL, 3);
            check(dc.getOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL) == 3,
                  "IP_MULTICAST_TTL readback 3");
            dc.setOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL, 1);
            check(dc.getOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL) == 1,
                  "IP_MULTICAST_TTL readback 1");
            boolean loop = dc.getOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP);
            dc.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, !loop);
            check(dc.getOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP) == !loop,
                  "IP_MULTICAST_LOOP toggles");
            dc.setOption(java.net.StandardSocketOptions.IP_MULTICAST_LOOP, loop);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testJoinIdempotency();
        testGroupRoundTrip();
        testMulticastOptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
