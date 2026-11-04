/*
 * @test
 * @summary java.net.Inet4Address conformance: full contract coverage. final class
 *          with no public constructors, IPv4 literal -> Inet4Address typing,
 *          dotted-quad formatting and byte round-trip, D-class multicast
 *          boundaries, MC scope matrix, equals/hashCode on bytes, defensive
 *          getAddress copy, and serialization type preservation. All values
 *          probed identical on JDK-8 and JDK-27.
 * @run main Inet4AddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class Inet4AddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static java.net.Inet4Address v4(String literal) throws Exception {
        return (java.net.Inet4Address) java.net.InetAddress.getByName(literal);
    }

    static java.net.Inet4Address byBytes(int a, int b, int c, int d) throws Exception {
        return (java.net.Inet4Address) java.net.InetAddress.getByAddress(new byte[]{(byte) a, (byte) b, (byte) c, (byte) d});
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Inet4Address");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(java.net.InetAddress.class.isAssignableFrom(c), "extends InetAddress");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable via InetAddress");
        int publicCtors = 0;
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ctor.getModifiers())) publicCtors++;
        }
        check(publicCtors == 0, "no public constructors (factory-only class)");
    }

    static void testOriginal() throws Exception {
        java.net.Inet4Address a = (java.net.Inet4Address) java.net.InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        check(a.isLoopbackAddress(), "isLoopbackAddress");
        check("127.0.0.1".equals(a.getHostAddress()), "getHostAddress");
        check(a.getAddress().length == 4, "4 bytes");
        check(a instanceof java.net.InetAddress, "is InetAddress");
    }

    static void testLiteralTyping() throws Exception {
        String[] literals = {"0.0.0.0", "1.2.3.4", "10.0.0.1", "127.0.0.1", "169.254.1.1",
                             "172.16.0.1", "192.168.1.1", "224.0.0.1", "239.255.255.255",
                             "255.255.255.255"};
        for (String s : literals) {
            java.net.InetAddress x = java.net.InetAddress.getByName(s);
            check(x instanceof java.net.Inet4Address, "literal " + s + " is Inet4Address");
            check(x.getClass() == java.net.Inet4Address.class, "exact class for " + s);
            check(s.equals(x.getHostAddress()), "host address round trip " + s);
        }
        // byte -> type
        check(byBytes(1, 2, 3, 4).getClass() == java.net.Inet4Address.class, "getByAddress 4 bytes exact class");
    }

    static void testBytesRoundTrip() throws Exception {
        int[][] samples = {{1, 2, 3, 4}, {127, 0, 0, 1}, {0, 0, 0, 0}, {255, 255, 255, 255},
                           {10, 255, 0, 1}, {192, 168, 1, 100}};
        for (int[] s : samples) {
            java.net.Inet4Address x = byBytes(s[0], s[1], s[2], s[3]);
            byte[] got = x.getAddress();
            check(got.length == 4, "length 4");
            check((got[0] & 0xff) == s[0] && (got[1] & 0xff) == s[1]
                    && (got[2] & 0xff) == s[2] && (got[3] & 0xff) == s[3],
                    "byte content " + s[0] + "." + s[1] + "." + s[2] + "." + s[3]);
            check(x.equals(byBytes(s[0], s[1], s[2], s[3])), "equals same bytes");
            // high-bit bytes survive as unsigned octets
            check(java.util.Arrays.equals(got, java.net.InetAddress.getByName(
                    (s[0] & 0xff) + "." + (s[1] & 0xff) + "." + (s[2] & 0xff) + "." + (s[3] & 0xff)).getAddress()),
                    "bytes identical to literal lookup");
        }
        // defensive copy
        java.net.Inet4Address x = byBytes(1, 2, 3, 4);
        byte[] mut = x.getAddress();
        mut[0] = 99;
        check(x.getAddress()[0] == 1, "getAddress defensive copy");
    }

    static void testClassification() throws Exception {
        check(v4("127.0.0.1").isLoopbackAddress(), "loopback");
        check(v4("127.255.255.254").isLoopbackAddress(), "whole 127/8 loopback");
        check(!v4("126.1.1.1").isLoopbackAddress(), "126 not loopback");
        check(v4("0.0.0.0").isAnyLocalAddress(), "any-local");
        check(!v4("0.0.0.1").isAnyLocalAddress(), "0.0.0.1 not any-local");
        check(v4("169.254.1.1").isLinkLocalAddress(), "link-local");
        check(!v4("169.253.1.1").isLinkLocalAddress(), "169.253 not link-local");
        check(!v4("169.255.1.1").isLinkLocalAddress(), "169.255 not link-local");
        check(v4("10.1.1.1").isSiteLocalAddress(), "10/8 site-local");
        check(v4("172.16.1.1").isSiteLocalAddress(), "172.16 site-local");
        check(v4("172.31.255.1").isSiteLocalAddress(), "172.31 site-local");
        check(!v4("172.32.1.1").isSiteLocalAddress(), "172.32 not site-local");
        check(v4("192.168.1.1").isSiteLocalAddress(), "192.168 site-local");
        check(!v4("192.169.1.1").isSiteLocalAddress(), "192.169 not site-local");
    }

    static void testMulticastBoundaries() throws Exception {
        // class D exactly 224.0.0.0 - 239.255.255.255 (probed both JDKs)
        check(!v4("223.255.255.255").isMulticastAddress(), "223 not multicast");
        check(v4("224.0.0.0").isMulticastAddress(), "224.0.0.0 multicast");
        check(v4("239.255.255.255").isMulticastAddress(), "239.255.255.255 multicast");
        check(!v4("240.0.0.0").isMulticastAddress(), "240 not multicast (class E)");
        check(!v4("255.255.255.255").isMulticastAddress(), "broadcast not multicast");
        // MC scope matrix (probed identical JDK-8/JDK-27)
        check(v4("224.0.0.1").isMCLinkLocal(), "224.0.0.1 link-local MC");
        check(v4("224.0.0.255").isMCLinkLocal(), "224.0.0.255 link-local MC");
        check(!v4("224.0.1.1").isMCLinkLocal(), "224.0.1.1 not link MC");
        check(v4("224.0.1.1").isMCGlobal(), "224.0.1.1 global MC");
        check(v4("224.0.1.2").isMCGlobal(), "224.0.1.2 global MC");
        check(!v4("239.0.0.1").isMCGlobal(), "239.0.0.1 not global");
        check(!v4("239.0.0.1").isMCLinkLocal(), "239.0.0.1 not link");
        check(!v4("239.0.0.1").isMCSiteLocal(), "239.0.0.1 not site");
        check(!v4("239.0.0.1").isMCOrgLocal(), "239.0.0.1 not org");
        check(v4("239.192.0.1").isMCOrgLocal(), "239.192.0.1 org MC");
        check(v4("239.255.255.254").isMCSiteLocal(), "239.255.255.254 site MC");
        check(v4("239.255.255.255").isMCSiteLocal(), "239.255.255.255 site MC");
        check(!v4("8.8.8.8").isMCGlobal(), "unicast no global MC");
        check(!v4("8.8.8.8").isMCSiteLocal(), "unicast no site MC");
        check(!v4("8.8.8.8").isMCOrgLocal(), "unicast no org MC");
        check(!v4("8.8.8.8").isMCLinkLocal(), "unicast no link MC");
        check(!v4("8.8.8.8").isMCNodeLocal(), "unicast no node MC");
    }

    static void testEqualsHashCode() throws Exception {
        java.net.Inet4Address a = byBytes(1, 2, 3, 4);
        check(a.equals(v4("1.2.3.4")), "bytes equals literal");
        check(a.hashCode() == 16909060, "hashCode is 0x01020304");
        check(v4("127.0.0.1").hashCode() == 2130706433, "hashCode 127.0.0.1");
        check(v4("0.0.0.0").hashCode() == 0, "hashCode 0.0.0.0");
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "null false");
        check(!a.equals("1.2.3.4"), "other type false");
        check(!a.equals(byBytes(1, 2, 3, 5)), "different last byte");
        check(!a.equals(byBytes(2, 2, 3, 4)), "different first byte");
        // IPv6 loopback ::1 is never equal to 127.0.0.1 despite same meaning
        check(!a.equals(java.net.InetAddress.getByName("::1")), "v4 not equal v6");
        // hostname irrelevant
        java.net.InetAddress named = java.net.InetAddress.getByAddress("x", new byte[]{1, 2, 3, 4});
        check(a.equals(named), "equals ignores hostname");
        check(a.hashCode() == named.hashCode(), "hashCode ignores hostname");
    }

    static void testToStringAndSerialization() throws Exception {
        java.net.Inet4Address a = byBytes(9, 8, 7, 6);
        check("/9.8.7.6".equals(a.toString()), "toString /dotted-quad without hostname");
        java.net.InetAddress named = java.net.InetAddress.getByAddress("hh", new byte[]{9, 8, 7, 6});
        check("hh/9.8.7.6".equals(named.toString()), "toString hostname/dotted-quad");
        java.net.Inet4Address back = roundTrip(a);
        check(back.getClass() == java.net.Inet4Address.class, "serialization preserves Inet4Address");
        check("9.8.7.6".equals(back.getHostAddress()), "serialized host address");
        check(back.equals(a), "serialized equals");
        check(back.isSiteLocalAddress() == false, "serialized classification preserved");
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testWhiteBox();
        testLiteralTyping();
        testBytesRoundTrip();
        testClassification();
        testMulticastBoundaries();
        testEqualsHashCode();
        testToStringAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
