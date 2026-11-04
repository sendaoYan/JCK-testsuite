/*
 * @test
 * @summary java.net.DatagramSocket conformance: white-box structure + black-box behavior.
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
