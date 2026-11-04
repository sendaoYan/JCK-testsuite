/*
 * @test
 * @summary java.net.Proxy conformance: white-box structure + black-box behavior.
 * @run main ProxyTest
 */

import java.lang.reflect.Modifier;
import java.net.*;

public class ProxyTest {
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
        Class<?> c = Class.forName("java.net.Proxy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getField("NO_PROXY") != null, "NO_PROXY");
        check(c.getConstructor(Proxy.Type.class, SocketAddress.class) != null, "ctor(Type, SocketAddress)");
        check(c.getMethod("type") != null, "type()");
        check(c.getMethod("address") != null, "address()");
        Class<?> t = Class.forName("java.net.Proxy$Type");
        check(Modifier.isPublic(t.getModifiers()) && t.isEnum(), "Type enum");
    }

    static void testBlackBox() throws Exception {
        Proxy direct = Proxy.NO_PROXY;
        check(direct.type() == Proxy.Type.DIRECT, "NO_PROXY type DIRECT");
        check(direct.address() == null, "NO_PROXY null address");
        check(direct.toString().equals("DIRECT"), "DIRECT toString");

        InetSocketAddress addr = new InetSocketAddress("localhost", 3128);
        Proxy http = new Proxy(Proxy.Type.HTTP, addr);
        check(http.type() == Proxy.Type.HTTP, "HTTP type");
        check(http.address().equals(addr), "HTTP address");
        check(http.toString().startsWith("HTTP @ "), "HTTP toString prefix");
        check(http.toString().endsWith(":3128"), "HTTP toString port");

        Proxy http2 = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 3128));
        check(http.equals(http2), "equals");
        check(http.hashCode() == http2.hashCode(), "hashCode");
        check(!http.equals(new Proxy(Proxy.Type.SOCKS, addr)), "different type not equal");
        check(!http.equals(direct), "different proxy not equal");

        Proxy.Type[] types = Proxy.Type.values();
        check(types.length == 3, "3 proxy types");
        check(Proxy.Type.valueOf("DIRECT") == Proxy.Type.DIRECT, "valueOf DIRECT");
        check(Proxy.Type.valueOf("HTTP") == Proxy.Type.HTTP, "valueOf HTTP");
        check(Proxy.Type.valueOf("SOCKS") == Proxy.Type.SOCKS, "valueOf SOCKS");

        fails(IllegalArgumentException.class, "DIRECT with address", () -> new Proxy(Proxy.Type.DIRECT, addr));
        fails(IllegalArgumentException.class, "null type", () -> new Proxy(null, null));
        fails(IllegalArgumentException.class, "valueOf bogus", () -> Proxy.Type.valueOf("BOGUS"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
