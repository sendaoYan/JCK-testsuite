/*
 * @test
 * @summary java.net.Inet6Address conformance: full contract coverage. final class
 *          with no public constructors, IPv6 literal -> Inet6Address typing,
 *          full-form getHostAddress normalisation matrix, scope id handling
 *          (numeric scope via getByAddress, equals ignores scope - probed),
 *          isIPv4CompatibleAddress byte rule, classification boundaries,
 *          equals/hashCode, serialization with scope. Values probed identical
 *          on JDK-8 and JDK-27.
 * @run main Inet6AddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Inet6AddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static byte[] bytes(int... v) {
        byte[] b = new byte[16];
        for (int i = 0; i < v.length; i++) b[i] = (byte) v[i];
        return b;
    }

    static Inet6Address v6(String literal) throws Exception {
        return (Inet6Address) InetAddress.getByName(literal);
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

    static void testOriginal() throws Exception {
        byte[] lb = new byte[16]; lb[15] = 1;
        Inet6Address a = (Inet6Address) InetAddress.getByAddress(lb);
        check(a.isLoopbackAddress(), "isLoopbackAddress");
        check("0:0:0:0:0:0:0:1".equals(a.getHostAddress()), "getHostAddress");
        check(a.getAddress().length == 16, "16 bytes");
        check(a instanceof InetAddress, "is InetAddress");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Inet6Address");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(InetAddress.class.isAssignableFrom(c), "extends InetAddress");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        int publicCtors = 0;
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ctor.getModifiers())) publicCtors++;
        }
        check(publicCtors == 0, "no public constructors (factory-only class)");
    }

    static void testTextFormNormalisation() throws Exception {
        // every IPv6 text form normalises to the full uncompressed hextext form
        String[][] cases = {
            {"::1", "0:0:0:0:0:0:0:1"},
            {"0:0:0:0:0:0:0:1", "0:0:0:0:0:0:0:1"},
            {"::", "0:0:0:0:0:0:0:0"},
            {"0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0:0"},
            {"1::", "1:0:0:0:0:0:0:0"},
            {"1:2:3:4:5:6:7:8", "1:2:3:4:5:6:7:8"},
            {"2001:db8::1", "2001:db8:0:0:0:0:0:1"},
            {"2001:DB8::1", "2001:db8:0:0:0:0:0:1"},   // hex digits lower-cased
            {"FFFF::1", "ffff:0:0:0:0:0:0:1"},
            {"fe80::1", "fe80:0:0:0:0:0:0:1"},
        };
        for (String[] c : cases) {
            check(c[1].equals(v6(c[0]).getHostAddress()),
                    "\"" + c[0] + "\" -> \"" + c[1] + "\"");
        }
        check(v6("::1") instanceof Inet6Address, "::1 is Inet6Address");
        check(v6("::").isAnyLocalAddress(), ":: any-local");
        check(!v6("::").isLoopbackAddress(), ":: not loopback");
        check(v6("::1").isLoopbackAddress(), "::1 loopback");
        check(!v6("::1").isAnyLocalAddress(), "::1 not any-local");
        check(v6("fe80::1").isLinkLocalAddress(), "fe80::1 link-local");
        check(!v6("2001:db8::1").isMulticastAddress(), "2001:db8::1 not multicast");
        // bracketed form accepted by getByName
        check(v6("[::1]").equals(v6("::1")), "[::1] equals ::1");
        // byte round trip
        Inet6Address a = v6("2001:db8::1");
        InetAddress b = InetAddress.getByAddress(a.getAddress());
        check(a.equals(b), "16-byte round trip equals");
        check("2001:db8:0:0:0:0:0:1".equals(b.getHostAddress()), "16-byte round trip form");
        check(a.getAddress()[0] == (byte) 0x20 && a.getAddress()[1] == 0x01, "2001 first bytes");
    }

    static void testMulticastScopes() throws Exception {
        // ff0X scope nibble matrix (probed identical on JDK-8/JDK-27)
        check(v6("ff01::1").isMCNodeLocal(), "ff01 node-local");
        check(!v6("ff01::1").isMCLinkLocal(), "ff01 not link");
        check(v6("ff02::1").isMCLinkLocal(), "ff02 link-local");
        check(!v6("ff02::1").isMCNodeLocal(), "ff02 not node");
        check(v6("ff05::1").isMCSiteLocal(), "ff05 site-local");
        check(v6("ff08::1").isMCOrgLocal(), "ff08 org-local");
        check(v6("ff0e::1").isMCGlobal(), "ff0e global");
        check(!v6("ff0f::1").isMCNodeLocal() && !v6("ff0f::1").isMCLinkLocal()
                && !v6("ff0f::1").isMCSiteLocal() && !v6("ff0f::1").isMCOrgLocal()
                && !v6("ff0f::1").isMCGlobal(), "ff0f reserved: no scope");
        check(v6("ff02::1").isMulticastAddress(), "ff02::1 multicast");
        check(InetAddress.getByAddress(bytes(0xff, 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isMulticastAddress(),
                "ffff::1 multicast");
    }

    static void testScopeId() throws Exception {
        byte[] fe80 = bytes(0xfe, 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
        Inet6Address s5 = Inet6Address.getByAddress("h", fe80, 5);
        check(s5.getScopeId() == 5, "numeric scope preserved");
        check(s5.getHostAddress().endsWith("%5"), "host address carries %5");
        check("h".equals(s5.getHostName()), "host name preserved");
        check("h/fe80:0:0:0:0:0:0:1%5".equals(s5.toString()), "toString with scope");
        check(s5.getScopedInterface() == null, "numeric scope -> null scoped interface");
        Inet6Address s0 = Inet6Address.getByAddress(null, fe80, 0);
        check(s0.getScopeId() == 0, "scope 0");
        // numeric scope 0 set via getByAddress is rendered (probed both JDKs)
        check("fe80:0:0:0:0:0:0:1%0".equals(s0.getHostAddress()), "numeric scope 0 rendered");
        check(s0.getScopedInterface() == null, "scope 0 no scoped interface");
        // null NetworkInterface variant
        Inet6Address viaNif = Inet6Address.getByAddress("h", new byte[16], (java.net.NetworkInterface) null);
        check(viaNif.getScopeId() == 0, "null nif -> scope 0");
        check(viaNif.getScopedInterface() == null, "null nif -> null scoped interface");
        check("0:0:0:0:0:0:0:0".equals(viaNif.getHostAddress()), "null nif host address");
        // equals/hashCode ignore the scope id (probed on both JDKs)
        Inet6Address s7 = Inet6Address.getByAddress("h", fe80, 7);
        Inet6Address s5b = Inet6Address.getByAddress("k", fe80, 5);
        check(s5.equals(s7), "equals ignores scope id");
        check(s7.equals(s5), "scope equality symmetric");
        check(s5.equals(s5b), "equals ignores hostname");
        check(s5.hashCode() == s5b.hashCode(), "hashCode stable across hostnames");
        // negative cases
        try {
            Inet6Address.getByAddress("h", null, 0);
            check(false, "null bytes must throw");
        } catch (UnknownHostException e) {
            check(true, "null bytes -> UHE");
        }
        try {
            Inet6Address.getByAddress("h", new byte[4], 0);
            check(false, "4-byte input must throw");
        } catch (UnknownHostException e) {
            check(true, "4 bytes -> UHE");
        }
        try {
            Inet6Address.getByAddress("h", new byte[5], 3);
            check(false, "5-byte input must throw");
        } catch (UnknownHostException e) {
            check(true, "5 bytes -> UHE");
        }
    }

    static void testIPv4CompatibleAddress() throws Exception {
        // rule: bytes 0..9 all zero (probed; ::1 true, ::0:1 false, ::1:0 true)
        check(Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), 0)
                .isIPv4CompatibleAddress(), "::1 compatible");
        check(Inet6Address.getByAddress(null, new byte[16], 0).isIPv4CompatibleAddress(),
                ":: (all zero) compatible");
        check(Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0), 0)
                .isIPv4CompatibleAddress(), "::1:0 compatible");
        check(!Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0), 0)
                .isIPv4CompatibleAddress(), "::0:1:0:0 not compatible (byte10 nonzero)");
        check(!v6("2001:db8::1").isIPv4CompatibleAddress(), "2001:db8::1 not compatible");
        check(!v6("fe80::1").isIPv4CompatibleAddress(), "fe80::1 not compatible");
        // v4-mapped literal collapses to Inet4Address: not an Inet6Address at all
        check(InetAddress.getByName("::ffff:1.2.3.4") instanceof java.net.Inet4Address,
                "v4-mapped literal -> Inet4Address (probed)");
        check(InetAddress.getByName("::ffff:1.2.3.4").getAddress().length == 4,
                "v4-mapped collapsed to 4 bytes");
    }

    static void testEqualsHashCodeSerialization() throws Exception {
        check(v6("::1").equals(v6("0:0:0:0:0:0:0:1")), "compressed equals full form");
        check(v6("::1").hashCode() == 1, "hashCode ::1 == 1");
        check(v6("::").hashCode() == 0, "hashCode :: == 0");
        check(v6("::1").equals(InetAddress.getByAddress(bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))),
                "literal equals byte-built");
        check(!v6("::1").equals(v6("::")), "::1 != ::");
        check(!v6("::1").equals(InetAddress.getByName("127.0.0.1")), "::1 != 127.0.0.1");
        check(!v6("::1").equals(null), "null false");
        check(!v6("::1").equals("::1"), "other type false");
        // serialization preserves concrete type and scope
        Inet6Address back = roundTrip(v6("::1"));
        check(back.getClass() == Inet6Address.class, "serialized type preserved");
        check("0:0:0:0:0:0:0:1".equals(back.getHostAddress()), "serialized full form");
        check(back.isLoopbackAddress(), "serialized loopback");
        Inet6Address scoped = roundTrip(Inet6Address.getByAddress("h", bytes(0xfe, 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), 5));
        check(scoped.getScopeId() == 5, "serialized scope preserved");
        check(scoped.getHostAddress().endsWith("%5"), "serialized scoped form");
        check(scoped.getHostName().equals("h"), "serialized host name");
        // scoped getByName with non-existent interface name -> UHE (probed)
        try {
            InetAddress.getByName("fe80::1%no.such.iface.definitely");
            check(false, "bad scope name must throw");
        } catch (UnknownHostException e) {
            check(true, "bad scope interface -> UHE");
        }
    }

    public static void main(String[] args) throws Exception {
        testOriginal();
        testWhiteBox();
        testTextFormNormalisation();
        testMulticastScopes();
        testScopeId();
        testIPv4CompatibleAddress();
        testEqualsHashCodeSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
