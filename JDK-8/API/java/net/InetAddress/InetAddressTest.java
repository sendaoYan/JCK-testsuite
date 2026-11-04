/*
 * @test
 * @summary java.net.InetAddress conformance: full contract coverage. getByAddress
 *          (byte[] and host+byte[]) with exact address-length validation,
 *          dotted-quad formatting, defensive getAddress() copy, equals/hashCode
 *          on address bytes only, address classification (any-local/loopback/
 *          link-local/site-local/multicast for IPv4 and IPv6 literals),
 *          getByName("localhost")/getLoopbackAddress(), and serialization
 *          round-trip. No reverse DNS is triggered (host names are always
 *          supplied explicitly via getByAddress(String, byte[])).
 * @run main InetAddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    static void testGetByAddress() throws Exception {
        InetAddress a = InetAddress.getByAddress(ipv4(1, 2, 3, 4));
        check("1.2.3.4".equals(a.getHostAddress()), "dotted-quad formatting");
        check(a.getAddress().length == 4, "ipv4 length");
        check(a.getAddress()[0] == 1 && a.getAddress()[3] == 4, "ipv4 bytes");

        InetAddress named = InetAddress.getByAddress("myhost", ipv4(10, 0, 0, 1));
        check("myhost".equals(named.getHostName()), "getHostName returns supplied name");
        check("10.0.0.1".equals(named.getHostAddress()), "named host address");

        checkThrows(UnknownHostException.class, () -> InetAddress.getByAddress(new byte[]{1, 2, 3, 4, 5}), "illegal address length");
        checkThrows(UnknownHostException.class, () -> InetAddress.getByAddress(new byte[0]), "zero-length address");
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

    // ------------------------------------------------------------------
    // Deepening: literal resolution matrix (probed identical on JDK-8/JDK-27)
    // ------------------------------------------------------------------

    static void testIPv4LiteralMatrix() throws Exception {
        String[] quad = {"0.0.0.0", "1.2.3.4", "10.0.0.1", "127.0.0.1", "169.254.0.0",
                         "192.168.1.1", "224.0.0.1", "239.255.255.255", "255.255.255.255"};
        for (String s : quad) {
            InetAddress a = InetAddress.getByName(s);
            check(a instanceof java.net.Inet4Address, "getByName(" + s + ") is Inet4Address");
            check(s.equals(a.getHostAddress()), "getHostAddress round-trip " + s);
            check(a.getAddress().length == 4, "4-byte address for " + s);
            check(a == InetAddress.getByName(s) || a.equals(InetAddress.getByName(s)),
                    "stable repeated lookup " + s);
        }
        // byte content of two representative literals
        InetAddress a = InetAddress.getByName("127.0.0.1");
        byte[] b = a.getAddress();
        check(b[0] == 127 && b[1] == 0 && b[2] == 0 && b[3] == 1, "127.0.0.1 byte content");
        InetAddress c = InetAddress.getByName("255.255.255.255");
        byte[] d = c.getAddress();
        check((d[0] & 0xff) == 255 && (d[3] & 0xff) == 255, "255.255.255.255 byte content");
        // toString of a literal lookup has no host part (no reverse DNS recorded)
        check("/1.2.3.4".equals(InetAddress.getByName("1.2.3.4").toString()),
                "toString of unnamed literal is /dotted-quad");
        // obsolete partial forms are still accepted (legacy behaviour, both JDKs)
        check("1.2.0.3".equals(InetAddress.getByName("1.2.3").getHostAddress()),
                "3-part form 1.2.3 -> 1.2.0.3");
        check("1.2.3.4".equals(InetAddress.getByName("01.2.3.4").getHostAddress()),
                "leading zero accepted 01.2.3.4 -> 1.2.3.4");
        // malformed IPv4 literals -> UnknownHostException (no DNS for dotted forms)
        String[] bad = {"999.1.2.3", "256.1.1.1", "-1.2.3.4", "1.2.3.-4", "1.2.3.4.5",
                        "1.2.3.256", "1.2.3..4", "[1.2.3.4]"};
        for (String s : bad) {
            checkThrows(UnknownHostException.class, () -> InetAddress.getByName(s),
                    "malformed literal " + s);
        }
    }

    static void testIPv6LiteralMatrix() throws Exception {
        // every IPv6 literal normalises to the full uncompressed hextext form
        check("0:0:0:0:0:0:0:1".equals(InetAddress.getByName("::1").getHostAddress()),
                "::1 -> full form");
        InetAddress v61 = InetAddress.getByName("::1");
        InetAddress v61b = InetAddress.getByName("0:0:0:0:0:0:0:1");
        check(v61 instanceof java.net.Inet6Address, "::1 is Inet6Address");
        check(v61b instanceof java.net.Inet6Address, "0:0:0:0:0:0:0:1 is Inet6Address");
        check(v61.equals(v61b), "compressed and full form equal (byte equality)");
        check(v61.hashCode() == v61b.hashCode(), "compressed/full hashCode equal");
        check("0:0:0:0:0:0:0:0".equals(InetAddress.getByName("::").getHostAddress()),
                ":: -> full form");
        check("1:0:0:0:0:0:0:0".equals(InetAddress.getByName("1::").getHostAddress()),
                "1:: -> full form");
        check("2001:db8:0:0:0:0:0:1".equals(InetAddress.getByName("2001:db8::1").getHostAddress()),
                "2001:db8::1 -> full form");
        check("2001:db8:0:0:0:0:0:1".equals(InetAddress.getByName("2001:DB8::1").getHostAddress()),
                "IPv6 hex digits case-insensitive");
        check("1:2:3:4:5:6:7:8".equals(InetAddress.getByName("1:2:3:4:5:6:7:8").getHostAddress()),
                "full form stays full form");
        InetAddress fe = InetAddress.getByName("fe80::1");
        check(fe instanceof java.net.Inet6Address, "fe80::1 is Inet6Address");
        check(fe.getAddress().length == 16, "IPv6 address length 16");
        check(fe.getAddress()[0] == (byte) 0xfe && fe.getAddress()[1] == (byte) 0x80,
                "fe80::1 first two bytes");
        check(fe.getAddress()[15] == 1, "fe80::1 last byte");
        // bracketed IPv6 form
        InetAddress br = InetAddress.getByName("[::1]");
        check(br instanceof java.net.Inet6Address, "[::1] accepted");
        check(br.equals(v61), "[::1] equals ::1");
        // IPv4-mapped literals collapse to Inet4Address (probed, JDK-8 == JDK-27)
        InetAddress mapped = InetAddress.getByName("::FFFF:127.0.0.1");
        check(mapped instanceof java.net.Inet4Address, "::FFFF:127.0.0.1 -> Inet4Address");
        check("127.0.0.1".equals(mapped.getHostAddress()), "mapped host address 127.0.0.1");
        check(mapped.isLoopbackAddress(), "mapped address is loopback");
        InetAddress mapped2 = InetAddress.getByName("::ffff:1.2.3.4");
        check(mapped2 instanceof java.net.Inet4Address, "::ffff:1.2.3.4 -> Inet4Address");
        check("1.2.3.4".equals(mapped2.getHostAddress()), "mapped lowercase host address");
        check(mapped2.equals(InetAddress.getByAddress(ipv4(1, 2, 3, 4))),
                "mapped equals byte-constructed Inet4Address");
        // 16-byte round trip via getByAddress
        InetAddress db8 = InetAddress.getByName("2001:db8::1");
        InetAddress db8b = InetAddress.getByAddress(db8.getAddress());
        check(db8.equals(db8b), "16-byte getByAddress round trip equals");
        check(db8.hashCode() == db8b.hashCode(), "16-byte round trip hashCode");
        check("2001:db8:0:0:0:0:0:1".equals(db8b.getHostAddress()), "16-byte round trip form");
        // getAllByName on literals
        check(InetAddress.getAllByName("::1").length == 1, "getAllByName(::1) single");
        check(InetAddress.getAllByName("127.0.0.1").length == 1, "getAllByName(127.0.0.1) single");
        InetAddress[] loc = InetAddress.getAllByName(null);
        check(loc != null && loc.length >= 1, "getAllByName(null) non-empty");
        for (InetAddress x : loc) check(x.isLoopbackAddress(), "getAllByName(null) item loopback");
    }

    static void testNullHostAndLocalhost() throws Exception {
        // getByName(null) == local host == loopback per spec ("localhost")
        InetAddress n = InetAddress.getByName(null);
        check(n != null, "getByName(null) non-null");
        check(n.isLoopbackAddress(), "getByName(null) is loopback");
        InetAddress lo = InetAddress.getByName("localhost");
        check(lo != null && lo.isLoopbackAddress(), "getByName(localhost) loopback");
        check(lo.toString().startsWith("localhost/"), "localhost toString keeps host part");
        InetAddress[] all = InetAddress.getAllByName("localhost");
        check(all.length >= 1, "getAllByName(localhost) non-empty");
        for (InetAddress x : all) check(x.isLoopbackAddress(), "localhost item loopback");
        // getLocalHost: only structural guarantees (hostname is environment specific)
        InetAddress lh = InetAddress.getLocalHost();
        check(lh != null, "getLocalHost non-null");
        check(lh.getHostAddress() != null, "getLocalHost hostAddress non-null");
        check(lh.getAddress().length == 4 || lh.getAddress().length == 16,
                "getLocalHost address length 4 or 16");
        // getLoopbackAddress: probed Inet4Address/127.0.0.1 on both JDKs (dual-stack)
        InetAddress loop = InetAddress.getLoopbackAddress();
        check(loop instanceof java.net.Inet4Address, "getLoopbackAddress is Inet4Address (dual-stack)");
        check("127.0.0.1".equals(loop.getHostAddress()), "getLoopbackAddress host address 127.0.0.1");
        check(loop.getAddress()[0] == 127, "loopback first byte 127");
    }

    static void testGetByAddressMatrix() throws Exception {
        // byte[] -> InetAddress -> byte[] full round trip incl. hashCode
        int[][] samples = {
            {0, 0, 0, 0}, {255, 255, 255, 255}, {1, 2, 3, 4}, {127, 0, 0, 1},
            {10, 20, 30, 40}, {172, 16, 5, 5}, {192, 168, 0, 1}, {169, 254, 7, 7},
            {224, 0, 0, 1}, {239, 255, 255, 255}
        };
        for (int[] s : samples) {
            byte[] raw = ipv4(s[0], s[1], s[2], s[3]);
            InetAddress a = InetAddress.getByAddress(raw);
            InetAddress b = InetAddress.getByAddress(ipv4(s[0], s[1], s[2], s[3]));
            check(a.equals(b), "getByAddress deterministic for " + s[0] + "." + s[1] + "." + s[2] + "." + s[3]);
            check(a.hashCode() == b.hashCode(), "getByAddress hashCode stable");
            byte[] back = a.getAddress();
            check(java.util.Arrays.equals(raw, back), "byte round trip " + s[0] + "." + s[1] + "." + s[2] + "." + s[3]);
        }
        // 0.0.0.0 legal
        InetAddress zero = InetAddress.getByAddress(new byte[4]);
        check("0.0.0.0".equals(zero.getHostAddress()), "byte[4] zeros -> 0.0.0.0");
        check(zero.isAnyLocalAddress(), "0.0.0.0 any-local");
        // null byte[] -> UnknownHostException (probed: NOT NullPointerException)
        checkThrows(UnknownHostException.class, () -> InetAddress.getByAddress((byte[]) null),
                "getByAddress(null) UHE");
        try {
            InetAddress.getByAddress((byte[]) null);
        } catch (UnknownHostException e) {
            check(e.getMessage() != null && e.getMessage().contains("illegal length"),
                    "getByAddress(null) message mentions illegal length");
        }
        // host + byte[] matrix
        InetAddress named = InetAddress.getByAddress("My.Host.Example", ipv4(1, 2, 3, 4));
        check("My.Host.Example".equals(named.getHostName()), "host name case preserved");
        check("My.Host.Example/1.2.3.4".equals(named.toString()), "toString host/address form");
        check("1.2.3.4".equals(named.getHostAddress()), "named host address");
        InetAddress nullHost = InetAddress.getByAddress(null, ipv4(1, 2, 3, 4));
        check(nullHost.equals(named), "null host same bytes equal");
        checkThrows(UnknownHostException.class,
                () -> InetAddress.getByAddress("h", new byte[]{1, 2, 3, 4, 5}), "host+byte[5] UHE");
        checkThrows(UnknownHostException.class,
                () -> InetAddress.getByAddress("h", new byte[]{1, 2, 3}), "host+byte[3] UHE");
        checkThrows(UnknownHostException.class,
                () -> InetAddress.getByAddress("h", new byte[0]), "host+byte[0] UHE");
        // getCanonicalHostName never null; value may be the literal (reverse DNS env dependent)
        check(InetAddress.getByAddress("h", ipv4(1, 2, 3, 4)).getCanonicalHostName() != null,
                "getCanonicalHostName non-null");
    }

    static void testHashCodeExactValues() throws Exception {
        // hashCode is the address as int: documented behaviour, probed stable both JDKs
        check(InetAddress.getByName("127.0.0.1").hashCode() == 2130706433, "hashCode 127.0.0.1");
        check(InetAddress.getByName("1.2.3.4").hashCode() == 16909060, "hashCode 1.2.3.4");
        check(InetAddress.getByName("0.0.0.0").hashCode() == 0, "hashCode 0.0.0.0");
        check(InetAddress.getByName("::1").hashCode() == 1, "hashCode ::1 (first 4 ints xor)");
        check(InetAddress.getByName("::").hashCode() == 0, "hashCode ::");
        check(InetAddress.getByName("1.2.3.4").hashCode()
                        == InetAddress.getByAddress(ipv4(1, 2, 3, 4)).hashCode(),
                "hashCode equal across construction paths");
    }

    static void testClassificationIPv4Boundaries() throws Exception {
        // 127.0.0.0/8 loopback: whole block, boundaries outside
        check(InetAddress.getByName("127.0.0.0").isLoopbackAddress(), "127.0.0.0 loopback");
        check(InetAddress.getByName("127.255.255.255").isLoopbackAddress(), "127.255.255.255 loopback");
        check(!InetAddress.getByName("126.255.255.255").isLoopbackAddress(), "126.x not loopback");
        check(!InetAddress.getByName("128.0.0.0").isLoopbackAddress(), "128.0.0.0 not loopback");
        // any-local only exactly 0.0.0.0
        check(InetAddress.getByName("0.0.0.1").isAnyLocalAddress() == false, "0.0.0.1 not any-local");
        // link-local 169.254/16
        check(InetAddress.getByName("169.254.0.0").isLinkLocalAddress(), "169.254.0.0 link-local");
        check(InetAddress.getByName("169.254.255.255").isLinkLocalAddress(), "169.254.255.255 link-local");
        check(!InetAddress.getByName("169.253.255.255").isLinkLocalAddress(), "169.253 not link-local");
        check(!InetAddress.getByName("169.255.0.0").isLinkLocalAddress(), "169.255 not link-local");
        // site-local 10/8, 172.16/12, 192.168/16 incl. boundaries
        check(InetAddress.getByName("10.0.0.0").isSiteLocalAddress(), "10.0.0.0 site-local");
        check(InetAddress.getByName("10.255.255.255").isSiteLocalAddress(), "10.255.255.255 site-local");
        check(!InetAddress.getByName("9.255.255.255").isSiteLocalAddress(), "9.x not site-local");
        check(!InetAddress.getByName("11.0.0.0").isSiteLocalAddress(), "11.x not site-local");
        check(InetAddress.getByName("172.16.0.0").isSiteLocalAddress(), "172.16.0.0 site-local");
        check(InetAddress.getByName("172.31.255.255").isSiteLocalAddress(), "172.31.255.255 site-local");
        check(!InetAddress.getByName("172.15.255.255").isSiteLocalAddress(), "172.15 not site-local");
        check(!InetAddress.getByName("172.32.0.0").isSiteLocalAddress(), "172.32 not site-local");
        check(InetAddress.getByName("192.168.0.0").isSiteLocalAddress(), "192.168.0.0 site-local");
        check(InetAddress.getByName("192.168.255.255").isSiteLocalAddress(), "192.168.255.255 site-local");
        check(!InetAddress.getByName("192.167.255.255").isSiteLocalAddress(), "192.167 not site-local");
        check(!InetAddress.getByName("192.169.0.0").isSiteLocalAddress(), "192.169 not site-local");
        // multicast 224.0.0.0/4 boundaries
        check(!InetAddress.getByName("223.255.255.255").isMulticastAddress(), "223.x not multicast");
        check(InetAddress.getByName("224.0.0.0").isMulticastAddress(), "224.0.0.0 multicast");
        check(InetAddress.getByName("239.255.255.255").isMulticastAddress(), "239.255.255.255 multicast");
        check(!InetAddress.getByName("240.0.0.0").isMulticastAddress(), "240.0.0.0 not multicast");
        // classification methods on unicast address all false
        InetAddress u = InetAddress.getByName("8.8.8.8");
        check(!u.isMulticastAddress(), "8.8.8.8 not multicast");
        check(!u.isAnyLocalAddress(), "8.8.8.8 not any-local");
        check(!u.isLoopbackAddress(), "8.8.8.8 not loopback");
        check(!u.isLinkLocalAddress(), "8.8.8.8 not link-local");
        check(!u.isSiteLocalAddress(), "8.8.8.8 not site-local");
    }

    static void testClassificationIPv6Boundaries() throws Exception {
        InetAddress lb = InetAddress.getByName("::1");
        check(lb.isLoopbackAddress(), "::1 loopback");
        check(!lb.isAnyLocalAddress(), "::1 not any-local");
        InetAddress any = InetAddress.getByName("::");
        check(any.isAnyLocalAddress(), ":: any-local");
        check(!any.isLoopbackAddress(), ":: not loopback");
        check(InetAddress.getByName("fe80::").isLinkLocalAddress(), "fe80:: link-local");
        check(!InetAddress.getByName("fe7f::1").isLinkLocalAddress(), "fe7f:: not link-local");
        check(InetAddress.getByName("febf::1").isLinkLocalAddress(), "febf:: link-local (fe80::/10 top)");
        check(!InetAddress.getByName("fec0::1").isLinkLocalAddress(), "fec0:: not link-local");
        // site-local ULA: fec0::/10 per implementation (probed both JDKs)
        check(InetAddress.getByAddress(ipv6(0xfe, 0xc0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isSiteLocalAddress(),
                "fec0::1 site-local");
        check(InetAddress.getByAddress(ipv6(0xfe, 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isSiteLocalAddress(),
                "feff::1 site-local");
        check(!InetAddress.getByAddress(ipv6(0xfe, 0xbf, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isSiteLocalAddress(),
                "febf:: not site-local");
        // fc00::/7 ULA is NOT site-local in the JDK classification (probed)
        check(!InetAddress.getByAddress(ipv6(0xfc, 0x00)).isSiteLocalAddress(), "fc00:: not site-local");
        check(!InetAddress.getByAddress(ipv6(0xfd, 0x00)).isSiteLocalAddress(), "fd00:: not site-local");
        // multicast ff00::/8 both ends
        check(InetAddress.getByAddress(ipv6(0xff, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isMulticastAddress(),
                "ff00::1 multicast");
        check(InetAddress.getByAddress(ipv6(0xff, 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isMulticastAddress(),
                "ffff::1 multicast");
        check(!InetAddress.getByAddress(ipv6(0xfe, 0xff, 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isMulticastAddress(),
                "feff:ff00:: not multicast");
        // unicast v6 all false
        InetAddress u = InetAddress.getByName("2001:db8::1");
        check(!u.isMulticastAddress(), "2001:db8::1 not multicast");
        check(!u.isLoopbackAddress(), "2001:db8::1 not loopback");
        check(!u.isAnyLocalAddress(), "2001:db8::1 not any-local");
        check(!u.isLinkLocalAddress(), "2001:db8::1 not link-local");
        check(!u.isSiteLocalAddress(), "2001:db8::1 not site-local");
    }

    static void testMulticastScopeMatrix() throws Exception {
        // matrix probed identical on JDK-8 and JDK-27
        String[][] v4 = {
            {"224.0.0.0",  "link"}, {"224.0.0.1", "link"}, {"224.0.0.255", "link"},
            {"224.0.1.1",  "global"}, {"224.0.1.2", "global"},
            {"239.0.0.1",  "none"},
            {"239.192.0.0", "org"}, {"239.192.0.1", "org"},
            {"239.251.255.255", "none"},
            {"239.255.255.254", "site"}, {"239.255.255.255", "site"}
        };
        for (String[] row : v4) {
            InetAddress a = InetAddress.getByName(row[0]);
            boolean g = "global".equals(row[1]);
            boolean l = "link".equals(row[1]);
            boolean s = "site".equals(row[1]);
            boolean o = "org".equals(row[1]);
            boolean n = "none".equals(row[1]);
            check(a.isMCGlobal() == g, row[0] + " isMCGlobal=" + g);
            check(a.isMCLinkLocal() == l, row[0] + " isMCLinkLocal=" + l);
            check(a.isMCSiteLocal() == s, row[0] + " isMCSiteLocal=" + s);
            check(a.isMCOrgLocal() == o, row[0] + " isMCOrgLocal=" + o);
            check(!a.isMCNodeLocal(), row[0] + " isMCNodeLocal=false for IPv4");
            if (n) check(!g && !l && !s && !o, row[0] + " no scope flags");
        }
        String[][] v6 = {
            {"ff01::1", "node"}, {"ff02::1", "link"}, {"ff05::1", "site"},
            {"ff08::1", "org"}, {"ff0e::1", "global"}, {"ff0f::1", "none"}
        };
        for (String[] row : v6) {
            InetAddress a = InetAddress.getByName(row[0]);
            check("node".equals(row[1]) == a.isMCNodeLocal(), row[0] + " node");
            check("link".equals(row[1]) == a.isMCLinkLocal(), row[0] + " link");
            check("site".equals(row[1]) == a.isMCSiteLocal(), row[0] + " site");
            check("org".equals(row[1]) == a.isMCOrgLocal(), row[0] + " org");
            check("global".equals(row[1]) == a.isMCGlobal(), row[0] + " global");
        }
        // unicast: all scope methods false
        InetAddress u = InetAddress.getByName("8.8.8.8");
        check(!u.isMCGlobal() && !u.isMCNodeLocal() && !u.isMCLinkLocal()
                        && !u.isMCSiteLocal() && !u.isMCOrgLocal(),
                "unicast has no MC scope");
    }

    static void testReachability() throws Exception {
        // loopback reachability is local-only (no root / no real ICMP needed)
        check(InetAddress.getByName("127.0.0.1").isReachable(200), "127.0.0.1 reachable");
        check(InetAddress.getByName("::1").isReachable(200), "::1 reachable");
        check(InetAddress.getByName("127.0.0.1").isReachable(0), "127.0.0.1 isReachable(0) true");
        // negative timeout -> IAE with exact probed message (identical on both JDKs)
        try {
            InetAddress.getByName("127.0.0.1").isReachable(-1);
            check(false, "isReachable(-1) must throw");
        } catch (IllegalArgumentException e) {
            check("timeout can't be negative".equals(e.getMessage()), "IAE message exact");
        }
        checkThrows(IllegalArgumentException.class,
                () -> InetAddress.getByName("127.0.0.1").isReachable(Integer.MIN_VALUE),
                "isReachable(MIN_VALUE) IAE");
        // netif variant with null NetworkInterface (any interface) and negative timeout
        check(InetAddress.getByName("127.0.0.1").isReachable(null, 0, 100),
                "isReachable(null netif, ttl 0, 100ms) on loopback");
        checkThrows(IllegalArgumentException.class,
                () -> InetAddress.getByName("127.0.0.1").isReachable(null, 0, -5),
                "isReachable(null,0,-5) IAE");
    }

    static void testCrossConstructionEquality() throws Exception {
        // three construction paths to the same bytes are all equal
        InetAddress viaName = InetAddress.getByName("1.2.3.4");
        InetAddress viaBytes = InetAddress.getByAddress(ipv4(1, 2, 3, 4));
        InetAddress viaNamed = InetAddress.getByAddress("other", ipv4(1, 2, 3, 4));
        check(viaName.equals(viaBytes), "name vs bytes");
        check(viaName.equals(viaNamed), "name vs named-bytes");
        check(viaBytes.equals(viaNamed), "bytes vs named-bytes");
        check(viaName.hashCode() == viaNamed.hashCode(), "hashCode equal across all paths");
        // IPv4 vs IPv6 never equal, symmetric
        InetAddress v6 = InetAddress.getByAddress(new byte[16]);
        InetAddress v4 = InetAddress.getByAddress(ipv4(1, 2, 3, 4));
        check(!v4.equals(v6), "v4 != v6");
        check(!v6.equals(v4), "v6 != v4");
        // mapped v6 literal equals plain v4 literal
        check(InetAddress.getByName("::ffff:1.2.3.4").equals(InetAddress.getByName("1.2.3.4")),
                "mapped literal equals plain v4 literal");
    }

    static void testSerializationDeep() throws Exception {
        // concrete subtypes survive serialization
        InetAddress v6 = roundTrip(InetAddress.getByName("::1"));
        check(v6 instanceof java.net.Inet6Address, "serialized ::1 stays Inet6Address");
        check("0:0:0:0:0:0:0:1".equals(v6.getHostAddress()), "serialized ::1 full form");
        check(v6.isLoopbackAddress(), "serialized ::1 loopback");
        InetAddress v4 = roundTrip(InetAddress.getByName("1.2.3.4"));
        check(v4 instanceof java.net.Inet4Address, "serialized 1.2.3.4 stays Inet4Address");
        check("1.2.3.4".equals(v4.getHostAddress()), "serialized 1.2.3.4 host address");
        InetAddress loop = roundTrip(InetAddress.getLoopbackAddress());
        check(loop.isLoopbackAddress(), "serialized loopback is loopback");
        check(loop.equals(InetAddress.getLoopbackAddress()), "serialized loopback equals fresh");
        InetAddress named = roundTrip(InetAddress.getByAddress("keep.me", ipv4(10, 1, 2, 3)));
        check("keep.me".equals(named.getHostName()), "serialized host name preserved");
        check("10.1.2.3".equals(named.getHostAddress()), "serialized named host address");
        check(named.equals(InetAddress.getByAddress(ipv4(10, 1, 2, 3))), "serialized equals fresh bytes");
        check(named.hashCode() == InetAddress.getByAddress(ipv4(10, 1, 2, 3)).hashCode(),
                "serialized hashCode contract");
    }

    public static void main(String[] args) throws Exception {
        testGetByAddress();
        testDefensiveCopy();
        testEqualsHashCode();
        testClassification();
        testLocalhostAndLoopback();
        testSerialization();
        testIPv4LiteralMatrix();
        testIPv6LiteralMatrix();
        testNullHostAndLocalhost();
        testGetByAddressMatrix();
        testHashCodeExactValues();
        testClassificationIPv4Boundaries();
        testClassificationIPv6Boundaries();
        testMulticastScopeMatrix();
        testReachability();
        testCrossConstructionEquality();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
