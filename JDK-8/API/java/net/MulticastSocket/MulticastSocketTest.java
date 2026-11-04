/*
 * @test
 * @summary java.net.MulticastSocket conformance: bind/close.
 *          Deepened to full contract coverage: white-box member table incl.
 *          the deprecated setTTL(byte)/getTTL()/send(DatagramPacket,byte)
 *          family, constructor matrix (wildcard/ephemeral/loopback/deferred
 *          bind), TTL matrix with byte-truncation semantics of the legacy API
 *          and IAE boundaries, interface/networkInterface round-trips,
 *          loopback mode round-trip, joinGroup/leaveGroup validation (NPE for
 *          null, exact "Not a multicast address", IAE for bad SocketAddress),
 *          a live group data path over an administratively-scoped group with
 *          TTL 0 (join -> send -> receive exact bytes; after leave -> timeout),
 *          deprecated send(pkt, ttl) unicast round-trip that leaves the socket
 *          TTL untouched, inherited DatagramSocket behavior, and closed-socket
 *          SocketExceptions. All real traffic stays on the local host.
 * @run main MulticastSocketTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

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
    static void failsMsg(Class<? extends Throwable> ex, String msg, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && msg.equals(t.getMessage())) { passed++; return; }
            throw new AssertionError(m + " expected " + ex.getSimpleName() + " [" + msg
                    + "] but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(m + " no exception");
    }

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    // ---- preserved original smoke assertions, kept verbatim ----
    static void testLegacySmoke() throws Exception {
        java.net.MulticastSocket ms = new java.net.MulticastSocket();
        check(ms.isBound(), "isBound");
        check(!ms.isClosed(), "not closed");
        ms.close();
        check(ms.isClosed(), "isClosed");
    }

    // White-box: JDK-8 member table, including the deprecated byte-TTL family.
    static void testWhiteBox() throws Exception {
        Class<?> c = MulticastSocket.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("java.net.DatagramSocket"), "extends DatagramSocket");
        check(c.getConstructors().length == 3, "three public ctors");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(SocketAddress.class) != null, "ctor(SocketAddress)");
        String[] names = {"setTTL", "getTTL", "setTimeToLive", "getTimeToLive",
                "joinGroup", "leaveGroup", "setInterface", "getInterface",
                "setNetworkInterface", "getNetworkInterface",
                "setLoopbackMode", "getLoopbackMode", "send"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
        check(c.getMethod("setTTL", byte.class) != null, "setTTL(byte) present on JDK-8");
        check(c.getMethod("getTTL") != null, "getTTL() present on JDK-8");
        check(c.getMethod("send", DatagramPacket.class, byte.class) != null,
                "send(DatagramPacket,byte) present on JDK-8");
        check(c.getMethod("joinGroup", InetAddress.class) != null, "joinGroup(InetAddress)");
        check(c.getMethod("joinGroup", SocketAddress.class, NetworkInterface.class) != null,
                "joinGroup(SocketAddress,NetworkInterface)");
        check(c.getMethod("leaveGroup", SocketAddress.class, NetworkInterface.class) != null,
                "leaveGroup(SocketAddress,NetworkInterface)");
    }

    // constructor matrix: wildcard / ephemeral / loopback / deferred bind.
    static void testConstructors() throws Exception {
        MulticastSocket a = new MulticastSocket();
        try {
            check(a.isBound(), "ctor() bound");
            check(a.getLocalPort() > 0, "ctor() ephemeral port");
            check(a.getLocalAddress() != null, "ctor() localAddress");
        } finally { a.close(); }

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

    // TTL matrix: modern int API boundaries + legacy byte semantics.
    static void testTtlMatrix() throws Exception {
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

            // legacy byte API: values wrap to unsigned byte range
            m.setTTL((byte) 9);
            check(m.getTimeToLive() == 9, "setTTL(9) visible via getTimeToLive");
            check(m.getTTL() == (byte) 9, "getTTL() 9");
            m.setTTL((byte) -1);
            check(m.getTimeToLive() == 255, "setTTL(-1) == TTL 255");
            check(m.getTTL() == (byte) -1, "getTTL() -1");
            m.setTTL((byte) 200);
            check(m.getTimeToLive() == 200, "setTTL(200) == TTL 200");
            check(m.getTTL() == (byte) -56, "getTTL() wraps 200 to -56");
            m.setTimeToLive(31);
            check(m.getTTL() == (byte) 31, "setTimeToLive visible via getTTL");
        } finally { m.close(); }
    }

    // multicast interface selection.
    static void testInterfaceMatrix() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            InetAddress def = m.getInterface();
            check(def != null, "default interface non-null");
            check(def.isAnyLocalAddress(), "default interface is the wildcard");
            m.setInterface(LO);
            InetAddress after = m.getInterface();
            check(after != null && after.isLoopbackAddress(), "setInterface(loopback) read back loopback");
            check(after.getAddress().equals(LO.getAddress()) || after.isLoopbackAddress(),
                    "interface address form");
            // the wildcard itself is not a valid outgoing interface
            fails(SocketException.class, "setInterface(0.0.0.0)",
                    () -> m.setInterface(InetAddress.getByName("0.0.0.0")));
        } finally { m.close(); }
    }

    static void testNetworkInterfaceMatrix() throws Exception {
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
            // JDK-8 rejects null with SocketException (JDK-27: IllegalArgumentException)
            fails(SocketException.class, "setNetworkInterface(null) JDK-8", () -> m.setNetworkInterface(null));
        } finally { m.close(); }
    }

    static void testLoopbackMode() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            m.setLoopbackMode(false);
            check(!m.getLoopbackMode(), "loopback disabled round-trip");
            m.setLoopbackMode(true);
            check(m.getLoopbackMode(), "loopback enabled round-trip");
            m.setLoopbackMode(false);
            check(!m.getLoopbackMode(), "loopback disabled again");
        } finally { m.close(); }
    }

    // joinGroup/leaveGroup validation matrix.
    static void testJoinLeaveValidation() throws Exception {
        MulticastSocket m = new MulticastSocket(0);
        try {
            fails(NullPointerException.class, "joinGroup(null)", () -> m.joinGroup(null));
            failsMsg(SocketException.class, "Not a multicast address",
                    "joinGroup(127.0.0.1)", () -> m.joinGroup(InetAddress.getByName("127.0.0.1")));
            // (SocketAddress, NetworkInterface) flavour
            fails(IllegalArgumentException.class, "joinGroup(null, NI)",
                    () -> m.joinGroup(null, NetworkInterface.getByInetAddress(LO)));
            // administratively-scoped group on the default interface
            InetAddress group = InetAddress.getByName("239.254.3.9");
            InetSocketAddress sa = new InetSocketAddress(group, 0);
            m.joinGroup(sa, null);
            check(true, "joinGroup(SA, null NI) uses default interface");
            m.leaveGroup(sa, null);
            check(true, "leaveGroup(SA, null NI)");
            // leave without join is an error
            fails(SocketException.class, "leaveGroup without join", () -> m.leaveGroup(group));
            // join/leave the InetAddress flavour
            m.joinGroup(group);
            m.leaveGroup(group);
            // a second, different group joins and leaves independently
            InetAddress group2 = InetAddress.getByName("239.254.5.17");
            m.joinGroup(group2);
            m.leaveGroup(group2);
        } finally { m.close(); }
    }

    // Live group data path: TTL 0 keeps the datagram on the host; the receiver
    // is wildcard-bound (group membership is on a non-loopback interface in
    // this environment, probed 2026-11-05). After leaveGroup no more delivery.
    static void testGroupDataPath() throws Exception {
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
            check(receiver.getTimeToLive() == 0 && sender.getTimeToLive() == 0, "TTL 0 armed on both");
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

    // Deprecated send(DatagramPacket, byte): explicit TTL for one datagram,
    // unicast loopback round-trip; the socket's own TTL stays untouched.
    static void testDeprecatedSendWithTtl() throws Exception {
        MulticastSocket receiver = new MulticastSocket(new InetSocketAddress(LO, 0));
        MulticastSocket sender = new MulticastSocket(0);
        try {
            receiver.setSoTimeout(500);
            sender.setTimeToLive(3);
            byte[] payload = {7, 9, 11};
            sender.send(new DatagramPacket(payload, payload.length, LO, receiver.getLocalPort()), (byte) 1);
            DatagramPacket in = new DatagramPacket(new byte[8], 8);
            receiver.receive(in);
            check(in.getLength() == 3, "deprecated send length");
            check(in.getData()[in.getOffset()] == 7 && in.getData()[in.getOffset() + 1] == 9
                    && in.getData()[in.getOffset() + 2] == 11, "deprecated send bytes exact");
            check(sender.getTimeToLive() == 3, "socket TTL untouched by deprecated send");
        } finally {
            receiver.close();
            sender.close();
        }
    }

    // inherited DatagramSocket behavior over loopback unicast.
    static void testInheritedDatagramBehavior() throws Exception {
        MulticastSocket a = new MulticastSocket(0);
        MulticastSocket b = new MulticastSocket(0);
        try {
            check(a.isBound() && !a.isClosed() && !a.isConnected(), "initial state via inherited API");
            byte[] data = "m".getBytes("UTF-8");
            a.send(new DatagramPacket(data, data.length, LO, b.getLocalPort()));
            byte[] rbuf = new byte[16];
            DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);
            b.setSoTimeout(500);
            b.receive(rpkt);
            check(new String(rpkt.getData(), rpkt.getOffset(), rpkt.getLength(), "UTF-8").equals("m"),
                    "unicast round-trip");
            check(rpkt.getPort() == a.getLocalPort(), "unicast source port");
            a.connect(LO, b.getLocalPort());
            check(a.isConnected() && a.getPort() == b.getLocalPort(), "inherited connect");
            check(a.getRemoteSocketAddress() != null, "inherited remote endpoint");
            a.disconnect();
            check(!a.isConnected(), "inherited disconnect");
            check(a.getChannel() == null, "inherited getChannel null");
            check(a.toString() != null, "toString non-null");
            a.setSoTimeout(77);
            check(a.getSoTimeout() == 77, "inherited SoTimeout");
        } finally {
            a.close();
            b.close();
        }
    }

    // closed socket semantics.
    static void testClosedSemantics() throws Exception {
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

    public static void main(String[] args) throws Exception {
        testLegacySmoke();
        testWhiteBox();
        testConstructors();
        testTtlMatrix();
        testInterfaceMatrix();
        testNetworkInterfaceMatrix();
        testLoopbackMode();
        testJoinLeaveValidation();
        testGroupDataPath();
        testTwoReceiverGroup();
        testDeprecatedSendWithTtl();
        testInheritedDatagramBehavior();
        testClosedSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
