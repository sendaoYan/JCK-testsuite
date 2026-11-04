/*
 * @test
 * @summary java.net.Inet6Address conformance: white-box structure + black-box behavior.
 * @run main Inet6AddressTest
 */

import java.lang.reflect.*;
import java.net.*;

public class Inet6AddressTest {
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

    static byte[] bytes(int... v) { byte[] b = new byte[v.length]; for (int i = 0; i < v.length; i++) b[i] = (byte) v[i]; return b; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.Inet6Address");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(InetAddress.class.isAssignableFrom(c), "extends InetAddress");
        check(c.getMethod("getByAddress", String.class, byte[].class, int.class) != null, "getByAddress");
        check(c.getMethod("ofLiteral", String.class) != null, "ofLiteral");
        check(c.getMethod("getScopeId") != null, "getScopeId");
    }

    static void testBlackBox() throws Exception {
        Inet6Address loop = (Inet6Address) InetAddress.ofLiteral("::1");
        check(loop.isLoopbackAddress(), "::1 loopback");
        check(loop.getAddress().length == 16, "address length 16");
        check(loop.getScopeId() == 0, "::1 scope 0");

        Inet6Address mc = (Inet6Address) InetAddress.ofLiteral("ff02::1");
        check(mc.isMulticastAddress(), "ff02::1 multicast");

        byte[] fe80 = bytes(0xfe, 0x80, 0,0,0,0,0,0,0,0,0,0,0,0,0, 1);
        Inet6Address sc = Inet6Address.getByAddress(null, fe80, 7);
        check(sc.getScopeId() == 7, "scope id");
        check(sc.getHostAddress().endsWith("%7"), "scoped host address");
        check(sc.getAddress().length == 16, "scoped address length");

        Inet6Address named = Inet6Address.getByAddress("myhost", fe80, 3);
        check(named.getHostName().equals("myhost"), "named getHostName");
        check(named.getScopeId() == 3, "named scope");

        Inet6Address lit = (Inet6Address) Inet6Address.ofLiteral("::1");
        check(lit.equals((Inet6Address) InetAddress.ofLiteral("::1")), "equals");
        check(lit.hashCode() == ((Inet6Address) InetAddress.ofLiteral("::1")).hashCode(), "hashCode");

        fails(UnknownHostException.class, "null bytes", () -> Inet6Address.getByAddress(null, null, 0));
        fails(UnknownHostException.class, "wrong length", () -> Inet6Address.getByAddress(null, new byte[4], 0));
        fails(NullPointerException.class, "null literal", () -> Inet6Address.ofLiteral(null));
    }

    // ------------------------------------------------------------------
    // Deepening: probed contract matrix (JDK-8 behaviour identical)
    // ------------------------------------------------------------------

    static Inet6Address lit(String literal) throws Exception {
        return (Inet6Address) InetAddress.getByName(literal);
    }

    static void testTextFormNormalisation() throws Exception {
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
            check(c[1].equals(lit(c[0]).getHostAddress()),
                    "\"" + c[0] + "\" -> \"" + c[1] + "\"");
            check(c[1].equals(InetAddress.ofLiteral(c[0]).getHostAddress()),
                    "ofLiteral(\"" + c[0] + "\") -> \"" + c[1] + "\"");
        }
        check(lit("::").isAnyLocalAddress(), ":: any-local");
        check(!lit("::").isLoopbackAddress(), ":: not loopback");
        check(lit("fe80::1").isLinkLocalAddress(), "fe80::1 link-local");
        check(lit("[::1]").equals(lit("::1")), "[::1] equals ::1 (getByName)");
        check(InetAddress.ofLiteral("[::1]").equals(InetAddress.ofLiteral("::1")),
                "ofLiteral bracketed equals bare");
        Inet6Address a = lit("2001:db8::1");
        InetAddress b = InetAddress.getByAddress(a.getAddress());
        check(a.equals(b), "16-byte round trip equals");
        check("2001:db8:0:0:0:0:0:1".equals(b.getHostAddress()), "16-byte round trip form");
        check(a.getAddress()[0] == (byte) 0x20 && a.getAddress()[1] == 0x01, "2001 first bytes");
    }

    static void testMulticastScopes() throws Exception {
        check(lit("ff01::1").isMCNodeLocal(), "ff01 node-local");
        check(!lit("ff01::1").isMCLinkLocal(), "ff01 not link");
        check(lit("ff02::1").isMCLinkLocal(), "ff02 link-local");
        check(!lit("ff02::1").isMCNodeLocal(), "ff02 not node");
        check(lit("ff05::1").isMCSiteLocal(), "ff05 site-local");
        check(lit("ff08::1").isMCOrgLocal(), "ff08 org-local");
        check(lit("ff0e::1").isMCGlobal(), "ff0e global");
        check(!lit("ff0f::1").isMCNodeLocal() && !lit("ff0f::1").isMCLinkLocal()
                && !lit("ff0f::1").isMCSiteLocal() && !lit("ff0f::1").isMCOrgLocal()
                && !lit("ff0f::1").isMCGlobal(), "ff0f reserved: no scope");
        check(InetAddress.getByAddress(bytes(0xff, 0xff, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)).isMulticastAddress(),
                "ffff::1 multicast");
    }

    static void testScopeIdDeep() throws Exception {
        byte[] fe80 = bytes(0xfe, 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
        Inet6Address s0 = Inet6Address.getByAddress(null, fe80, 0);
        check(s0.getScopeId() == 0, "scope 0");
        // numeric scope 0 set via getByAddress is rendered (probed both JDKs)
        check("fe80:0:0:0:0:0:0:1%0".equals(s0.getHostAddress()), "numeric scope 0 rendered");
        check(s0.getScopedInterface() == null, "scope 0 no scoped interface");
        // numeric scope on non-link-local address also rendered
        byte[] one = new byte[16]; one[15] = 1;
        Inet6Address u5 = Inet6Address.getByAddress(null, one, 5);
        check("0:0:0:0:0:0:0:1%5".equals(u5.getHostAddress()), "::1 scope 5 rendered");
        check(u5.getScopeId() == 5, "::1 scope id 5");
        // null NetworkInterface variant: no numeric scope -> no suffix (probed)
        Inet6Address viaNif = Inet6Address.getByAddress("h", new byte[16], (NetworkInterface) null);
        check(viaNif.getScopeId() == 0, "null nif -> scope 0");
        check(viaNif.getScopedInterface() == null, "null nif -> null scoped interface");
        check("0:0:0:0:0:0:0:0".equals(viaNif.getHostAddress()), "null nif host address no suffix");
        // equals/hashCode ignore the scope id (probed on both JDKs)
        Inet6Address s5 = Inet6Address.getByAddress("h", fe80, 5);
        Inet6Address s7 = Inet6Address.getByAddress("h", fe80, 7);
        Inet6Address s5b = Inet6Address.getByAddress("k", fe80, 5);
        check(s5.equals(s7), "equals ignores scope id");
        check(s7.equals(s5), "scope equality symmetric");
        check(s5.equals(s5b), "equals ignores hostname");
        check(s5.hashCode() == s5b.hashCode(), "hashCode stable across hostnames");
        check("h/fe80:0:0:0:0:0:0:1%5".equals(s5.toString()), "toString with scope");
        fails(UnknownHostException.class, "5 bytes", () -> Inet6Address.getByAddress("h", new byte[5], 3));
    }

    static void testIPv4CompatibleAddress() throws Exception {
        check(Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), 0)
                .isIPv4CompatibleAddress(), "::1 compatible");
        check(Inet6Address.getByAddress(null, new byte[16], 0).isIPv4CompatibleAddress(),
                ":: (all zero) compatible");
        check(Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0), 0)
                .isIPv4CompatibleAddress(), "::1:0 compatible");
        check(!Inet6Address.getByAddress(null, bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0), 0)
                .isIPv4CompatibleAddress(), "::0:1:0:0 not compatible (byte10 nonzero)");
        check(!lit("2001:db8::1").isIPv4CompatibleAddress(), "2001:db8::1 not compatible");
        check(!lit("fe80::1").isIPv4CompatibleAddress(), "fe80::1 not compatible");
        // v4-mapped literal collapses to Inet4Address: not an Inet6Address at all
        check(InetAddress.ofLiteral("::ffff:1.2.3.4") instanceof Inet4Address,
                "v4-mapped ofLiteral -> Inet4Address (probed)");
        check(InetAddress.ofLiteral("::ffff:1.2.3.4").getAddress().length == 4,
                "v4-mapped collapsed to 4 bytes");
    }

    static void testEqualsHashCodeSerialization() throws Exception {
        check(lit("::1").equals(lit("0:0:0:0:0:0:0:1")), "compressed equals full form");
        check(lit("::1").hashCode() == 1, "hashCode ::1 == 1");
        check(lit("::").hashCode() == 0, "hashCode :: == 0");
        check(lit("::1").equals(InetAddress.getByAddress(bytes(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1))),
                "literal equals byte-built");
        check(!lit("::1").equals(lit("::")), "::1 != ::");
        check(!lit("::1").equals(InetAddress.getByName("127.0.0.1")), "::1 != 127.0.0.1");
        check(!lit("::1").equals(null), "null false");
        check(!lit("::1").equals("::1"), "other type false");
        // serialization preserves concrete type and scope
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(lit("::1"));
        oos.writeObject(Inet6Address.getByAddress("h", bytes(0xfe, 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1), 5));
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Inet6Address back = (Inet6Address) ois.readObject();
        Inet6Address scoped = (Inet6Address) ois.readObject();
        check(back.getClass() == Inet6Address.class, "serialized type preserved");
        check("0:0:0:0:0:0:0:1".equals(back.getHostAddress()), "serialized full form");
        check(back.isLoopbackAddress(), "serialized loopback");
        check(scoped.getScopeId() == 5, "serialized scope preserved");
        check(scoped.getHostAddress().endsWith("%5"), "serialized scoped form");
        check(scoped.getHostName().equals("h"), "serialized host name");
        // scoped getByName with non-existent interface name -> UHE (probed)
        fails(UnknownHostException.class, "bad scope interface",
                () -> InetAddress.getByName("fe80::1%no.such.iface.definitely"));
        // Inet6Address.ofLiteral rejects IPv4 literals (strict, probed)
        fails(IllegalArgumentException.class, "ofLiteral v4 literal", () -> Inet6Address.ofLiteral("127.0.0.1"));
        check(lit("::1").equals(Inet6Address.ofLiteral("::1")), "ofLiteral equals getByName");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTextFormNormalisation();
        testMulticastScopes();
        testScopeIdDeep();
        testIPv4CompatibleAddress();
        testEqualsHashCodeSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
