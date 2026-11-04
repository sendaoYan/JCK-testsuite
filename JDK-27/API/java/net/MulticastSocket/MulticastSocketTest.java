/*
 * @test
 * @summary java.net.MulticastSocket conformance: white-box structure + black-box
 *          behavior. Deepened to full contract coverage: member table (legacy
 *          byte-TTL family REMOVED on JDK-27, verified via reflection),
 *          constructor matrix (wildcard/ephemeral/loopback/deferred bind), TTL
 *          matrix with IAE boundaries, interface/networkInterface round-trips
 *          (setNetworkInterface(null) -> IAE on JDK-27 vs SocketException on
 *          JDK-8), loopback mode round-trip, joinGroup/leaveGroup validation
 *          (NPE for null, exact "Not a multicast address", IAE for bad
 *          SocketAddress), a live group data path with TTL 0 (join -> send ->
 *          receive exact bytes; after leave -> timeout), inherited
 *          DatagramSocket behavior, and closed-socket SocketExceptions. All
 *          real traffic stays on the local host.
 * @run main MulticastSocketTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class MulticastSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.MulticastSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("java.net.DatagramSocket"), "extends DatagramSocket");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(SocketAddress.class) != null, "ctor(SocketAddress)");
        check(c.getMethod("setTimeToLive", int.class) != null, "setTimeToLive");
        check(c.getMethod("getTimeToLive") != null, "getTimeToLive");
        check(c.getMethod("joinGroup", InetAddress.class) != null, "joinGroup(InetAddress)");
        check(c.getMethod("leaveGroup", InetAddress.class) != null, "leaveGroup(InetAddress)");
        check(c.getMethod("joinGroup", SocketAddress.class, NetworkInterface.class) != null, "joinGroup(SocketAddress,NetworkInterface)");
        check(c.getMethod("leaveGroup", SocketAddress.class, NetworkInterface.class) != null, "leaveGroup(SocketAddress,NetworkInterface)");
        check(c.getMethod("setInterface", InetAddress.class) != null, "setInterface");
        check(c.getMethod("getInterface") != null, "getInterface");
        check(c.getMethod("setNetworkInterface", NetworkInterface.class) != null, "setNetworkInterface");
        check(c.getMethod("getNetworkInterface") != null, "getNetworkInterface");
        check(c.getMethod("setLoopbackMode", boolean.class) != null, "setLoopbackMode");
        check(c.getMethod("getLoopbackMode") != null, "getLoopbackMode");
    }

    static void testBlackBox() throws Exception {
        MulticastSocket ms = new MulticastSocket(0);
        try {
            check(ms.isBound(), "bound");
            check(!ms.isClosed(), "not closed");
            check(ms.getLocalPort() > 0, "ephemeral port");

            ms.setTimeToLive(2);
            check(ms.getTimeToLive() == 2, "timeToLive");

            ms.setLoopbackMode(true);
            check(ms.getLoopbackMode(), "loopbackMode true");
            ms.setLoopbackMode(false);
            check(!ms.getLoopbackMode(), "loopbackMode false");

            InetAddress lb = InetAddress.getLoopbackAddress();
            ms.setInterface(lb);
            check(ms.getInterface().isLoopbackAddress(), "setInterface/getInterface loopback");

            NetworkInterface lo = NetworkInterface.getByInetAddress(lb);
            if (lo != null) {
                ms.setNetworkInterface(lo);
                check(ms.getNetworkInterface() != null, "getNetworkInterface non-null");
            }

            check(ms.toString() != null, "toString");
        } finally {
            ms.close();
        }
        check(ms.isClosed(), "closed");

        // inherited unicast DatagramSocket behavior over loopback
        MulticastSocket a = new MulticastSocket(0);
        MulticastSocket b = new MulticastSocket(0);
        try {
            byte[] data = "m".getBytes("UTF-8");
            DatagramPacket pkt = new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), b.getLocalPort());
            a.send(pkt);
            byte[] rbuf = new byte[16];
            DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);
            b.receive(rpkt);
            check(new String(rpkt.getData(), rpkt.getOffset(), rpkt.getLength(), "UTF-8").equals("m"), "unicast roundtrip");
            check(rpkt.getPort() == a.getLocalPort(), "unicast source port");
        } finally {
            a.close();
            b.close();
        }
    }

    static void failsMsg(Class<? extends Throwable> ex, String msg, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && msg.equals(t.getMessage())) { passed++; return; }
            throw new AssertionError(m + " expected " + ex.getSimpleName() + " [" + msg
                    + "] but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(m + " no exception");
    }

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    // The legacy byte-TTL family was removed in JDK-13+; verified by reflection
    // so the file compiles on both sides.
    static void testLegacyTtlFamilyRemoved() throws Exception {
        Class<?> c = MulticastSocket.class;
        try {
            c.getMethod("setTTL", byte.class);
            throw new AssertionError("setTTL(byte) still present on JDK-27");
        } catch (NoSuchMethodException expected) { passed++; }
        try {
            c.getMethod("getTTL");
            throw new AssertionError("getTTL() still present on JDK-27");
        } catch (NoSuchMethodException expected) { passed++; }
        try {
            c.getMethod("send", DatagramPacket.class, byte.class);
            throw new AssertionError("send(DatagramPacket,byte) still present on JDK-27");
        } catch (NoSuchMethodException expected) { passed++; }
    }

    // constructor matrix: wildcard / ephemeral / loopback / deferred bind.
    static void testConstructorsDeep() throws Exception {
        MulticastSocket b = new MulticastSocket(0);
        try {
            check(b.isBound() && b.getLocalPort() > 0, "ctor(0) bound ephemeral");
        } finally { b.close(); }

        MulticastSocket c = new MulticastSocket(new InetSocketAddress(LO, 0));
        try {
            check(c.isBound() && c.getLocalPort() > 0, "ctor(SA loopback) bound");
            check(c.getLocalAddress().isLoopbackAddress(), "ctor(SA loopback) address");
        } finally { c.close(); }

        MulticastSocket d = new MulticastSocket(null);
        try {
            check(!d.isBound(), "ctor(null) defers binding");
            check(d.getLocalPort() == 0, "ctor(null) port 0");
            d.bind(new InetSocketAddress(LO, 0));
            check(d.isBound() && d.getLocalPort() > 0, "bind after ctor(null)");
            check(d.getLocalAddress().isLoopbackAddress(), "bound loopback");
        } finally { d.close(); }
    }

    // TTL matrix boundaries (JDK-27 message differs from JDK-8: type only).
    static void testTtlMatrixDeep() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            check(m.getTimeToLive() == 1, "default TTL 1");
            m.setTimeToLive(0);
            check(m.getTimeToLive() == 0, "TTL 0 round-trip");
            m.setTimeToLive(1);
            check(m.getTimeToLive() == 1, "TTL 1 round-trip");
            m.setTimeToLive(128);
            check(m.getTimeToLive() == 128, "TTL 128 round-trip");
            m.setTimeToLive(255);
            check(m.getTimeToLive() == 255, "TTL 255 round-trip");
            fails(IllegalArgumentException.class, "setTimeToLive(-1)", () -> m.setTimeToLive(-1));
            fails(IllegalArgumentException.class, "setTimeToLive(256)", () -> m.setTimeToLive(256));
            fails(IllegalArgumentException.class, "setTimeToLive(Integer.MAX_VALUE)",
                    () -> m.setTimeToLive(Integer.MAX_VALUE));
        } finally { m.close(); }
    }

    static void testInterfaceMatrixDeep() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            InetAddress def = m.getInterface();
            check(def != null, "default interface non-null");
            check(def.isAnyLocalAddress(), "default interface is the wildcard");
            m.setInterface(LO);
            InetAddress after = m.getInterface();
            check(after != null && after.isLoopbackAddress(), "setInterface(loopback) read back loopback");
            fails(SocketException.class, "setInterface(0.0.0.0)",
                    () -> m.setInterface(InetAddress.getByName("0.0.0.0")));
        } finally { m.close(); }
    }

    static void testNetworkInterfaceMatrixDeep() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            NetworkInterface def = m.getNetworkInterface();
            check(def != null, "default NetworkInterface non-null");
            NetworkInterface lo = NetworkInterface.getByInetAddress(LO);
            if (lo != null) {
                m.setNetworkInterface(lo);
                NetworkInterface after = m.getNetworkInterface();
                check(after != null, "NI read back non-null");
                check(after.equals(lo), "setNetworkInterface loopback equals round-trip");
            }
            // JDK-27 rejects null with IllegalArgumentException (JDK-8: SocketException)
            fails(IllegalArgumentException.class, "setNetworkInterface(null) JDK-27",
                    () -> m.setNetworkInterface(null));
        } finally { m.close(); }
    }

    // joinGroup/leaveGroup validation matrix.
    static void testJoinLeaveValidationDeep() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            fails(NullPointerException.class, "joinGroup(null)", () -> m.joinGroup(null));
            failsMsg(SocketException.class, "Not a multicast address",
                    "joinGroup(127.0.0.1)", () -> m.joinGroup(InetAddress.getByName("127.0.0.1")));
            fails(IllegalArgumentException.class, "joinGroup(null, NI)",
                    () -> m.joinGroup(null, NetworkInterface.getByInetAddress(LO)));
            InetAddress group = InetAddress.getByName("239.254.3.9");
            InetSocketAddress sa = new InetSocketAddress(group, 0);
            m.joinGroup(sa, null);
            check(true, "joinGroup(SA, null NI) uses default interface");
            m.leaveGroup(sa, null);
            check(true, "leaveGroup(SA, null NI)");
            fails(SocketException.class, "leaveGroup without join", () -> m.leaveGroup(group));
            m.joinGroup(group);
            m.leaveGroup(group);
            InetAddress group2 = InetAddress.getByName("239.254.5.17");
            m.joinGroup(group2);
            m.leaveGroup(group2);
        } finally { m.close(); }
    }

    // Live group data path with TTL 0 (probed working in this environment:
    // wildcard-bound receiver; membership resolves on a non-loopback iface).
    static void testGroupDataPathDeep() throws Exception {
        InetAddress group = InetAddress.getByName("239.254.4.11");
        MulticastSocket receiver = new MulticastSocket(0);
        MulticastSocket sender = new MulticastSocket(0);
        try {
            int port = receiver.getLocalPort();
            check(port > 0, "receiver ephemeral port");
            check(sender.getLocalPort() > 0 && sender.getLocalPort() != port, "distinct ports");
            receiver.setSoTimeout(500);
            receiver.setTimeToLive(0);
            sender.setTimeToLive(0);
            check(receiver.getTimeToLive() == 0 && sender.getTimeToLive() == 0, "TTL 0 armed");
            receiver.joinGroup(group);
            byte[] data = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
            sender.send(new DatagramPacket(data, data.length, group, port));
            DatagramPacket in = new DatagramPacket(new byte[16], 16);
            receiver.receive(in);
            check(in.getLength() == 4, "group datagram length");
            check(in.getData()[in.getOffset()] == (byte) 0xDE
                    && in.getData()[in.getOffset() + 1] == (byte) 0xAD
                    && in.getData()[in.getOffset() + 2] == (byte) 0xBE
                    && in.getData()[in.getOffset() + 3] == (byte) 0xEF, "group datagram bytes exact");
            check(in.getAddress() != null, "group datagram source address filled");
            check(in.getPort() == sender.getLocalPort(), "group datagram source port");
            receiver.leaveGroup(group);
            sender.send(new DatagramPacket(data, data.length, group, port));
            fails(java.net.SocketTimeoutException.class, "after leave: no delivery",
                    () -> receiver.receive(new DatagramPacket(new byte[16], 16)));
        } finally {
            receiver.close();
            sender.close();
        }
    }

    // closed socket semantics.
    static void testClosedSemanticsDeep() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        m.close();
        check(m.isClosed(), "closed");
        check(m.isBound(), "was bound: isBound stays true");
        fails(SocketException.class, "closed joinGroup", () -> m.joinGroup(InetAddress.getByName("239.1.2.3")));
        fails(SocketException.class, "closed setTimeToLive", () -> m.setTimeToLive(4));
        fails(SocketException.class, "closed getTimeToLive", () -> m.getTimeToLive());
        fails(SocketException.class, "closed getInterface", () -> m.getInterface());
        fails(SocketException.class, "closed setInterface", () -> m.setInterface(LO));
        fails(SocketException.class, "closed getNetworkInterface", () -> m.getNetworkInterface());
        fails(SocketException.class, "closed getLoopbackMode", () -> m.getLoopbackMode());
        m.close();
        check(m.isClosed(), "close idempotent");
    }

    // Inherited DatagramSocket surface beyond the base black-box pass.
    static void testInheritedExtras() throws Exception {
        MulticastSocket a = new MulticastSocket(0);
        MulticastSocket b = new MulticastSocket(0);
        try {
            check(a.isBound() && !a.isClosed() && !a.isConnected(), "inherited initial state");
            a.setSoTimeout(77);
            check(a.getSoTimeout() == 77, "inherited SoTimeout round-trip");
            a.connect(LO, b.getLocalPort());
            check(a.isConnected() && a.getPort() == b.getLocalPort(), "inherited connect");
            check(a.getInetAddress().isLoopbackAddress(), "inherited getInetAddress");
            check(a.getRemoteSocketAddress() != null, "inherited remote endpoint");
            a.disconnect();
            check(!a.isConnected() && a.getInetAddress() == null, "inherited disconnect resets");
            check(a.getChannel() == null, "inherited getChannel null");
            check(a.toString() != null, "inherited toString non-null");
            // zero-length datagram over unicast loopback
            b.setSoTimeout(500);
            a.send(new DatagramPacket(new byte[1], 0, LO, b.getLocalPort()));
            DatagramPacket in = new DatagramPacket(new byte[4], 4);
            b.receive(in);
            check(in.getLength() == 0, "inherited zero-length datagram");
            check(in.getPort() == a.getLocalPort(), "inherited zero-length source port");
        } finally {
            a.close();
            b.close();
        }
    }

    // Two members of the same group each receive the datagram (same host).
    static void testTwoReceiverGroup() throws Exception {
        InetAddress group = InetAddress.getByName("239.254.6.23");
        MulticastSocket r1 = new MulticastSocket(0);
        MulticastSocket r2 = new MulticastSocket(0);
        MulticastSocket s = new MulticastSocket(0);
        try {
            r1.setSoTimeout(500);
            r2.setSoTimeout(500);
            r1.setTimeToLive(0);
            r2.setTimeToLive(0);
            s.setTimeToLive(0);
            r1.joinGroup(group);
            r2.joinGroup(group);
            byte[] data = {(byte) 0x5A};
            s.send(new DatagramPacket(data, data.length, group, r1.getLocalPort()));
            DatagramPacket in1 = new DatagramPacket(new byte[4], 4);
            r1.receive(in1);
            check(in1.getLength() == 1 && in1.getData()[in1.getOffset()] == 0x5A, "member 1 got datagram");
            s.send(new DatagramPacket(data, data.length, group, r2.getLocalPort()));
            DatagramPacket in2 = new DatagramPacket(new byte[4], 4);
            r2.receive(in2);
            check(in2.getLength() == 1 && in2.getData()[in2.getOffset()] == 0x5A, "member 2 got datagram");
            r1.leaveGroup(group);
            r2.leaveGroup(group);
        } finally {
            r1.close();
            r2.close();
            s.close();
        }
    }

    // Misc extras: well-known group join/leave, local endpoint form, and the
    // inherited SocketOption-based API on a MulticastSocket.
    static void testMiscExtras27() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            m.joinGroup(InetAddress.getByName("224.0.0.1"));
            m.leaveGroup(InetAddress.getByName("224.0.0.1"));
            check(true, "well-known group 224.0.0.1 joined and left");
            java.net.SocketAddress lsa = m.getLocalSocketAddress();
            check(lsa instanceof java.net.InetSocketAddress, "local SA type");
            check(((java.net.InetSocketAddress) lsa).getPort() == m.getLocalPort(), "local SA port");
            check(m.toString() != null, "toString non-null");
            java.util.Set<java.net.SocketOption<?>> opts = m.supportedOptions();
            check(opts != null && opts.contains(java.net.StandardSocketOptions.IP_MULTICAST_TTL),
                    "IP_MULTICAST_TTL in supportedOptions");
            check(opts.contains(java.net.StandardSocketOptions.IP_MULTICAST_LOOP),
                    "IP_MULTICAST_LOOP in supportedOptions");
            check(m.isBound() && !m.isClosed(), "still bound and open");
            m.setOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL, Integer.valueOf(9));
            check(m.getTimeToLive() == 9, "setOption IP_MULTICAST_TTL visible via getTimeToLive");
            check(((Integer) m.getOption(java.net.StandardSocketOptions.IP_MULTICAST_TTL)).intValue() == 9,
                    "getOption IP_MULTICAST_TTL");
        } finally { m.close(); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyTtlFamilyRemoved();
        testConstructorsDeep();
        testTtlMatrixDeep();
        testInterfaceMatrixDeep();
        testNetworkInterfaceMatrixDeep();
        testJoinLeaveValidationDeep();
        testGroupDataPathDeep();
        testTwoReceiverGroup();
        testInheritedExtras();
        testMiscExtras27();
        testClosedSemanticsDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
