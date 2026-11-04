/*
 * @test
 * @summary java.net.InetAddress conformance: white-box structure + black-box behavior.
 * @run main InetAddressTest
 */

import java.lang.reflect.*;
import java.net.*;

public class InetAddressTest {
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
        Class<?> c = Class.forName("java.net.InetAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getMethod("getByAddress", byte[].class) != null, "getByAddress(byte[])");
        check(c.getMethod("getByName", String.class) != null, "getByName");
        check(c.getMethod("getLoopbackAddress") != null, "getLoopbackAddress");
        check(c.getMethod("ofLiteral", String.class) != null, "ofLiteral");
    }

    static void testBlackBox() throws Exception {
        InetAddress lb = InetAddress.getLoopbackAddress();
        check(lb.isLoopbackAddress(), "loopback flag");
        check(lb.getAddress().length == 4 || lb.getAddress().length == 16, "loopback address length");

        InetAddress byAddr = InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
        check(byAddr instanceof Inet4Address, "getByAddress -> Inet4Address");
        check(byAddr.getHostAddress().equals("127.0.0.1"), "hostAddress");
        check(byAddr.isLoopbackAddress(), "byAddr loopback");
        check(byAddr.getHostName() != null, "getHostName non-null");

        InetAddress named = InetAddress.getByAddress("myhost", new byte[]{10, 0, 0, 1});
        check(named.getHostName().equals("myhost"), "named getHostName");
        check(named.getHostAddress().equals("10.0.0.1"), "named getHostAddress");

        InetAddress local = InetAddress.getByName("localhost");
        check(local.isLoopbackAddress(), "localhost loopback");

        InetAddress lit = InetAddress.getByName("127.0.0.1");
        check(lit.getHostAddress().equals("127.0.0.1"), "getByName literal");

        InetAddress[] all = InetAddress.getAllByName("127.0.0.1");
        check(all.length == 1 && all[0].getHostAddress().equals("127.0.0.1"), "getAllByName literal");

        InetAddress ofLit = InetAddress.ofLiteral("127.0.0.1");
        check(ofLit.getHostAddress().equals("127.0.0.1"), "ofLiteral");

        check(byAddr.equals(InetAddress.getByAddress(new byte[]{127, 0, 0, 1})), "equals");
        check(byAddr.hashCode() == InetAddress.getByAddress(new byte[]{127, 0, 0, 1}).hashCode(), "hashCode");
        check(!byAddr.equals(named), "not equals different address");
        check(byAddr.toString().endsWith("/127.0.0.1"), "toString address suffix");

        fails(UnknownHostException.class, "bad address length", () -> InetAddress.getByAddress(new byte[]{1, 2, 3}));
        fails(UnknownHostException.class, "null bytes", () -> InetAddress.getByAddress(null));
        check(InetAddress.getByAddress(null, new byte[]{127, 0, 0, 1}).getHostAddress().equals("127.0.0.1"), "null host still yields address");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
