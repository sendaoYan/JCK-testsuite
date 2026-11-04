/*
 * @test
 * @summary java.net.InetSocketAddress conformance: white-box structure + black-box behavior.
 * @run main InetSocketAddressTest
 */

import java.lang.reflect.*;
import java.net.*;

public class InetSocketAddressTest {
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
        Class<?> c = Class.forName("java.net.InetSocketAddress");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(SocketAddress.class.isAssignableFrom(c), "extends SocketAddress");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(InetAddress.class, int.class) != null, "ctor(InetAddress,int)");
        check(c.getConstructor(String.class, int.class) != null, "ctor(String,int)");
        check(c.getMethod("createUnresolved", String.class, int.class) != null, "createUnresolved");
        check(c.getMethod("getPort") != null, "getPort");
    }

    static void testBlackBox() throws Exception {
        // wildcard address from port-only constructor
        InetSocketAddress wild = new InetSocketAddress(9090);
        check(wild.getPort() == 9090, "wildcard port");
        check(wild.getAddress() != null, "wildcard has address");
        check(wild.getAddress().isAnyLocalAddress(), "wildcard any-local");
        check(!wild.isUnresolved(), "wildcard resolved");
        check(wild.getHostString().equals(wild.getAddress().getHostAddress()), "wildcard host string == address literal");

        // hostname + port resolves to loopback
        InetSocketAddress local = new InetSocketAddress("localhost", 8080);
        check(local.getPort() == 8080, "localhost port");
        check(local.getHostString().equals("localhost"), "localhost hostString");
        check(!local.isUnresolved(), "localhost resolved");
        check(local.getAddress().isLoopbackAddress(), "localhost loopback");

        // InetAddress + port
        InetAddress lb = InetAddress.getLoopbackAddress();
        InetSocketAddress byAddr = new InetSocketAddress(lb, 8080);
        check(byAddr.getAddress().equals(lb), "byAddr address");
        check(byAddr.getPort() == 8080, "byAddr port");

        // unresolved
        InetSocketAddress un = InetSocketAddress.createUnresolved("foo.example", 1234);
        check(un.isUnresolved(), "unresolved flag");
        check(un.getAddress() == null, "unresolved null address");
        check(un.getHostString().equals("foo.example"), "unresolved hostString");
        check(un.getHostName().equals("foo.example"), "unresolved hostName");
        check(un.getPort() == 1234, "unresolved port");

        // equals / hashCode
        InetSocketAddress a1 = InetSocketAddress.createUnresolved("h", 1);
        InetSocketAddress a2 = InetSocketAddress.createUnresolved("h", 1);
        check(a1.equals(a2), "unresolved equals");
        check(a1.hashCode() == a2.hashCode(), "unresolved hashCode");
        InetSocketAddress a3 = InetSocketAddress.createUnresolved("h", 2);
        check(!a1.equals(a3), "unresolved different port not equal");
        check(!a1.equals(InetSocketAddress.createUnresolved("other", 1)), "unresolved different host not equal");

        // toString forms
        check(wild.toString().endsWith(":9090"), "wildcard toString port");
        check(wild.toString().startsWith(wild.getAddress().getHostAddress() + "/"), "wildcard toString host prefix");
        check(un.toString().equals("foo.example/<unresolved>:1234"), "unresolved toString");

        // port range validation
        fails(IllegalArgumentException.class, "port -1", () -> new InetSocketAddress(-1));
        fails(IllegalArgumentException.class, "port 65536", () -> new InetSocketAddress(65536));
        fails(IllegalArgumentException.class, "null host", () -> new InetSocketAddress((String) null, 80));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
