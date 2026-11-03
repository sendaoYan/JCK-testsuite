/*
 * @test
 * @summary java.net.DatagramPacket conformance: white-box structure + black-box behavior.
 * @run main DatagramPacketTest
 */

import java.lang.reflect.*;
import java.net.*;

public class DatagramPacketTest {
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
        Class<?> c = Class.forName("java.net.DatagramPacket");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructor(byte[].class, int.class) != null, "ctor(byte[],int)");
        check(c.getConstructor(byte[].class, int.class, InetAddress.class, int.class) != null, "ctor(byte[],int,InetAddress,int)");
        check(c.getConstructor(byte[].class, int.class, SocketAddress.class) != null, "ctor(byte[],int,SocketAddress)");
        check(c.getMethod("getData") != null, "getData");
        check(c.getMethod("setData", byte[].class, int.class, int.class) != null, "setData");
    }

    static void testBlackBox() throws Exception {
        byte[] buf = new byte[10];
        DatagramPacket p = new DatagramPacket(buf, 10);
        check(p.getData() == buf, "getData returns same buffer");
        check(p.getOffset() == 0, "offset 0");
        check(p.getLength() == 10, "length 10");
        check(p.getAddress() == null, "address null default");
        check(p.getPort() == 0, "port 0 default");
        check(p.getSocketAddress() != null, "socketAddress non-null (wildcard)");

        // set address/port -> socket address roundtrip
        InetAddress lb = InetAddress.getLoopbackAddress();
        p.setAddress(lb);
        p.setPort(9999);
        check(p.getAddress().isLoopbackAddress(), "address set");
        check(p.getPort() == 9999, "port set");
        check(p.getSocketAddress().equals(new InetSocketAddress(lb, 9999)), "socketAddress derived");

        p.setSocketAddress(new InetSocketAddress(lb, 1234));
        check(p.getPort() == 1234 && p.getAddress().equals(lb), "setSocketAddress");

        // offset/length via constructor and setters
        DatagramPacket p2 = new DatagramPacket(buf, 2, 5);
        check(p2.getOffset() == 2 && p2.getLength() == 5, "ctor offset/length");
        p2.setLength(4);
        check(p2.getLength() == 4, "setLength");
        p2.setData(new byte[20]);
        check(p2.getData().length == 20 && p2.getLength() == 20 && p2.getOffset() == 0, "setData resets offset/length");
        p2.setData(buf, 1, 6);
        check(p2.getData() == buf && p2.getOffset() == 1 && p2.getLength() == 6, "setData(buf,off,len)");

        // address-bearing constructors
        DatagramPacket p3 = new DatagramPacket(new byte[8], 8, lb, 777);
        check(p3.getAddress().equals(lb) && p3.getPort() == 777, "ctor(byte[],int,InetAddress,int)");
        DatagramPacket p4 = new DatagramPacket(new byte[8], 8, new InetSocketAddress(lb, 888));
        check(p4.getPort() == 888 && p4.getAddress().equals(lb), "ctor(byte[],int,SocketAddress)");

        fails(IllegalArgumentException.class, "negative length", () -> new DatagramPacket(new byte[8], -1));
        fails(NullPointerException.class, "null buffer", () -> new DatagramPacket(null, 8));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
