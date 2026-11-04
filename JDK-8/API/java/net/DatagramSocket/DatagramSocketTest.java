/*
 * @test
 * @summary java.net.DatagramSocket conformance: bind/close on ephemeral port.
 *          Deepened to full contract coverage: white-box member table (JDK-8:
 *          synchronized bind/receive/setters, no setOption/getOption family),
 *          unbound/auto-bind state machine, bind matrix (null/wildcard/
 *          loopback/occupied/already-bound/closed), port validation with exact
 *          "port out of range:" messages, connect matrix (loopback filtering,
 *          connect-to-remote no-IO, exact port messages, unresolved address),
 *          send/receive semantics (addressless packet NPE, "Port out of range"
 *          on bad packet port, null packet NPE, receive timeout with exact
 *          "Receive timed out" message), socket options round-trips with IAE
 *          boundaries, close state machine (SocketException on closed
 *          send/receive/bind/timeout, getters -1/null, idempotent close), and
 *          the one-shot DatagramSocketImplFactory (second install ->
 *          SocketException "factory already defined"). All sockets loopback /
 *          wildcard-bound ephemeral; every receive bounded by SoTimeout.
 * @run main DatagramSocketTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class DatagramSocketTest {
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
    // like fails() but additionally asserts the exact exception message
    static void failsMsg(Class<? extends Throwable> ex, String msg, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && msg.equals(t.getMessage())) { passed++; return; }
            throw new AssertionError(m + " expected " + ex.getSimpleName() + " ["
                    + msg + "] but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(m + " no exception");
    }

    static final InetAddress LO = loopback();
    static InetAddress loopback() {
        try { return InetAddress.getLoopbackAddress(); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    // ---- preserved original smoke assertions, kept verbatim ----
    static void testLegacySmoke() throws Exception {
        java.net.DatagramSocket ds = new java.net.DatagramSocket();
        check(ds.isBound(), "isBound");
        check(ds.getLocalPort() >= 0, "getLocalPort");
        check(!ds.isClosed(), "not closed");
        ds.close();
        check(ds.isClosed(), "isClosed");
    }

    // White-box: exact public member table of JDK-8 DatagramSocket. bind and
    // receive and every option setter/getter are synchronized (dropped in
    // JDK-27); the setOption/getOption/supportedOptions family is absent.
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements Closeable");
        check(c.getConstructors().length == 4, "four public ctors, got " + c.getConstructors().length);
        String[] syncNames = {"bind", "receive", "setSoTimeout", "getSoTimeout",
                "setSendBufferSize", "getSendBufferSize", "setReceiveBufferSize", "getReceiveBufferSize",
                "setReuseAddress", "getReuseAddress", "setBroadcast", "getBroadcast",
                "setTrafficClass", "getTrafficClass"};
        for (String n : syncNames) {
            Method found = null;
            for (Method mm : c.getMethods()) if (mm.getName().equals(n)) found = mm;
            check(found != null, "method " + n + " present");
            check(Modifier.isSynchronized(found.getModifiers()), n + " synchronized in JDK-8");
        }
        // JDK-8 structure: send/connect/close are NOT synchronized (only
        // bind/receive and the option accessors are)
        check(!Modifier.isSynchronized(c.getMethod("send", DatagramPacket.class).getModifiers()), "send not synchronized");
        check(Modifier.isSynchronized(c.getMethod("setDatagramSocketImplFactory",
                Class.forName("java.net.DatagramSocketImplFactory")).getModifiers()), "factory setter synchronized");
        boolean hasSocketOptionApi = false;
        for (Method mm : c.getMethods()) {
            if (mm.getName().equals("supportedOptions")) hasSocketOptionApi = true;
        }
        check(!hasSocketOptionApi, "JDK-8 has no supportedOptions()");
        check(c.getMethod("getChannel") != null, "getChannel present");
        check(c.getMethod("close") != null, "close present");
        check(c.getMethod("isClosed") != null, "isClosed present");
        check(c.getMethod("isBound") != null, "isBound present");
        check(c.getMethod("isConnected") != null, "isConnected present");
    }

    // Unbound state: new DatagramSocket(null) defers binding entirely.
    static void testUnboundState() throws Exception {
        DatagramSocket s = new DatagramSocket(null);
        try {
            check(!s.isBound(), "unbound: isBound false");
            check(!s.isClosed(), "unbound: not closed");
            check(s.getLocalPort() == 0, "unbound: localPort 0, got " + s.getLocalPort());
            check(s.getLocalAddress() != null && s.getLocalAddress().isAnyLocalAddress(),
                    "unbound: localAddress wildcard");
            check(s.getLocalSocketAddress() == null, "unbound: localSocketAddress null");
            check(s.getPort() == -1, "unbound: getPort -1");
            check(s.getInetAddress() == null, "unbound: inetAddress null");
            check(s.getRemoteSocketAddress() == null, "unbound: remoteSocketAddress null");
            check(!s.isConnected(), "unbound: not connected");
        } finally {
            s.close();
        }
        check(s.isClosed(), "unbound then close: isClosed");
        check(!s.isBound(), "close of never-bound keeps isBound false");
    }

    // send/receive on an unbound socket implicitly binds it to the wildcard.
    static void testImplicitBind() throws Exception {
        DatagramSocket s1 = new DatagramSocket(null);
        try {
            s1.send(new DatagramPacket(new byte[1], 1, LO, 9));
            check(s1.isBound(), "send auto-binds");
            check(s1.getLocalPort() > 0, "send auto-bind picks ephemeral port");
        } finally { s1.close(); }

        DatagramSocket s2 = new DatagramSocket(null);
        try {
            s2.setSoTimeout(200);
            failsMsg(SocketTimeoutException.class, "Receive timed out", "receive on unbound times out",
                    () -> s2.receive(new DatagramPacket(new byte[1], 1)));
            check(s2.isBound(), "receive auto-binds");
            check(s2.getLocalPort() > 0, "receive auto-bind picks ephemeral port");
        } finally { s2.close(); }
    }

    // bind matrix: null / loopback:0 / already bound / occupied port / closed.
    static void testBindMatrix() throws Exception {
        DatagramSocket s = new DatagramSocket(null);
        try {
            s.bind(null);
            check(s.isBound(), "bind(null) binds");
            check(s.getLocalPort() > 0, "bind(null) ephemeral port");
            check(s.getLocalAddress().isAnyLocalAddress(), "bind(null) wildcard address");
            SocketAddress lsa = s.getLocalSocketAddress();
            check(lsa instanceof InetSocketAddress, "localSocketAddress type");
            check(((InetSocketAddress) lsa).getPort() == s.getLocalPort(), "lsa port matches");
            check(((InetSocketAddress) lsa).getAddress().isAnyLocalAddress(), "lsa wildcard");
            // double bind rejected
            fails(SocketException.class, "bind on already-bound socket", () -> s.bind(null));
        } finally { s.close(); }

        DatagramSocket loop = new DatagramSocket(null);
        try {
            loop.bind(new InetSocketAddress(LO, 0));
            check(loop.getLocalAddress().isLoopbackAddress(), "bind(loopback) localAddress");
            check(loop.getLocalPort() > 0, "bind(loopback) port");
        } finally { loop.close(); }

        // occupied port -> BindException (subclass of SocketException)
        DatagramSocket holder = new DatagramSocket(new InetSocketAddress(LO, 0));
        try {
            final int port = holder.getLocalPort();
            fails(BindException.class, "bind to occupied port", () -> new DatagramSocket(port));
            fails(BindException.class, "bind(SA) to occupied port",
                    () -> new DatagramSocket(new InetSocketAddress(LO, port)));
        } finally { holder.close(); }

        // bind on closed socket
        DatagramSocket closed = new DatagramSocket(null);
        closed.close();
        fails(SocketException.class, "bind on closed socket", () -> closed.bind(null));
    }

    // constructor port validation: exact "port out of range:<v>" both JDKs.
    static void testPortValidation() throws Exception {
        for (int bad : new int[]{-1, -2, 65536, 65537, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            final int p = bad;
            failsMsg(IllegalArgumentException.class, "port out of range:" + bad,
                    "ctor(int) port " + bad, () -> new DatagramSocket(p));
            failsMsg(IllegalArgumentException.class, "port out of range:" + bad,
                    "ctor(int,InetAddress) port " + bad, () -> new DatagramSocket(p, LO));
        }
        DatagramSocket hi = new DatagramSocket(65535, LO);
        try {
            check(hi.getLocalPort() == 65535, "port 65535 binds");
            check(hi.getLocalAddress().isLoopbackAddress(), "bound to loopback addr");
        } finally { hi.close(); }
        DatagramSocket any = new DatagramSocket(0, null);
        try {
            check(any.isBound() && any.getLocalPort() > 0, "ctor(0,null) binds wildcard ephemeral");
        } finally { any.close(); }
    }

    // connect matrix: state, remote endpoint, filtering, disconnect, no-IO connect.
    static void testConnectMatrix() throws Exception {
        DatagramSocket peer = new DatagramSocket(0);
        try {
            final int peerPort = peer.getLocalPort();
            DatagramSocket c = new DatagramSocket(0);
            try {
                check(!c.isConnected(), "initially not connected");
                c.connect(LO, peerPort);
                check(c.isConnected(), "connect(InetAddress,int) sets state");
                check(c.getInetAddress().isLoopbackAddress(), "getInetAddress loopback");
                check(c.getPort() == peerPort, "getPort");
                check(c.getRemoteSocketAddress().equals(new InetSocketAddress(LO, peerPort)),
                        "getRemoteSocketAddress round-trip");
                // reconnect to a different endpoint is allowed
                c.connect(LO, 65535);
                check(c.getPort() == 65535, "reconnect updates port");
                c.connect(LO, peerPort);
                check(c.getPort() == peerPort, "reconnect back");
                // disconnect resets everything
                c.disconnect();
                check(!c.isConnected(), "disconnect clears state");
                check(c.getInetAddress() == null, "disconnect inetAddress null");
                check(c.getPort() == -1, "disconnect port -1");
                check(c.getRemoteSocketAddress() == null, "disconnect remote null");
                c.disconnect();
                check(!c.isConnected(), "disconnect idempotent");
            } finally { c.close(); }

            // exact port validation on connect (JDK-8 message: "connect: <v>";
            // JDK-27 uses "port out of range:<v>" - documented divergence)
            DatagramSocket v = new DatagramSocket(0);
            try {
                failsMsg(IllegalArgumentException.class, "connect: 65536",
                        "connect port 65536", () -> v.connect(LO, 65536));
                failsMsg(IllegalArgumentException.class, "connect: -1",
                        "connect port -1", () -> v.connect(LO, -1));
                fails(IllegalArgumentException.class, "connect null address", () -> v.connect((InetAddress) null, 5));
                check(!v.isConnected(), "failed connect leaves state clear");
                // JDK-8: connect to port 0 succeeds and only records state (no IO)
                v.connect(LO, 0);
                check(v.isConnected(), "JDK-8 connect(lo,0) records state");
                v.disconnect();
                // connect to a non-loopback address must not send any packet
                v.connect(InetAddress.getByName("1.2.3.4"), 7777);
                check(v.isConnected(), "connect to remote records state without IO");
                check(v.getPort() == 7777, "remote port recorded");
                check(v.getRemoteSocketAddress() != null
                        && ((InetSocketAddress) v.getRemoteSocketAddress()).getAddress()
                                .equals(InetAddress.getByName("1.2.3.4")), "remote address recorded");
            } finally { v.close(); }

            // connect(SocketAddress) variants
            DatagramSocket sa = new DatagramSocket(0);
            try {
                sa.connect(new InetSocketAddress(LO, peerPort));
                check(sa.isConnected(), "connect(SocketAddress)");
                check(sa.getPort() == peerPort, "connect(SocketAddress) port");
                sa.disconnect();
                failsMsg(SocketException.class, "Unresolved address", "connect unresolved",
                        () -> sa.connect(InetSocketAddress.createUnresolved("no.such.host.invalid", 5)));
                fails(IllegalArgumentException.class, "connect wrong SocketAddress type",
                        () -> sa.connect(new SocketAddress() { }));
            } finally { sa.close(); }
        } finally { peer.close(); }
    }

    // connected socket: receive filters non-peer packets; send without address
    // goes to the connect target. Verified over a live loopback trio.
    static void testConnectFilteringAndDefaultTarget() throws Exception {
        DatagramSocket a = new DatagramSocket(0);
        DatagramSocket b = new DatagramSocket(0);
        DatagramSocket c = new DatagramSocket(0);
        try {
            b.setSoTimeout(400);
            b.connect(LO, a.getLocalPort());
            a.connect(LO, b.getLocalPort());
            check(a.isConnected() && b.isConnected(), "both connected");

            // c is not b's peer: b must not receive c's datagram
            c.send(new DatagramPacket(new byte[]{9}, 1, LO, b.getLocalPort()));
            failsMsg(SocketTimeoutException.class, "Receive timed out", "connected receive filters stranger",
                    () -> b.receive(new DatagramPacket(new byte[4], 4)));

            // a is b's peer and is connected to b: an addressless send is
            // delivered to the connect target
            a.send(new DatagramPacket(new byte[]{5}, 1));
            DatagramPacket in = new DatagramPacket(new byte[4], 4);
            b.receive(in);
            check(in.getLength() == 1, "addressless send length 1");
            check(in.getData()[in.getOffset()] == 5, "addressless send payload");
            check(in.getPort() == a.getLocalPort(), "addressless send source port");
            check(in.getAddress().isLoopbackAddress(), "addressless send source address");

            // plain unconnected receive still accepts anyone
            DatagramSocket plain = new DatagramSocket(0);
            try {
                plain.setSoTimeout(400);
                c.send(new DatagramPacket(new byte[]{3}, 1, LO, plain.getLocalPort()));
                DatagramPacket in2 = new DatagramPacket(new byte[4], 4);
                plain.receive(in2);
                check(in2.getLength() == 1 && in2.getData()[0] == 3, "unconnected receive accepts anyone");
            } finally { plain.close(); }
        } finally {
            a.close(); b.close(); c.close();
        }
    }

    // send/receive semantics and illegal packet validation.
    static void testSendReceiveSemantics() throws Exception {
        DatagramSocket s1 = new DatagramSocket(0);
        DatagramSocket s2 = new DatagramSocket(0);
        try {
            // exact byte round-trip with offset windows in both directions
            byte[] out = {1, 2, 3, 4, 5, 6, 7, 8};
            s1.send(new DatagramPacket(out, 2, 4, LO, s2.getLocalPort()));
            byte[] rb = new byte[16];
            DatagramPacket in = new DatagramPacket(rb, 3, 10);
            s2.setSoTimeout(500);
            s2.receive(in);
            check(in.getLength() == 4, "received exact length");
            check(in.getOffset() == 3, "receive keeps caller offset");
            check(rb[3] == 3 && rb[4] == 4 && rb[5] == 5 && rb[6] == 6, "payload exact");
            check(in.getPort() == s1.getLocalPort(), "source port exact");
            check(in.getAddress().isLoopbackAddress(), "source address loopback");

            // send a packet whose port is invalid: exact shared message
            failsMsg(IllegalArgumentException.class, "Port out of range:2147483647",
                    "send to Integer.MAX_VALUE port",
                    () -> s1.send(new DatagramPacket(out, 1, LO, Integer.MAX_VALUE)));
            // JDK-8: addressless packet on an unconnected socket -> NPE
            fails(NullPointerException.class, "send addressless packet unconnected (JDK-8)",
                    () -> s1.send(new DatagramPacket(out, 1)));
            fails(NullPointerException.class, "send(null)", () -> s1.send(null));
            fails(NullPointerException.class, "receive(null)", () -> s1.receive(null));
        } finally {
            s1.close(); s2.close();
        }

        // receive timeout: exact message, packet length untouched by timeout
        DatagramSocket idle = new DatagramSocket(0);
        try {
            idle.setSoTimeout(150);
            DatagramPacket p = new DatagramPacket(new byte[8], 8);
            failsMsg(SocketTimeoutException.class, "Receive timed out", "idle receive times out", () -> idle.receive(p));
            check(p.getLength() == 8, "timeout leaves packet length");
            check(p.getAddress() == null, "timeout leaves packet address null");
        } finally { idle.close(); }
    }

    // socket option contracts: defaults, round-trips, illegal values.
    static void testOptionsMatrix() throws Exception {
        DatagramSocket s = new DatagramSocket(0);
        try {
            check(s.getSoTimeout() == 0, "default SoTimeout 0");
            s.setSoTimeout(123);
            check(s.getSoTimeout() == 123, "SoTimeout round-trip");
            s.setSoTimeout(0);
            check(s.getSoTimeout() == 0, "SoTimeout 0 legal");
            failsMsg(IllegalArgumentException.class, "timeout < 0", "negative SoTimeout",
                    () -> s.setSoTimeout(-1));

            check(s.getSendBufferSize() > 0, "send buffer positive");
            check(s.getReceiveBufferSize() > 0, "receive buffer positive");
            s.setSendBufferSize(64 * 1024);
            check(s.getSendBufferSize() == 65536, "send buffer honours 64k request");
            s.setReceiveBufferSize(64 * 1024);
            check(s.getReceiveBufferSize() == 65536, "receive buffer honours 64k request");
            fails(IllegalArgumentException.class, "setSendBufferSize(0)", () -> s.setSendBufferSize(0));
            fails(IllegalArgumentException.class, "setSendBufferSize(-1)", () -> s.setSendBufferSize(-1));
            fails(IllegalArgumentException.class, "setReceiveBufferSize(0)", () -> s.setReceiveBufferSize(0));

            check(s.getTrafficClass() == 0, "default traffic class 0");
            s.setTrafficClass(0x28);
            check(s.getTrafficClass() == 0x28, "traffic class round-trip");
            fails(IllegalArgumentException.class, "setTrafficClass(-1)", () -> s.setTrafficClass(-1));
            s.setTrafficClass(0);
            check(s.getTrafficClass() == 0, "traffic class reset");

            check(s.getBroadcast(), "default broadcast true");
            s.setBroadcast(false);
            check(!s.getBroadcast(), "broadcast off round-trip");
            s.setBroadcast(true);
            check(s.getBroadcast(), "broadcast on round-trip");
        } finally { s.close(); }

        // SO_REUSEADDR: only settable before binding, readable after
        DatagramSocket u = new DatagramSocket(null);
        try {
            check(!u.getReuseAddress(), "default reuseAddress false (unbound)");
            u.setReuseAddress(true);
            check(u.getReuseAddress(), "reuseAddress true (unbound)");
            u.setReuseAddress(false);
            check(!u.getReuseAddress(), "reuseAddress false again");
        } finally { u.close(); }

        // getChannel is null unless the socket was created from a channel
        DatagramSocket ch = new DatagramSocket(0);
        try {
            check(ch.getChannel() == null, "plain socket has null channel");
        } finally { ch.close(); }
    }

    // close state machine: operations after close, getters, idempotence.
    static void testCloseStateMachine() throws Exception {
        DatagramSocket s = new DatagramSocket(0);
        final int wasPort = s.getLocalPort();
        s.close();
        check(s.isClosed(), "isClosed");
        check(s.isBound(), "was bound: isBound stays true after close");
        check(s.getLocalPort() == -1, "closed getLocalPort -1");
        check(s.getLocalAddress() == null, "closed getLocalAddress null");
        check(s.getLocalSocketAddress() == null, "closed localSocketAddress null");
        fails(SocketException.class, "closed send",
                () -> s.send(new DatagramPacket(new byte[1], 1, LO, 9)));
        fails(SocketException.class, "closed receive",
                () -> s.receive(new DatagramPacket(new byte[1], 1)));
        fails(SocketException.class, "closed bind", () -> s.bind(null));
        fails(SocketException.class, "closed getSoTimeout", () -> s.getSoTimeout());
        fails(SocketException.class, "closed setSoTimeout", () -> s.setSoTimeout(100));
        // connect on a closed socket is a no-op that must not resurrect state
        s.connect(LO, 1234);
        check(!s.isConnected(), "connect on closed socket does not connect");
        check(s.getLocalPort() == -1, "port stays -1");
        s.disconnect();
        check(!s.isConnected(), "disconnect on closed socket tolerated");
        s.close();
        s.close();
        check(s.isClosed(), "close idempotent");
        check(wasPort > 0, "port was positive before close");

        DatagramSocket s2 = new DatagramSocket(0);
        s2.close();
        fails(SocketException.class, "closed getSendBufferSize", () -> s2.getSendBufferSize());
    }

    // One-shot impl factory (JVM global): installed once, second install
    // rejected with an exact shared message, and a null-returning factory makes
    // subsequent socket creation fail with NPE. MUST run last in the file.
    static void testImplFactoryOneShot() throws Exception {
        java.net.DatagramSocketImplFactory f = new java.net.DatagramSocketImplFactory() {
            public java.net.DatagramSocketImpl createDatagramSocketImpl() { return null; }
        };
        DatagramSocket.setDatagramSocketImplFactory(f);
        try {
            DatagramSocket.setDatagramSocketImplFactory(f);
            throw new AssertionError("second factory install accepted");
        } catch (SocketException e) {
            check("factory already defined".equals(e.getMessage()),
                    "second install message: " + e.getMessage());
        }
        fails(NullPointerException.class, "null impl from factory breaks new DatagramSocket",
                () -> new DatagramSocket(0));
    }

    // White-box extras: exact constructor parameter types and class shape.
    static void testWhiteBoxConstructors() throws Exception {
        Class<?> c = DatagramSocket.class;
        java.util.Set<String> sigs = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getSimpleName()).append(",");
            sigs.add(sb.toString());
        }
        check(sigs.contains(""), "no-arg ctor signature");
        check(sigs.contains("int,"), "ctor(int) signature");
        check(sigs.contains("int,InetAddress,"), "ctor(int,InetAddress) signature");
        check(sigs.contains("SocketAddress,"), "ctor(SocketAddress) signature");
        check(sigs.size() == 4, "exactly four public ctors");
        java.util.Set<String> ifaceNames = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaceNames.add(i.getName());
        check(ifaceNames.contains("java.io.Closeable"), "direct interface Closeable");
        check(ifaceNames.size() == 1, "only direct interface is Closeable");
    }

    // A socket can send to itself; multiple datagrams arrive in send order
    // over loopback.
    static void testSelfDatagramAndOrdering() throws Exception {
        DatagramSocket s = new DatagramSocket(0);
        try {
            s.setSoTimeout(500);
            byte[] payload = "self".getBytes("UTF-8");
            s.send(new DatagramPacket(payload, payload.length, LO, s.getLocalPort()));
            DatagramPacket in = new DatagramPacket(new byte[16], 16);
            s.receive(in);
            check(new String(in.getData(), in.getOffset(), in.getLength(), "UTF-8").equals("self"),
                    "self datagram round-trip");
            check(in.getPort() == s.getLocalPort(), "self datagram source port is own port");

            for (int i = 1; i <= 3; i++) {
                byte[] b = {(byte) i};
                s.send(new DatagramPacket(b, 1, LO, s.getLocalPort()));
            }
            for (int i = 1; i <= 3; i++) {
                DatagramPacket p = new DatagramPacket(new byte[4], 4);
                s.receive(p);
                check(p.getLength() == 1 && p.getData()[p.getOffset()] == (byte) i,
                        "datagram " + i + " received in order");
            }
        } finally { s.close(); }
    }

    // Distinct sockets on ephemeral ports never collide; wildcard ctor binds.
    static void testEphemeralIndependence() throws Exception {
        DatagramSocket a = new DatagramSocket();
        DatagramSocket b = new DatagramSocket(0);
        DatagramSocket cc = new DatagramSocket(new InetSocketAddress(LO, 0));
        try {
            check(a.isBound() && b.isBound() && cc.isBound(), "all three bound");
            check(a.getLocalPort() > 0 && b.getLocalPort() > 0 && cc.getLocalPort() > 0, "all ports positive");
            check(a.getLocalPort() != b.getLocalPort(), "distinct ephemeral ports a/b");
            check(b.getLocalPort() != cc.getLocalPort(), "distinct ephemeral ports b/c");
            check(a.getLocalAddress() != null && b.getLocalAddress() != null, "local addresses non-null");
            check(cc.getLocalAddress().isLoopbackAddress(), "loopback-bound local address");
            check(a.toString() != null && a.toString().contains("DatagramSocket"), "toString sane");
        } finally {
            a.close(); b.close(); cc.close();
        }
    }

    // Misc extras: zero-length datagram at socket level, wildcard local
    // endpoint form, remote endpoint equality after a no-IO connect.
    static void testMiscExtras() throws Exception {
        DatagramSocket s1 = new DatagramSocket(0);
        DatagramSocket s2 = new DatagramSocket(0);
        try {
            s2.setSoTimeout(500);
            s1.send(new DatagramPacket(new byte[1], 0, LO, s2.getLocalPort()));
            DatagramPacket in = new DatagramPacket(new byte[8], 8);
            s2.receive(in);
            check(in.getLength() == 0, "zero-length datagram delivered");
            check(in.getPort() == s1.getLocalPort(), "zero-length source port");
            check(in.getAddress() != null && in.getAddress().isLoopbackAddress(), "zero-length source address");
            check(in.getSocketAddress() instanceof InetSocketAddress, "zero-length SA type");
        } finally { s1.close(); s2.close(); }

        DatagramSocket w = new DatagramSocket(0);
        try {
            SocketAddress lsa = w.getLocalSocketAddress();
            check(lsa instanceof InetSocketAddress, "wildcard lsa type");
            check(((InetSocketAddress) lsa).getPort() == w.getLocalPort(), "wildcard lsa port");
            check(((InetSocketAddress) lsa).getAddress().isAnyLocalAddress(), "wildcard lsa address");
            check(w.toString() != null, "toString non-null while open");
            w.close();
            check(w.toString() != null, "toString non-null after close");
        } finally { if (!w.isClosed()) w.close(); }

        DatagramSocket r = new DatagramSocket(0);
        try {
            InetAddress remote = InetAddress.getByName("1.2.3.4");
            r.connect(remote, 7777);
            check(r.getRemoteSocketAddress().equals(new InetSocketAddress(remote, 7777)),
                    "remote endpoint equals InetSocketAddress");
            check(r.getInetAddress().equals(remote), "getInetAddress equals literal");
        } finally { r.close(); }
    }

    public static void main(String[] args) throws Exception {
        testLegacySmoke();
        testWhiteBox();
        testWhiteBoxConstructors();
        testSelfDatagramAndOrdering();
        testEphemeralIndependence();
        testUnboundState();
        testImplicitBind();
        testBindMatrix();
        testPortValidation();
        testConnectMatrix();
        testConnectFilteringAndDefaultTarget();
        testSendReceiveSemantics();
        testOptionsMatrix();
        testCloseStateMachine();
        testMiscExtras();
        testImplFactoryOneShot();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
