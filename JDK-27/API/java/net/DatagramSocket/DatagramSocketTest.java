/*
 * @test
 * @summary java.net.DatagramSocket conformance: white-box structure + black-box
 *          behavior. Deepened to full contract coverage: member table incl.
 *          JDK-27 setOption/getOption/supportedOptions/joinGroup(SA,NI)
 *          (synchronized modifiers dropped vs JDK-8), unbound/auto-bind state
 *          machine, bind matrix, port validation with exact "port out of
 *          range:" messages, connect matrix (loopback filtering, no-IO remote
 *          connect, port 0 -> UncheckedIOException on JDK-27, unresolved
 *          address exact message), send/receive semantics (addressless packet
 *          IAE, "Port out of range" on bad packet port, null packet NPE,
 *          receive timeout exact "Receive timed out"), socket options
 *          round-trips incl. SocketOption-based API, close state machine, and
 *          the one-shot DatagramSocketImplFactory ("factory already defined").
 *          All sockets loopback / wildcard-bound ephemeral; every receive
 *          bounded by SoTimeout.
 * @run main DatagramSocketTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.DatagramSocket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(int.class, InetAddress.class) != null, "ctor(int,InetAddress)");
        check(c.getConstructor(SocketAddress.class) != null, "ctor(SocketAddress)");
        check(c.getMethod("send", DatagramPacket.class) != null, "send");
        check(c.getMethod("receive", DatagramPacket.class) != null, "receive");
        check(c.getMethod("connect", InetAddress.class, int.class) != null, "connect(InetAddress,int)");
        check(c.getMethod("connect", SocketAddress.class) != null, "connect(SocketAddress)");
        check(c.getMethod("disconnect") != null, "disconnect");
        check(c.getMethod("setOption", SocketOption.class, Object.class) != null, "setOption");
        check(c.getMethod("getOption", SocketOption.class) != null, "getOption");
        check(c.getMethod("supportedOptions") != null, "supportedOptions");
    }

    static void testBlackBox() throws Exception {
        // ephemeral bound sockets
        DatagramSocket s1 = new DatagramSocket(0);
        DatagramSocket s2 = new DatagramSocket(0);
        try {
            check(s1.isBound(), "s1 bound");
            check(!s1.isClosed(), "s1 not closed");
            check(!s1.isConnected(), "s1 not connected");
            check(s1.getLocalPort() > 0, "s1 localPort > 0");
            check(s1.getLocalAddress() != null, "s1 localAddress");
            check(s1.getLocalSocketAddress() != null, "s1 localSocketAddress");

            // options
            s1.setSoTimeout(1000);
            check(s1.getSoTimeout() == 1000, "soTimeout");
            s1.setSendBufferSize(8192);
            check(s1.getSendBufferSize() > 0, "sendBufferSize");
            s1.setReceiveBufferSize(8192);
            check(s1.getReceiveBufferSize() > 0, "receiveBufferSize");
            s1.setReuseAddress(true);
            check(s1.getReuseAddress(), "reuseAddress");
            s1.setBroadcast(true);
            check(s1.getBroadcast(), "broadcast");

            // setOption / getOption
            s1.setOption(StandardSocketOptions.SO_BROADCAST, true);
            check(Boolean.TRUE.equals(s1.getOption(StandardSocketOptions.SO_BROADCAST)), "setOption/getOption");

            // send/receive roundtrip on loopback
            int s2Port = s2.getLocalPort();
            byte[] data = "hello".getBytes("UTF-8");
            DatagramPacket pkt = new DatagramPacket(data, data.length, InetAddress.getLoopbackAddress(), s2Port);
            s1.send(pkt);
            byte[] rbuf = new byte[64];
            DatagramPacket rpkt = new DatagramPacket(rbuf, rbuf.length);
            s2.receive(rpkt);
            check(new String(rpkt.getData(), rpkt.getOffset(), rpkt.getLength(), "UTF-8").equals("hello"), "payload roundtrip");
            check(rpkt.getAddress().isLoopbackAddress(), "source loopback");
            check(rpkt.getPort() == s1.getLocalPort(), "source port");

            // connect + remote endpoint + disconnect
            DatagramSocket c1 = new DatagramSocket(0);
            try {
                c1.connect(InetAddress.getLoopbackAddress(), s2Port);
                check(c1.isConnected(), "connected");
                check(c1.getInetAddress().isLoopbackAddress(), "getInetAddress");
                check(c1.getPort() == s2Port, "getPort");
                check(c1.getRemoteSocketAddress().equals(new InetSocketAddress(InetAddress.getLoopbackAddress(), s2Port)), "remote address");
                c1.disconnect();
                check(!c1.isConnected(), "disconnected");
                check(c1.getInetAddress() == null, "inetAddress null after disconnect");
            } finally {
                c1.close();
            }

            // connect(SocketAddress)
            DatagramSocket c2 = new DatagramSocket(0);
            try {
                c2.connect(new InetSocketAddress(InetAddress.getLoopbackAddress(), s2Port));
                check(c2.isConnected(), "connected via SocketAddress");
            } finally {
                c2.close();
            }
        } finally {
            s1.close();
            s2.close();
        }
        check(s1.isClosed(), "s1 closed");

        // receive timeout on a fresh socket (nothing pending)
        DatagramSocket t = new DatagramSocket(0);
        t.setSoTimeout(200);
        byte[] b = new byte[1];
        DatagramPacket p = new DatagramPacket(b, b.length);
        fails(SocketTimeoutException.class, "receive timeout", () -> t.receive(p));
        t.close();

        // bind on loopback
        DatagramSocket loop = new DatagramSocket(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
        check(loop.isBound(), "bound to loopback");
        check(loop.getLocalAddress().isLoopbackAddress(), "localAddress loopback");
        loop.close();
    }

    static void failsMsg(Class<? extends Throwable> ex, String msg, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && msg.equals(t.getMessage())) { passed++; return; }
            throw new AssertionError(m + " expected " + ex.getSimpleName() + " ["
                    + msg + "] but got " + t.getClass().getName() + " [" + t.getMessage() + "]");
        }
        throw new AssertionError(m + " no exception");
    }

    // JDK-27 structure: the option accessors are no longer synchronized
    // (JDK-8 had synchronized bind/receive/setters), and the SocketOption-based
    // API family plus joinGroup/leaveGroup(SocketAddress,NetworkInterface) exist.
    static void testWhiteBox27() throws Exception {
        Class<?> c = DatagramSocket.class;
        check(c.getConstructors().length == 4, "four public ctors");
        for (java.lang.reflect.Method mm : c.getMethods()) {
            if (mm.getDeclaringClass() == DatagramSocket.class
                    && !java.lang.reflect.Modifier.isStatic(mm.getModifiers())) {
                check(!java.lang.reflect.Modifier.isSynchronized(mm.getModifiers()),
                        "JDK-27 instance method not synchronized: " + mm.getName());
            }
        }
        // the static factory setter is still synchronized on both JDKs
        check(java.lang.reflect.Modifier.isSynchronized(
                c.getMethod("setDatagramSocketImplFactory", java.net.DatagramSocketImplFactory.class).getModifiers()),
                "static factory setter synchronized");
        check(c.getMethod("setOption", java.net.SocketOption.class, Object.class) != null, "setOption(SocketOption,T)");
        check(c.getMethod("getOption", java.net.SocketOption.class) != null, "getOption(SocketOption)");
        check(c.getMethod("supportedOptions") != null, "supportedOptions");
        check(c.getMethod("joinGroup", SocketAddress.class, NetworkInterface.class) != null, "joinGroup(SA,NI)");
        check(c.getMethod("leaveGroup", SocketAddress.class, NetworkInterface.class) != null, "leaveGroup(SA,NI)");
        java.util.Set<String> sigs = new java.util.HashSet<String>();
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> t : k.getParameterTypes()) sb.append(t.getSimpleName()).append(",");
            sigs.add(sb.toString());
        }
        check(sigs.contains("") && sigs.contains("int,") && sigs.contains("int,InetAddress,")
                && sigs.contains("SocketAddress,"), "ctor signatures");
    }

    // SocketOption-based API (JDK-27 only): supported set + round-trips.
    static void testSocketOptionApi() throws Exception {
        DatagramSocket s = new DatagramSocket(0);
        try {
            java.util.Set<java.net.SocketOption<?>> opts = s.supportedOptions();
            check(opts != null && !opts.isEmpty(), "supportedOptions non-empty");
            check(opts.contains(StandardSocketOptions.SO_BROADCAST), "SO_BROADCAST supported");
            check(opts.contains(StandardSocketOptions.SO_RCVBUF), "SO_RCVBUF supported");
            check(opts.contains(StandardSocketOptions.SO_SNDBUF), "SO_SNDBUF supported");
            check(opts.contains(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR supported");
            check(opts.contains(StandardSocketOptions.IP_TOS), "IP_TOS supported");
            s.setOption(StandardSocketOptions.SO_BROADCAST, Boolean.FALSE);
            check(Boolean.FALSE.equals(s.getOption(StandardSocketOptions.SO_BROADCAST)), "setOption/getOption SO_BROADCAST");
            s.setOption(StandardSocketOptions.SO_BROADCAST, Boolean.TRUE);
            check(Boolean.TRUE.equals(s.getOption(StandardSocketOptions.SO_BROADCAST)), "SO_BROADCAST back on");
            Object snd = s.getOption(StandardSocketOptions.SO_SNDBUF);
            check(snd instanceof Integer && ((Integer) snd).intValue() > 0, "getOption SO_SNDBUF positive int");
            s.setOption(StandardSocketOptions.SO_SNDBUF, Integer.valueOf(64 * 1024));
            check(((Integer) s.getOption(StandardSocketOptions.SO_SNDBUF)).intValue() == 65536,
                    "setOption SO_SNDBUF 64k honoured");
        } finally { s.close(); }
        // getOption on a closed socket -> ClosedChannelException on JDK-27
        // (NIO-backed implementation; documented divergence from the classic
        // SocketException family)
        try {
            s.getOption(StandardSocketOptions.SO_BROADCAST);
            throw new AssertionError("getOption on closed accepted");
        } catch (java.nio.channels.ClosedChannelException expected) {
            passed++;
        }
    }

    // Unbound state and implicit binding.
    static void testUnboundAndImplicitBind() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket u = new DatagramSocket(null);
        try {
            check(!u.isBound(), "unbound isBound false");
            check(u.getLocalPort() == 0, "unbound localPort 0");
            check(u.getLocalAddress() != null && u.getLocalAddress().isAnyLocalAddress(), "unbound localAddress wildcard");
            check(u.getLocalSocketAddress() == null, "unbound lsa null");
            check(u.getPort() == -1, "unbound getPort -1");
            check(u.getInetAddress() == null, "unbound inetAddress null");
            check(u.getRemoteSocketAddress() == null, "unbound remote null");
        } finally { u.close(); }
        check(u.isClosed() && !u.isBound(), "close of never-bound");

        DatagramSocket s1 = new DatagramSocket(null);
        try {
            s1.send(new DatagramPacket(new byte[1], 1, LO, 9));
            check(s1.isBound(), "send auto-binds");
            check(s1.getLocalPort() > 0, "send auto-bind ephemeral");
        } finally { s1.close(); }

        DatagramSocket s2 = new DatagramSocket(null);
        try {
            s2.setSoTimeout(200);
            failsMsg(SocketTimeoutException.class, "Receive timed out", "unbound receive times out",
                    () -> s2.receive(new DatagramPacket(new byte[1], 1)));
            check(s2.isBound() && s2.getLocalPort() > 0, "receive auto-binds");
        } finally { s2.close(); }
    }

    // bind matrix incl. already-bound / occupied / closed.
    static void testBindMatrixDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket s = new DatagramSocket(null);
        try {
            s.bind(null);
            check(s.isBound() && s.getLocalPort() > 0, "bind(null)");
            check(s.getLocalAddress().isAnyLocalAddress(), "bind(null) wildcard");
            SocketAddress lsa = s.getLocalSocketAddress();
            check(lsa instanceof InetSocketAddress
                    && ((InetSocketAddress) lsa).getPort() == s.getLocalPort(), "lsa port");
            fails(SocketException.class, "already bound", () -> s.bind(null));
        } finally { s.close(); }

        DatagramSocket holder = new DatagramSocket(new InetSocketAddress(LO, 0));
        try {
            final int port = holder.getLocalPort();
            fails(BindException.class, "occupied port ctor", () -> new DatagramSocket(port));
            fails(BindException.class, "occupied port bind", () -> new DatagramSocket(new InetSocketAddress(LO, port)));
        } finally { holder.close(); }

        DatagramSocket closed = new DatagramSocket(null);
        closed.close();
        fails(SocketException.class, "bind on closed", () -> closed.bind(null));
    }

    // ctor port validation: exact "port out of range:<v>" (JDK-27 wording).
    static void testPortValidationDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        for (int bad : new int[]{-1, -2, 65536, 65537, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            final int p = bad;
            failsMsg(IllegalArgumentException.class, "port out of range:" + bad,
                    "ctor(int) " + bad, () -> new DatagramSocket(p));
            failsMsg(IllegalArgumentException.class, "port out of range:" + bad,
                    "ctor(int,IA) " + bad, () -> new DatagramSocket(p, LO));
        }
        DatagramSocket hi = new DatagramSocket(65535, LO);
        try { check(hi.getLocalPort() == 65535, "port 65535 binds"); }
        finally { hi.close(); }
    }

    // connect matrix with JDK-27 specifics.
    static void testConnectMatrixDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket peer = new DatagramSocket(0);
        try {
            final int peerPort = peer.getLocalPort();
            DatagramSocket c = new DatagramSocket(0);
            try {
                c.connect(LO, peerPort);
                check(c.isConnected() && c.getPort() == peerPort, "connect state");
                check(c.getRemoteSocketAddress().equals(new InetSocketAddress(LO, peerPort)), "remote SA");
                failsMsg(IllegalArgumentException.class, "port out of range:65536",
                        "connect port 65536", () -> c.connect(LO, 65536));
                failsMsg(IllegalArgumentException.class, "port out of range:-1",
                        "connect port -1", () -> c.connect(LO, -1));
                fails(IllegalArgumentException.class, "connect null address", () -> c.connect((InetAddress) null, 5));
                // JDK-27: connecting to port 0 attempts a real connect and fails
                try {
                    c.connect(LO, 0);
                    throw new AssertionError("connect(lo,0) accepted on JDK-27");
                } catch (java.io.UncheckedIOException e) {
                    passed++;
                }
            } finally { c.close(); }

            // fresh socket: no-IO connect to a remote address records the endpoint
            DatagramSocket r2 = new DatagramSocket(0);
            try {
                r2.connect(InetAddress.getByName("1.2.3.4"), 7777);
                check(r2.isConnected() && r2.getPort() == 7777, "remote connect state");
                check(r2.getRemoteSocketAddress().equals(
                        new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 7777)), "remote endpoint");
                r2.disconnect();
                check(!r2.isConnected() && r2.getInetAddress() == null
                        && r2.getPort() == -1 && r2.getRemoteSocketAddress() == null, "disconnect resets");
                r2.disconnect();
                check(!r2.isConnected(), "disconnect idempotent");
            } finally { r2.close(); }

            DatagramSocket sa = new DatagramSocket(0);
            try {
                sa.connect(new InetSocketAddress(LO, peerPort));
                check(sa.isConnected(), "connect(SA)");
                sa.disconnect();
                failsMsg(SocketException.class, "Unresolved address", "connect unresolved",
                        () -> sa.connect(InetSocketAddress.createUnresolved("no.such.host.invalid", 5)));
                // JDK-27: wrong SocketAddress type -> UnsupportedAddressTypeException
                try {
                    sa.connect(new SocketAddress() { });
                    throw new AssertionError("wrong SA type accepted");
                } catch (java.nio.channels.UnsupportedAddressTypeException e) {
                    passed++;
                }
            } finally { sa.close(); }
        } finally { peer.close(); }
    }

    // connected filtering + addressless send to connect target.
    static void testConnectFilteringDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket a = new DatagramSocket(0);
        DatagramSocket b = new DatagramSocket(0);
        DatagramSocket c = new DatagramSocket(0);
        try {
            b.setSoTimeout(400);
            b.connect(LO, a.getLocalPort());
            a.connect(LO, b.getLocalPort());
            c.send(new DatagramPacket(new byte[]{9}, 1, LO, b.getLocalPort()));
            failsMsg(SocketTimeoutException.class, "Receive timed out", "stranger filtered",
                    () -> b.receive(new DatagramPacket(new byte[4], 4)));
            a.send(new DatagramPacket(new byte[]{5}, 1));
            DatagramPacket in = new DatagramPacket(new byte[4], 4);
            b.receive(in);
            check(in.getLength() == 1 && in.getData()[in.getOffset()] == 5, "addressless send payload");
            check(in.getPort() == a.getLocalPort(), "addressless send source");
        } finally { a.close(); b.close(); c.close(); }
    }

    // send/receive semantics, illegal packets, timeout.
    static void testSendReceiveSemanticsDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket s1 = new DatagramSocket(0);
        DatagramSocket s2 = new DatagramSocket(0);
        try {
            byte[] out = {1, 2, 3, 4, 5, 6, 7, 8};
            s1.send(new DatagramPacket(out, 2, 4, LO, s2.getLocalPort()));
            byte[] rb = new byte[16];
            DatagramPacket in = new DatagramPacket(rb, 3, 10);
            s2.setSoTimeout(500);
            s2.receive(in);
            check(in.getLength() == 4 && in.getOffset() == 3, "window round-trip");
            check(rb[3] == 3 && rb[6] == 6, "payload exact");
            check(in.getPort() == s1.getLocalPort(), "source port");
            failsMsg(IllegalArgumentException.class, "Port out of range:2147483647",
                    "send bad port packet",
                    () -> s1.send(new DatagramPacket(out, 1, LO, Integer.MAX_VALUE)));
            // JDK-27: addressless packet on unconnected socket -> IAE (JDK-8: NPE)
            fails(IllegalArgumentException.class, "send addressless packet unconnected (JDK-27)",
                    () -> s1.send(new DatagramPacket(out, 1)));
            fails(NullPointerException.class, "send(null)", () -> s1.send(null));
            fails(NullPointerException.class, "receive(null)", () -> s1.receive(null));
            // self-datagram and ordering
            DatagramSocket self = new DatagramSocket(0);
            try {
                self.setSoTimeout(500);
                for (int i = 1; i <= 3; i++) {
                    byte[] bb = {(byte) i};
                    self.send(new DatagramPacket(bb, 1, LO, self.getLocalPort()));
                }
                for (int i = 1; i <= 3; i++) {
                    DatagramPacket p = new DatagramPacket(new byte[4], 4);
                    self.receive(p);
                    check(p.getLength() == 1 && p.getData()[p.getOffset()] == (byte) i, "ordered " + i);
                }
            } finally { self.close(); }
        } finally { s1.close(); s2.close(); }

        DatagramSocket idle = new DatagramSocket(0);
        try {
            idle.setSoTimeout(150);
            DatagramPacket p = new DatagramPacket(new byte[8], 8);
            failsMsg(SocketTimeoutException.class, "Receive timed out", "idle receive", () -> idle.receive(p));
            check(p.getLength() == 8 && p.getAddress() == null, "timeout leaves packet untouched");
        } finally { idle.close(); }
    }

    // options incl. JDK-27 messages, defaults and boundaries.
    static void testOptionsMatrixDeep() throws Exception {
        DatagramSocket s = new DatagramSocket(0);
        try {
            check(s.getSoTimeout() == 0, "default timeout 0");
            s.setSoTimeout(123);
            check(s.getSoTimeout() == 123, "timeout round-trip");
            s.setSoTimeout(0);
            check(s.getSoTimeout() == 0, "timeout 0");
            failsMsg(IllegalArgumentException.class, "timeout < 0", "negative timeout", () -> s.setSoTimeout(-1));
            check(s.getSendBufferSize() > 0 && s.getReceiveBufferSize() > 0, "buffers positive");
            s.setSendBufferSize(64 * 1024);
            check(s.getSendBufferSize() == 65536, "send buffer 64k");
            s.setReceiveBufferSize(64 * 1024);
            check(s.getReceiveBufferSize() == 65536, "receive buffer 64k");
            fails(IllegalArgumentException.class, "setSendBufferSize(0)", () -> s.setSendBufferSize(0));
            fails(IllegalArgumentException.class, "setSendBufferSize(-1)", () -> s.setSendBufferSize(-1));
            fails(IllegalArgumentException.class, "setReceiveBufferSize(0)", () -> s.setReceiveBufferSize(0));
            check(s.getTrafficClass() == 0, "default tc 0");
            s.setTrafficClass(0x28);
            check(s.getTrafficClass() == 0x28, "tc round-trip");
            fails(IllegalArgumentException.class, "setTrafficClass(-1)", () -> s.setTrafficClass(-1));
            check(s.getBroadcast(), "default broadcast true");
            s.setBroadcast(false);
            check(!s.getBroadcast(), "broadcast off");
        } finally { s.close(); }

        DatagramSocket u = new DatagramSocket(null);
        try {
            check(!u.getReuseAddress(), "default reuse false");
            u.setReuseAddress(true);
            check(u.getReuseAddress(), "reuse true unbound");
        } finally { u.close(); }
    }

    // close state machine.
    static void testCloseStateMachineDeep() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
        DatagramSocket s = new DatagramSocket(0);
        s.close();
        check(s.isClosed(), "isClosed");
        check(s.isBound(), "isBound preserved after close");
        check(s.getLocalPort() == -1, "closed port -1");
        check(s.getLocalAddress() == null, "closed address null");
        check(s.getLocalSocketAddress() == null, "closed lsa null");
        fails(SocketException.class, "closed send", () -> s.send(new DatagramPacket(new byte[1], 1, LO, 9)));
        fails(SocketException.class, "closed receive", () -> s.receive(new DatagramPacket(new byte[1], 1)));
        fails(SocketException.class, "closed bind", () -> s.bind(null));
        fails(SocketException.class, "closed getSoTimeout", () -> s.getSoTimeout());
        fails(SocketException.class, "closed setSoTimeout", () -> s.setSoTimeout(100));
        s.connect(LO, 1234);
        check(!s.isConnected(), "connect on closed is a no-op");
        s.disconnect();
        s.close(); s.close();
        check(s.isClosed(), "close idempotent");
        DatagramSocket s2 = new DatagramSocket(0);
        s2.close();
        fails(SocketException.class, "closed getSendBufferSize", () -> s2.getSendBufferSize());
    }

    // Misc extras: zero-length datagram, wildcard local endpoint form,
    // toString stability, SO_REUSEADDR via the SocketOption API.
    static void testMiscExtras27() throws Exception {
        InetAddress LO = InetAddress.getLoopbackAddress();
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
            check(w.toString() != null, "toString non-null while open");
            check(w.toString().contains("DatagramSocket"), "toString mentions class");
        } finally { w.close(); }
        check(w.toString() != null, "toString non-null after close");
        check(w.isClosed(), "closed flag set");
        check(w.getChannel() == null, "channel null for plain socket");

        DatagramSocket u = new DatagramSocket(null);
        try {
            u.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
            check(Boolean.TRUE.equals(u.getOption(StandardSocketOptions.SO_REUSEADDR)),
                    "SO_REUSEADDR round-trip via SocketOption API");
            u.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.FALSE);
            check(Boolean.FALSE.equals(u.getOption(StandardSocketOptions.SO_REUSEADDR)),
                    "SO_REUSEADDR cleared via SocketOption API");
            check(!u.isBound() && u.getLocalPort() == 0, "options on unbound socket keep it unbound");
        } finally { u.close(); }
    }

    // One-shot impl factory (JVM global) - runs LAST in this file.
    static void testImplFactoryOneShot27() throws Exception {
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
        fails(NullPointerException.class, "null impl breaks socket creation", () -> new DatagramSocket(0));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBox27();
        testSocketOptionApi();
        testUnboundAndImplicitBind();
        testBindMatrixDeep();
        testPortValidationDeep();
        testConnectMatrixDeep();
        testConnectFilteringDeep();
        testSendReceiveSemanticsDeep();
        testOptionsMatrixDeep();
        testCloseStateMachineDeep();
        testMiscExtras27();
        testImplFactoryOneShot27();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
