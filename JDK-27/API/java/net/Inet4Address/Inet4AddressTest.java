/*
 * @test
 * @summary java.net.Inet4Address conformance: white-box structure + black-box behavior.
 * @run main Inet4AddressTest
 */

import java.lang.reflect.*;
import java.net.*;

public class Inet4AddressTest {
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
        Class<?> c = Class.forName("java.net.Inet4Address");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(InetAddress.class.isAssignableFrom(c), "extends InetAddress");
        check(c.getMethod("ofLiteral", String.class) != null, "ofLiteral");
        check(c.getMethod("ofPosixLiteral", String.class) != null, "ofPosixLiteral");
    }

    static void testBlackBox() throws Exception {
        Inet4Address loop = Inet4Address.ofLiteral("127.0.0.1");
        check(loop.getHostAddress().equals("127.0.0.1"), "literal");
        check(loop.isLoopbackAddress(), "loopback");
        check(!loop.isAnyLocalAddress(), "not any-local");
        check(!loop.isMulticastAddress(), "not multicast");
        check(loop.getAddress().length == 4, "address length 4");

        Inet4Address any = Inet4Address.ofLiteral("0.0.0.0");
        check(any.isAnyLocalAddress(), "0.0.0.0 any-local");

        Inet4Address mc = Inet4Address.ofLiteral("224.0.0.1");
        check(mc.isMulticastAddress(), "multicast");
        check(mc.isMCLinkLocal(), "link-local multicast");
        check(!mc.isMCGlobal(), "not global multicast");

        Inet4Address siteMc = Inet4Address.ofLiteral("239.255.255.250");
        check(siteMc.isMCSiteLocal(), "site-local multicast");
        check(!siteMc.isMCGlobal(), "not global multicast");

        Inet4Address priv = Inet4Address.ofLiteral("10.0.0.1");
        check(priv.isSiteLocalAddress(), "10/8 site-local");
        check(!priv.isLinkLocalAddress(), "not link-local");

        Inet4Address link = Inet4Address.ofLiteral("169.254.1.1");
        check(link.isLinkLocalAddress(), "169.254 link-local");
        check(!link.isSiteLocalAddress(), "not site-local");

        Inet4Address posix = Inet4Address.ofPosixLiteral("127.0.0.1");
        check(posix.getHostAddress().equals("127.0.0.1"), "ofPosixLiteral");

        check(loop.equals(Inet4Address.ofLiteral("127.0.0.1")), "equals");
        check(loop.hashCode() == Inet4Address.ofLiteral("127.0.0.1").hashCode(), "hashCode");
        check(!loop.equals(any), "not equals different address");

        fails(NullPointerException.class, "null literal", () -> Inet4Address.ofLiteral(null));
        fails(IllegalArgumentException.class, "bad literal", () -> Inet4Address.ofLiteral("999.1.1.1"));
    }

    // ------------------------------------------------------------------
    // Deepening: probed contract matrix (identical behaviour on JDK-8 via
    // getByName/getByAddress; strict literals only on JDK-27)
    // ------------------------------------------------------------------

    static void testStrictLiterals() throws Exception {
        // ofLiteral accepts legacy partial forms but rejects hostnames (probed)
        check("1.2.0.3".equals(Inet4Address.ofLiteral("1.2.3").getHostAddress()), "ofLiteral 1.2.3 -> 1.2.0.3");
        check("1.2.3.4".equals(Inet4Address.ofLiteral("01.2.3.4").getHostAddress()), "ofLiteral leading zero");
        fails(IllegalArgumentException.class, "five parts", () -> Inet4Address.ofLiteral("1.2.3.4.5"));
        fails(IllegalArgumentException.class, "bracketed v4", () -> Inet4Address.ofLiteral("[1.2.3.4]"));
        fails(IllegalArgumentException.class, "hostname", () -> Inet4Address.ofLiteral("localhost"));
        // ofPosixLiteral accepts the historic inet_aton forms (probed)
        check("127.0.0.1".equals(Inet4Address.ofPosixLiteral("127.1").getHostAddress()), "posix 127.1 -> 127.0.0.1");
        check("1.2.0.3".equals(Inet4Address.ofPosixLiteral("1.2.3").getHostAddress()), "posix 3-part form");
        check("8.1.1.1".equals(Inet4Address.ofPosixLiteral("010.1.1.1").getHostAddress()), "posix octal 010 -> 8");
        check("255.255.255.255".equals(Inet4Address.ofPosixLiteral("255.255.255.255").getHostAddress()), "posix broadcast");
        check("0.0.0.0".equals(Inet4Address.ofPosixLiteral("0.0.0.0").getHostAddress()), "posix any-local");
        fails(IllegalArgumentException.class, "posix rejects v6", () -> Inet4Address.ofPosixLiteral("::1"));
        fails(IllegalArgumentException.class, "posix rejects scoped v6", () -> Inet4Address.ofPosixLiteral("fe80::1%lo"));
        fails(NullPointerException.class, "posix null", () -> Inet4Address.ofPosixLiteral(null));
        // exact IAE message (probed)
        try {
            Inet4Address.ofLiteral("999.1.1.1");
            check(false, "must throw");
        } catch (IllegalArgumentException e) {
            check("Invalid IP address literal: 999.1.1.1".equals(e.getMessage()), "IAE message exact");
        }
    }

    static void testLiteralTyping() throws Exception {
        String[] literals = {"0.0.0.0", "1.2.3.4", "10.0.0.1", "127.0.0.1", "169.254.1.1",
                             "172.16.0.1", "192.168.1.1", "224.0.0.1", "239.255.255.255",
                             "255.255.255.255"};
        for (String s : literals) {
            java.net.InetAddress x = InetAddress.getByName(s);
            check(x instanceof Inet4Address, "literal " + s + " is Inet4Address");
            check(x.getClass() == Inet4Address.class, "exact class for " + s);
            check(s.equals(x.getHostAddress()), "host address round trip " + s);
        }
        check(((Inet4Address) InetAddress.getByAddress(new byte[]{1, 2, 3, 4})).getClass() == Inet4Address.class,
                "getByAddress 4 bytes exact class");
    }

    static void testBytesRoundTrip() throws Exception {
        int[][] samples = {{1, 2, 3, 4}, {127, 0, 0, 1}, {0, 0, 0, 0}, {255, 255, 255, 255},
                           {10, 255, 0, 1}, {192, 168, 1, 100}};
        for (int[] s : samples) {
            Inet4Address x = (Inet4Address) InetAddress.getByAddress(
                    new byte[]{(byte) s[0], (byte) s[1], (byte) s[2], (byte) s[3]});
            byte[] got = x.getAddress();
            check(got.length == 4, "length 4");
            check((got[0] & 0xff) == s[0] && (got[1] & 0xff) == s[1]
                    && (got[2] & 0xff) == s[2] && (got[3] & 0xff) == s[3],
                    "byte content " + s[0] + "." + s[1] + "." + s[2] + "." + s[3]);
            check(x.equals(InetAddress.getByAddress(new byte[]{(byte) s[0], (byte) s[1], (byte) s[2], (byte) s[3]})),
                    "equals same bytes");
            check(java.util.Arrays.equals(got, InetAddress.getByName(
                    (s[0] & 0xff) + "." + (s[1] & 0xff) + "." + (s[2] & 0xff) + "." + (s[3] & 0xff)).getAddress()),
                    "bytes identical to literal lookup");
        }
        Inet4Address x = Inet4Address.ofLiteral("1.2.3.4");
        byte[] mut = x.getAddress();
        mut[0] = 99;
        check(x.getAddress()[0] == 1, "getAddress defensive copy");
    }

    static void testClassificationDeep() throws Exception {
        check(InetAddress.getByName("127.255.255.254").isLoopbackAddress(), "whole 127/8 loopback");
        check(!InetAddress.getByName("126.1.1.1").isLoopbackAddress(), "126 not loopback");
        check(!InetAddress.getByName("0.0.0.1").isAnyLocalAddress(), "0.0.0.1 not any-local");
        check(InetAddress.getByName("169.254.0.0").isLinkLocalAddress(), "169.254.0.0 link-local");
        check(!InetAddress.getByName("169.255.1.1").isLinkLocalAddress(), "169.255 not link-local");
        check(InetAddress.getByName("172.31.255.1").isSiteLocalAddress(), "172.31 site-local");
        check(!InetAddress.getByName("172.32.1.1").isSiteLocalAddress(), "172.32 not site-local");
        check(!InetAddress.getByName("192.169.1.1").isSiteLocalAddress(), "192.169 not site-local");
    }

    static void testMulticastBoundaries() throws Exception {
        // class D exactly 224.0.0.0 - 239.255.255.255 (probed)
        check(!InetAddress.getByName("223.255.255.255").isMulticastAddress(), "223 not multicast");
        check(InetAddress.getByName("224.0.0.0").isMulticastAddress(), "224.0.0.0 multicast");
        check(InetAddress.getByName("239.255.255.255").isMulticastAddress(), "239.255.255.255 multicast");
        check(!InetAddress.getByName("240.0.0.0").isMulticastAddress(), "240 not multicast (class E)");
        check(!InetAddress.getByName("255.255.255.255").isMulticastAddress(), "broadcast not multicast");
        check(InetAddress.getByName("224.0.0.255").isMCLinkLocal(), "224.0.0.255 link-local MC");
        check(InetAddress.getByName("224.0.1.2").isMCGlobal(), "224.0.1.2 global MC");
        check(!InetAddress.getByName("239.0.0.1").isMCGlobal(), "239.0.0.1 not global");
        check(!InetAddress.getByName("239.0.0.1").isMCLinkLocal(), "239.0.0.1 not link");
        check(!InetAddress.getByName("239.0.0.1").isMCSiteLocal(), "239.0.0.1 not site");
        check(!InetAddress.getByName("239.0.0.1").isMCOrgLocal(), "239.0.0.1 not org");
        check(InetAddress.getByName("239.192.0.1").isMCOrgLocal(), "239.192.0.1 org MC");
        check(InetAddress.getByName("239.255.255.254").isMCSiteLocal(), "239.255.255.254 site MC");
        check(InetAddress.getByName("239.255.255.255").isMCSiteLocal(), "239.255.255.255 site MC");
        InetAddress u = InetAddress.getByName("8.8.8.8");
        check(!u.isMCGlobal() && !u.isMCNodeLocal() && !u.isMCLinkLocal()
                        && !u.isMCSiteLocal() && !u.isMCOrgLocal(), "unicast no MC scope");
    }

    static void testEqualsHashCodeDeep() throws Exception {
        Inet4Address a = (Inet4Address) InetAddress.getByAddress(new byte[]{1, 2, 3, 4});
        check(a.equals(InetAddress.getByName("1.2.3.4")), "bytes equals literal");
        check(a.hashCode() == 16909060, "hashCode is 0x01020304");
        check(InetAddress.getByName("127.0.0.1").hashCode() == 2130706433, "hashCode 127.0.0.1");
        check(InetAddress.getByName("0.0.0.0").hashCode() == 0, "hashCode 0.0.0.0");
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "null false");
        check(!a.equals(InetAddress.getByName("::1")), "v4 not equal v6");
        InetAddress named = InetAddress.getByAddress("x", new byte[]{1, 2, 3, 4});
        check(a.equals(named), "equals ignores hostname");
        check(a.hashCode() == named.hashCode(), "hashCode ignores hostname");
        // ofLiteral and getByName construct equal objects
        check(Inet4Address.ofLiteral("1.2.3.4").equals(InetAddress.getByName("1.2.3.4")),
                "ofLiteral equals getByName");
    }

    static void testToStringAndSerialization() throws Exception {
        Inet4Address a = (Inet4Address) InetAddress.getByAddress(new byte[]{9, 8, 7, 6});
        check("/9.8.7.6".equals(a.toString()), "toString /dotted-quad without hostname");
        InetAddress named = InetAddress.getByAddress("hh", new byte[]{9, 8, 7, 6});
        check("hh/9.8.7.6".equals(named.toString()), "toString hostname/dotted-quad");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(a);
        oos.close();
        Inet4Address back = (Inet4Address) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.getClass() == Inet4Address.class, "serialization preserves Inet4Address");
        check("9.8.7.6".equals(back.getHostAddress()), "serialized host address");
        check(back.equals(a), "serialized equals");
        check(!back.isSiteLocalAddress(), "serialized classification preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStrictLiterals();
        testLiteralTyping();
        testBytesRoundTrip();
        testClassificationDeep();
        testMulticastBoundaries();
        testEqualsHashCodeDeep();
        testToStringAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
