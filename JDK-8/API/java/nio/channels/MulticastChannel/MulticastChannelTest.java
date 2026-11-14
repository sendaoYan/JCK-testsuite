/*
 * @test
 * @summary java.nio.channels.MulticastChannel conformance: interface hierarchy and methods.
 * @run main MulticastChannelTest
 */

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.MulticastChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MulticastChannelTest {
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

    static void testMulticastChannel() {
        check(MulticastChannel.class.isInterface(), "is interface");
        check(NetworkChannel.class.isAssignableFrom(MulticastChannel.class), "extends NetworkChannel");
        Set<String> names = new HashSet<String>();
        for (Method m : MulticastChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "join", "close" };
        for (String n : required) check(names.contains(n), "has method " + n);
        // two join overloads: any-source and source-filtering
        int joinOverloads = 0;
        for (Method m : MulticastChannel.class.getMethods())
            if (m.getName().equals("join")) joinOverloads++;
        check(joinOverloads == 2, "two join overloads (any-source + source-specific)");
        check(MulticastChannel.class.isAssignableFrom(DatagramChannel.class),
              "DatagramChannel implements MulticastChannel");
        check(DatagramChannel.class.isAssignableFrom(MulticastChannel.class) == false,
              "MulticastChannel does not extend DatagramChannel");
    }

    // ===================== A6 deepening: join/drop lifecycle, group round trip, multicast options =====================

    /** Picks an up, multicast-capable, non-loopback interface with an IPv4 address (probe-verified on this host). */
    static NetworkInterface pickMulticastInterface() throws Exception {
        NetworkInterface fallback = null;
        for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || !ni.supportsMulticast()) continue;
            boolean hasV4 = false;
            for (InetAddress a : Collections.list(ni.getInetAddresses()))
                if (a instanceof java.net.Inet4Address) hasV4 = true;
            if (!hasV4) continue;
            if (!ni.isLoopback()) return ni;
            if (fallback == null) fallback = ni;
        }
        return fallback;
    }

    static void testJoinAndGetters() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) {
            System.out.println("NOTE: no multicast-capable interface; join behavioral tests skipped");
            return;
        }
        InetAddress group = InetAddress.getByName("239.255.16.1");
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(0)); // wildcard: Windows rejects a group address
            MulticastChannel mc = dc;
            MembershipKey key = mc.join(group, nif);
            check(key != null, "join returns a membership key");
            check(key.isValid(), "membership valid");
            check(key.channel() == dc, "membership channel");
            check(key.group().equals(group), "membership group");
            check(key.networkInterface() == nif, "membership interface");
            check(key.sourceAddress() == null, "any-source membership has no source");
            check(key.networkInterface().getName().equals(nif.getName()), "interface name matches");
            // joining the same group twice returns the SAME key (idempotent implementation)
            MembershipKey again = mc.join(group, nif);
            check(again == key, "re-join of the same group yields the same key");
            key.drop();
            check(!key.isValid(), "invalid after drop");
            MembershipKey rejoined = mc.join(group, nif);
            check(rejoined != key, "drop then re-join produces a fresh key");
            check(rejoined.isValid(), "rejoined membership valid");
        }
    }

    static void testGroupRoundTrip() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.16.2");
        try (DatagramChannel rx = DatagramChannel.open(StandardProtocolFamily.INET);
             DatagramChannel tx = DatagramChannel.open(StandardProtocolFamily.INET)) {
            rx.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: multicast reception is driven by the membership;
            // binding the unicast interface address would not match group
            // traffic, and Windows rejects binding the group address itself.
            rx.bind(new InetSocketAddress(0));
            MulticastChannel mc = rx;
            mc.join(group, nif);
            rx.configureBlocking(false);
            int port = ((InetSocketAddress) rx.getLocalAddress()).getPort();
            tx.setOption(StandardSocketOptions.IP_MULTICAST_IF, nif);
            byte[] payload = {1, 2, 3, 4, 5};
            check(tx.send(ByteBuffer.wrap(payload), new InetSocketAddress(group, port)) == 5,
                  "sent 5 bytes to the group");
            try (Selector sel = Selector.open()) {
                rx.register(sel, SelectionKey.OP_READ);
                check(sel.select(5_000) == 1, "group datagram made the channel readable");
                ByteBuffer in = ByteBuffer.allocate(16);
                SocketAddress from = rx.receive(in);
                check(from != null, "received from the group");
                check(in.position() == 5, "payload length exact");
                boolean same = true;
                for (int i = 0; i < 5; i++)
                    if (in.get(i) != payload[i]) { same = false; break; }
                check(same, "group payload byte-exact");
            }
        }
    }

    static void testJoinNegativesAndClose() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.16.3");
        InetAddress src = InetAddress.getByName("127.0.0.1");
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(0)); // wildcard: Windows rejects a group address
            MulticastChannel mc = dc;
            MembershipKey any = mc.join(group, nif);
            // any-source membership + source-specific join for the same group -> IllegalStateException
            checkThrows(IllegalStateException.class, () -> mc.join(group, nif, src),
                        "source-specific join while any-source member -> IllegalStateException");
            check(any.isValid(), "failed join left the original membership intact");
            any.drop();
            // now source-specific join is possible on the dropped group
            MembershipKey specific = mc.join(group, nif, src);
            check(specific.isValid(), "source-specific membership after drop");
            check(specific.sourceAddress().equals(src), "source address recorded");
            specific.drop();
        }
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(0)); // wildcard: Windows rejects a group address
            MulticastChannel mc = dc;
            MembershipKey key = mc.join(group, nif);
            check(key.isValid(), "valid before close");
            mc.close(); // close() through the interface type
            check(!dc.isOpen(), "closed via MulticastChannel.close");
            check(!key.isValid(), "membership invalidated by channel close");
            checkThrows(ClosedChannelException.class, () -> mc.join(group, nif),
                        "join after channel close -> ClosedChannelException");
        }
    }

    static void testMulticastOptions() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            check(dc.supportedOptions().contains(StandardSocketOptions.IP_MULTICAST_IF),
                  "IP_MULTICAST_IF supported");
            check(dc.supportedOptions().contains(StandardSocketOptions.IP_MULTICAST_TTL),
                  "IP_MULTICAST_TTL supported");
            check(dc.supportedOptions().contains(StandardSocketOptions.IP_MULTICAST_LOOP),
                  "IP_MULTICAST_LOOP supported");
            dc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 3);
            check(dc.getOption(StandardSocketOptions.IP_MULTICAST_TTL) == 3,
                  "IP_MULTICAST_TTL readback 3");
            dc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1);
            check(dc.getOption(StandardSocketOptions.IP_MULTICAST_TTL) == 1,
                  "IP_MULTICAST_TTL readback 1");
            boolean loop = dc.getOption(StandardSocketOptions.IP_MULTICAST_LOOP);
            dc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, !loop);
            check(dc.getOption(StandardSocketOptions.IP_MULTICAST_LOOP) == !loop,
                  "IP_MULTICAST_LOOP toggles");
            dc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, loop);
            if (nif != null) {
                dc.setOption(StandardSocketOptions.IP_MULTICAST_IF, nif);
                check(dc.getOption(StandardSocketOptions.IP_MULTICAST_IF).getName()
                      .equals(nif.getName()), "IP_MULTICAST_IF round trip");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testMulticastChannel();
        testJoinAndGetters();
        testGroupRoundTrip();
        testJoinNegativesAndClose();
        testMulticastOptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
