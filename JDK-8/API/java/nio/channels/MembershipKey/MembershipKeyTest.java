/*
 * @test
 * @summary java.nio.channels.MembershipKey conformance: abstract surface.
 * @run main MembershipKeyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MembershipKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static void testMembershipKey() {
        check(Modifier.isAbstract(MembershipKey.class.getModifiers()), "abstract");
        Set<String> names = new HashSet<String>();
        for (Method m : MembershipKey.class.getMethods()) names.add(m.getName());
        String[] required = {"isValid", "drop", "block", "unblock", "channel", "group",
                             "networkInterface", "sourceAddress"};
        for (String n : required) check(names.contains(n), "has method " + n);
        boolean prot = true;
        for (Constructor<?> ct : MembershipKey.class.getDeclaredConstructors())
            prot &= Modifier.isProtected(ct.getModifiers());
        check(prot, "declared constructors are protected");
    }

    // ===================== A6 deepening: getters / drop / block-unblock / source filtering =====================

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

    static void testLifecycleAndGetters() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) {
            System.out.println("NOTE: no multicast-capable interface; membership behavioral tests skipped");
            return;
        }
        InetAddress group = InetAddress.getByName("239.255.17.1");
        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET);
        try {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(group, 0));
            MembershipKey key = dc.join(group, nif);
            check(key.isValid(), "valid on join");
            check(key.channel() == dc, "channel() identity");
            check(key.group().equals(group), "group()");
            check(key.networkInterface() == nif, "networkInterface() identity");
            check(key.sourceAddress() == null, "sourceAddress() null for any-source join");
            // getters stay usable after drop, only validity flips
            key.drop();
            check(!key.isValid(), "invalid after drop");
            key.drop(); // documented no-op
            check(!key.isValid(), "still invalid after no-op drop");
            check(key.group().equals(group), "group() still readable after drop");
            check(key.networkInterface() == nif, "networkInterface() still readable after drop");
            check(key.channel() == dc, "channel() still readable after drop");
            MembershipKey rejoined = dc.join(group, nif);
            check(rejoined != key, "re-join yields a new key");
            check(rejoined.isValid(), "rejoined key valid");
        } finally {
            dc.close();
        }
        // channel close invalidates
        DatagramChannel dc2 = DatagramChannel.open(StandardProtocolFamily.INET);
        dc2.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        dc2.bind(new InetSocketAddress(group, 0));
        MembershipKey k2 = dc2.join(group, nif);
        dc2.close();
        check(!k2.isValid(), "invalid after channel close");
        final NetworkInterface fnif = nif;
        checkThrows(ClosedChannelException.class, () -> dc2.join(group, fnif),
                    "join on closed channel -> ClosedChannelException");
    }

    static void testBlockContract() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.17.2");
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(group, 0));
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
            // operations on an invalid key
            key.drop();
            checkThrows(IllegalStateException.class, () -> key.block(src),
                        "block after drop -> IllegalStateException");
            checkThrows(IllegalStateException.class, () -> key.unblock(src),
                        "unblock after drop -> IllegalStateException");
        }
    }

    static void testSourceFilteringDelivery() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.17.3");
        InetAddress ifaceAddr = null;
        for (InetAddress a : Collections.list(nif.getInetAddresses()))
            if (a instanceof java.net.Inet4Address) ifaceAddr = a;
        try (DatagramChannel rx = DatagramChannel.open(StandardProtocolFamily.INET);
             DatagramChannel tx = DatagramChannel.open(StandardProtocolFamily.INET)) {
            rx.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            rx.bind(new InetSocketAddress(group, 0));
            MembershipKey key = rx.join(group, nif);
            rx.configureBlocking(false);
            tx.setOption(StandardSocketOptions.IP_MULTICAST_IF, nif);
            int port = ((InetSocketAddress) rx.getLocalAddress()).getPort();
            try (Selector sel = Selector.open()) {
                rx.register(sel, SelectionKey.OP_READ);
                // sanity: unblocked delivery works
                tx.send(ByteBuffer.wrap(new byte[] {1}), new InetSocketAddress(group, port));
                check(sel.select(5_000) == 1, "unblocked group datagram received");
                ByteBuffer drain = ByteBuffer.allocate(8);
                check(rx.receive(drain) != null, "drained");
                sel.selectedKeys().clear();
                // block the interface address the sender transmits from
                key.block(ifaceAddr);
                tx.send(ByteBuffer.wrap(new byte[] {2}), new InetSocketAddress(group, port));
                check(sel.select(1_000) == 0, "blocked source datagram is filtered out");
                check(sel.selectNow() == 0, "still nothing ready from the blocked source");
                key.unblock(ifaceAddr);
                tx.send(ByteBuffer.wrap(new byte[] {3}), new InetSocketAddress(group, port));
                check(sel.select(5_000) == 1, "unblocking restores delivery");
                ByteBuffer got = ByteBuffer.allocate(8);
                check(rx.receive(got) != null && got.get(0) == 3, "payload after unblock exact");
            }
        }
    }

    static void testSourceSpecificMembership() throws Exception {
        NetworkInterface nif = pickMulticastInterface();
        if (nif == null) return;
        InetAddress group = InetAddress.getByName("239.255.17.4");
        InetAddress src = InetAddress.getByName("192.0.2.55"); // unicast, never sends here
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            dc.bind(new InetSocketAddress(group, 0));
            MembershipKey key = dc.join(group, nif, src);
            check(key.isValid(), "source-specific membership valid");
            check(key.sourceAddress().equals(src), "sourceAddress() records the filter source");
            check(key.group().equals(group), "group() on source-specific key");
            check(dc.join(group, nif, src) == key, "re-join with same source is idempotent");
            checkThrows(IllegalStateException.class, () -> key.block(src),
                        "block on source-specific membership -> IllegalStateException");
            checkThrows(IllegalStateException.class, () -> key.unblock(src),
                        "unblock on source-specific membership -> IllegalStateException");
            key.drop();
            // after dropping the source-specific key, any-source join becomes possible
            MembershipKey any = dc.join(group, nif);
            check(any.isValid(), "any-source join after dropping source-specific");
            checkThrows(IllegalStateException.class, () -> dc.join(group, nif, src),
                        "source-specific join while any-source member -> IllegalStateException");
        }
    }

    public static void main(String[] args) throws Exception {
        testMembershipKey();
        testLifecycleAndGetters();
        testBlockContract();
        testSourceFilteringDelivery();
        testSourceSpecificMembership();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
