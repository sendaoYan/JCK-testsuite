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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
