/*
 * @test
 * @summary java.net.InetAddress conformance: white-box structure + full contract
 *          coverage. getByAddress (byte[] and host+byte[]) with exact
 *          address-length validation, dotted-quad formatting, defensive
 *          getAddress() copy, equals/hashCode on address bytes only, address
 *          classification (any-local/loopback/link-local/site-local/multicast
 *          for IPv4 and IPv6 literals), JDK 18+ ofLiteral(), localhost/loopback,
 *          and serialization round-trip. No reverse DNS is triggered.
 * @run main InetAddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static byte[] ipv4(int a, int b, int c, int d) {
        return new byte[]{(byte) a, (byte) b, (byte) c, (byte) d};
    }

    static byte[] ipv6(int... bytes) {
        byte[] out = new byte[16];
        for (int i = 0; i < bytes.length; i++) out[i] = (byte) bytes[i];
        return out;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.InetAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getMethod("getByAddress", byte[].class) != null, "getByAddress(byte[])");
        check(c.getMethod("getByName", String.class) != null, "getByName");
        check(c.getMethod("getLoopbackAddress") != null, "getLoopbackAddress");
        check(c.getMethod("ofLiteral", String.class) != null, "ofLiteral");
    }

    static void testGetByAddress() throws Exception {
        InetAddress a = InetAddress.getByAddress(ipv4(1, 2, 3, 4));
        check("1.2.3.4".equals(a.getHostAddress()), "dotted-quad formatting");
        check(a.getAddress().length == 4, "ipv4 length");
        check(a.getAddress()[0] == 1 && a.getAddress()[3] == 4, "ipv4 bytes");
        check(a instanceof Inet4Address, "ipv4 literal is Inet4Address");

        InetAddress named = InetAddress.getByAddress("myhost", ipv4(10, 0, 0, 1));
        check("myhost".equals(named.getHostName()), "getHostName returns supplied name");
        check("10.0.0.1".equals(named.getHostAddress()), "named host address");

        checkThrows(UnknownHostException.class, () -> InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5}), "illegal address length");
        checkThrows(UnknownHostException.class, () -> InetAddress.getByAddress(new byte[0]), "zero-length address");
    }

    static void testOfLiteral() throws Exception {
        InetAddress v4 = InetAddress.ofLiteral("127.0.0.1");
        check(v4 instanceof Inet4Address, "ofLiteral IPv4 -> Inet4Address");
        check("127.0.0.1".equals(v4.getHostAddress()), "ofLiteral IPv4 host address");
        check(v4.isLoopbackAddress(), "ofLiteral 127.0.0.1 loopback");

        InetAddress v6 = InetAddress.ofLiteral("::1");
        check(v6 instanceof Inet6Address, "ofLiteral IPv6 -> Inet6Address");
        check(v6.isLoopbackAddress(), "ofLiteral ::1 loopback");

        checkThrows(IllegalArgumentException.class, () -> InetAddress.ofLiteral("not-an-ip"), "ofLiteral invalid literal");
        checkThrows(NullPointerException.class, () -> InetAddress.ofLiteral(null), "ofLiteral null");
    }

    static void testDefensiveCopy() throws Exception {
        InetAddress a = InetAddress.getByAddress(ipv4(1, 2, 3, 4));
        byte[] got = a.getAddress();
        got[0] = 99;
        check(a.getAddress()[0] == 1, "getAddress returns defensive copy");
    }

    static void testEqualsHashCode() throws Exception {
        InetAddress a = InetAddress.getByAddress("a", ipv4(127, 0, 0, 1));
        InetAddress b = InetAddress.getByAddress("b", ipv4(127, 0, 0, 1));
        check(a.equals(b), "equals ignores hostname");
        check(a.hashCode() == b.hashCode(), "hashCode ignores hostname");
        check(!a.equals(null), "equals null false");
        check(!a.equals("127.0.0.1"), "equals non-InetAddress false");
        check(!a.equals(InetAddress.getByAddress(ipv4(127, 0, 0, 2))), "equals different bytes false");
        check(a.equals(InetAddress.getByAddress(ipv4(127, 0, 0, 1))), "equals byte[] form");
    }

    static void testClassification() throws Exception {
        check(InetAddress.getByAddress(ipv4(0, 0, 0, 0)).isAnyLocalAddress(), "0.0.0.0 any-local");
        check(!InetAddress.getByAddress(ipv4(1, 1, 1, 1)).isAnyLocalAddress(), "1.1.1.1 not any-local");
        check(InetAddress.getByAddress(ipv4(127, 1, 2, 3)).isLoopbackAddress(), "127.1.2.3 loopback");
        check(!InetAddress.getByAddress(ipv4(128, 0, 0, 1)).isLoopbackAddress(), "128.0.0.1 not loopback");
        check(InetAddress.getByAddress(ipv4(169, 254, 1, 1)).isLinkLocalAddress(), "169.254 link-local");
        check(!InetAddress.getByAddress(ipv4(169, 253, 1, 1)).isLinkLocalAddress(), "169.253 not link-local");
        check(InetAddress.getByAddress(ipv4(10, 0, 0, 1)).isSiteLocalAddress(), "10/8 site-local");
        check(InetAddress.getByAddress(ipv4(172, 16, 0, 1)).isSiteLocalAddress(), "172.16 site-local");
        check(InetAddress.getByAddress(ipv4(192, 168, 1, 1)).isSiteLocalAddress(), "192.168 site-local");
        check(!InetAddress.getByAddress(ipv4(172, 32, 0, 1)).isSiteLocalAddress(), "172.32 not site-local");
        check(InetAddress.getByAddress(ipv4(224, 0, 0, 1)).isMulticastAddress(), "224.0.0.1 multicast");
        check(InetAddress.getByAddress(ipv4(239, 255, 255, 255)).isMulticastAddress(), "239.255.255.255 multicast");
        check(!InetAddress.getByAddress(ipv4(8, 8, 8, 8)).isMulticastAddress(), "8.8.8.8 not multicast");

        check(InetAddress.getByAddress(ipv6()).isAnyLocalAddress(), ":: any-local");
        check(InetAddress.getByAddress(ipv6(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isLoopbackAddress(), "::1 loopback");
        check(InetAddress.getByAddress(ipv6(0xfe, 0x80)).isLinkLocalAddress(), "fe80:: link-local");
        check(InetAddress.getByAddress(ipv6(0xff, 0x00)).isMulticastAddress(), "ff00:: multicast");
    }

    static void testLocalhostAndLoopback() throws Exception {
        InetAddress local = InetAddress.getByName("localhost");
        check(local != null, "getByName localhost non-null");
        check(local.isLoopbackAddress(), "localhost is loopback");
        check(local.getAddress().length == 4 || local.getAddress().length == 16, "localhost address length");

        InetAddress loop = InetAddress.getLoopbackAddress();
        check(loop.isLoopbackAddress(), "getLoopbackAddress is loopback");
        check(loop.getHostAddress() != null, "getLoopbackAddress hostAddress non-null");
        check(loop.getAddress().length == 4 || loop.getAddress().length == 16, "loopback address length");
        check(loop.equals(InetAddress.getLoopbackAddress()), "getLoopbackAddress stable");
    }

    static void testSerialization() throws Exception {
        InetAddress a = InetAddress.getByAddress("h", ipv4(1, 2, 3, 4));
        InetAddress b = roundTrip(a);
        check(a.equals(b), "serialization equals");
        check("1.2.3.4".equals(b.getHostAddress()), "serialization host address");
        check("h".equals(b.getHostName()), "serialization host name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testGetByAddress();
        testOfLiteral();
        testDefensiveCopy();
        testEqualsHashCode();
        testClassification();
        testLocalhostAndLoopback();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
