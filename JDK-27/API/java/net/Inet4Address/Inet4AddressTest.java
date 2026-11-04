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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
