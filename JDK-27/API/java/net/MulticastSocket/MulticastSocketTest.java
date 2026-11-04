/*
 * @test
 * @summary java.net.MulticastSocket conformance: white-box structure + black-box behavior.
 * @run main MulticastSocketTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class MulticastSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
