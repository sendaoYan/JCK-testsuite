/*
 * @test
 * @summary java.nio.channels.MembershipKey conformance: white-box structure + black-box behavior.
 * @run main MembershipKeyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Set;

public class MembershipKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.MembershipKey");
        check(Modifier.isAbstract(c.getModifiers()), "MembershipKey is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"isValid", "drop", "block", "unblock", "channel",
                "group", "networkInterface", "sourceAddress"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress group = InetAddress.getByName("224.0.113.2");

        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));

            // Require a real IPv4 address: an IPv6-only interface (e.g. a
            // docker veth link) satisfies supportsMulticast but join(IPv4
            // group) on it throws "not configured for IPv4". Prefer a real
            // adapter; fall back to loopback when that is all there is.
            NetworkInterface nif = pickMulticastInterface();
            if (nif == null) {
                System.out.println("NOTE: no multicast-capable interface; behavioral join skipped");
            } else {
                MembershipKey key = dc.join(group, nif);
                check(key.isValid(), "valid on join");
                check(key.channel() == dc, "channel()");
                check(key.group().equals(group), "group()");
                check(key.networkInterface().equals(nif), "networkInterface()");
                check(key.sourceAddress() == null, "sourceAddress null for any-source join");

                // drop invalidates
                key.drop();
                check(!key.isValid(), "invalid after drop");
                key.drop(); // second drop is a documented no-op
                check(!key.isValid(), "still invalid after no-op drop");

                // block/unblock round trip on an any-source membership
                MembershipKey any = dc.join(group, nif);
                InetAddress src = InetAddress.getLoopbackAddress();
                MembershipKey blocked = any.block(src);
                check(blocked == any, "block returns this key");
                MembershipKey unblocked = any.unblock(src);
                check(unblocked == any, "unblock returns this key");
                boolean threw = false;
                try { any.unblock(src); }
                catch (IllegalStateException e) { threw = true; }
                check(threw, "unblock non-blocked source -> IllegalStateException");

                // channel close invalidates membership
                dc.close();
                check(!any.isValid(), "invalid after channel close");
                threw = false;
                try { DatagramChannel.open(java.net.StandardProtocolFamily.INET).close();
                      DatagramChannel closed = DatagramChannel.open(java.net.StandardProtocolFamily.INET);
                      closed.close();
                      closed.join(group, nif); }
                catch (Exception e) { threw = true; }
                check(threw, "join on closed channel rejected");
            }
        }
    }

    // ===================== A6 deepening: getters after drop / block contract / source filtering =====================

    /** Throwing action so lambdas may call methods that declare checked IOException. */
    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
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

    static NetworkInterface pickMulticastInterface() throws Exception {
        NetworkInterface fallback = null;
        for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || !ni.supportsMulticast()) continue;
            boolean hasV4 = false;
            for (InetAddress a : java.util.Collections.list(ni.getInetAddresses()))
                if (a instanceof java.net.Inet4Address) hasV4 = true;
            if (!hasV4) continue;
            if (!ni.isLoopback()) return ni;
            if (fallback == null) fallback = ni;
        }
        return fallback;
    }

    static void testLifecycleNegatives() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) {
            System.out.println("NOTE: no multicast-capable interface; extra membership tests skipped");
            return;
        }
        InetAddress group = InetAddress.getByName("239.255.18.1");
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: membership is independent of the local endpoint,
            // and Windows rejects a multicast group address as a local address.
            dc.bind(new InetSocketAddress(0));
            MembershipKey key = dc.join(group, nif);
            check(key.isValid(), "fresh membership valid before drop");
            key.drop();
            // getters stay readable after drop, only validity flips
            check(key.group().equals(group), "group() readable after drop");
            check(key.networkInterface() == nif, "networkInterface() readable after drop");
            check(key.channel() == dc, "channel() readable after drop");
            checkThrows(IllegalStateException.class, () -> key.block(group),
                        "block after drop -> IllegalStateException");
            checkThrows(IllegalStateException.class, () -> key.unblock(group),
                        "unblock after drop -> IllegalStateException");
            MembershipKey rejoined = dc.join(group, nif);
            check(rejoined != key, "re-join yields a new key");
        }
    }

    static void testBlockContract() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.18.2");
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: membership is independent of the local endpoint,
            // and Windows rejects a multicast group address as a local address.
            dc.bind(new InetSocketAddress(0));
            MembershipKey key = dc.join(group, nif);
            InetAddress src = InetAddress.getByName("127.0.0.1");
            check(key.block(src) == key, "block returns this key");
            // implementation is idempotent: blocking the same source again is a no-op
            // (the javadoc mentions IllegalStateException, but the RI just returns)
            check(key.block(src) == key, "re-block of the same source is a no-op in the RI");
            check(key.unblock(src) == key, "unblock returns this key");
            checkThrows(IllegalStateException.class, () -> key.unblock(src),
                        "unblock of a not-blocked source -> IllegalStateException");
            checkThrows(NullPointerException.class, () -> key.block(null),
                        "block(null) -> NullPointerException");
            checkThrows(IllegalArgumentException.class,
                        () -> key.block(InetAddress.getByName("224.0.0.1")),
                        "block(multicast source) -> IllegalArgumentException");
        }
    }

    static void testSourceSpecificMembership() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.18.3");
        InetAddress src = InetAddress.getByName("192.0.2.66");
        try (DatagramChannel dc = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            dc.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: membership is independent of the local endpoint,
            // and Windows rejects a multicast group address as a local address.
            dc.bind(new InetSocketAddress(0));
            MembershipKey key = dc.join(group, nif, src);
            check(key.sourceAddress().equals(src), "sourceAddress() records the filter source");
            check(dc.join(group, nif, src) == key, "re-join with same source is idempotent");
            checkThrows(IllegalStateException.class, () -> key.block(src),
                        "block on source-specific membership -> IllegalStateException");
            checkThrows(IllegalStateException.class, () -> key.unblock(src),
                        "unblock on source-specific membership -> IllegalStateException");
            key.drop();
            MembershipKey any = dc.join(group, nif);
            check(any.isValid(), "any-source join after dropping source-specific");
            checkThrows(IllegalStateException.class, () -> dc.join(group, nif, src),
                        "source-specific join while any-source member -> IllegalStateException");
        }
    }

    static void testSourceFilteringDelivery() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.18.4");
        InetAddress ifaceAddr = null;
        for (InetAddress a : java.util.Collections.list(nif.getInetAddresses()))
            if (a instanceof java.net.Inet4Address) ifaceAddr = a;
        try (DatagramChannel rx = DatagramChannel.open(java.net.StandardProtocolFamily.INET);
             DatagramChannel tx = DatagramChannel.open(java.net.StandardProtocolFamily.INET)) {
            rx.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            // Wildcard bind: Windows rejects a multicast group address as the
            // local endpoint; reception is driven by the membership, not by
            // the bound address.
            rx.bind(new InetSocketAddress(0));
            MembershipKey key = rx.join(group, nif);
            rx.configureBlocking(false);
            // Send from the interface address itself so the packet source is
            // deterministic (and matches the address that is blocked below);
            // a wildcard-bound sender uses an OS-chosen source address.
            tx.bind(new InetSocketAddress(ifaceAddr, 0));
            tx.setOption(java.net.StandardSocketOptions.IP_MULTICAST_IF, nif);
            int port = ((InetSocketAddress) rx.getLocalAddress()).getPort();
            try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
                rx.register(sel, java.nio.channels.SelectionKey.OP_READ);
                tx.send(java.nio.ByteBuffer.wrap(new byte[] {1}),
                        new InetSocketAddress(group, port));
                check(sel.select(5_000) == 1, "unblocked group datagram received");
                java.nio.ByteBuffer drain = java.nio.ByteBuffer.allocate(8);
                check(rx.receive(drain) != null, "drained");
                sel.selectedKeys().clear();
                key.block(ifaceAddr);
                tx.send(java.nio.ByteBuffer.wrap(new byte[] {2}),
                        new InetSocketAddress(group, port));
                check(sel.select(1_000) == 0, "blocked source datagram is filtered out");
                key.unblock(ifaceAddr);
                tx.send(java.nio.ByteBuffer.wrap(new byte[] {3}),
                        new InetSocketAddress(group, port));
                check(sel.select(5_000) == 1, "unblocking restores delivery");
                java.nio.ByteBuffer got = java.nio.ByteBuffer.allocate(8);
                check(rx.receive(got) != null && got.get(0) == 3, "payload after unblock exact");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLifecycleNegatives();
        testBlockContract();
        testSourceSpecificMembership();
        testSourceFilteringDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
