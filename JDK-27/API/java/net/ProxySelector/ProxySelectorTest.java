/*
 * @test
 * @summary java.net.ProxySelector conformance: white-box structure + black-box behavior.
 * @run main ProxySelectorTest
 */

import java.lang.reflect.Modifier;
import java.net.*;
import java.util.*;

public class ProxySelectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProxySelector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getMethod("getDefault") != null, "getDefault");
        check(c.getMethod("setDefault", ProxySelector.class) != null, "setDefault");
        check(c.getMethod("select", URI.class) != null, "select");
        check(c.getMethod("connectFailed", URI.class, SocketAddress.class, java.io.IOException.class) != null, "connectFailed");
        check(c.getMethod("of", InetSocketAddress.class) != null, "of");
    }

    static void testBlackBox() throws Exception {
        // static factory: single HTTP proxy selector
        InetSocketAddress addr = new InetSocketAddress("localhost", 3128);
        ProxySelector ps = ProxySelector.of(addr);
        List<Proxy> l = ps.select(URI.create("http://example.com/"));
        check(l.size() == 1, "single proxy");
        check(l.get(0).type() == Proxy.Type.HTTP, "HTTP type");
        check(l.get(0).address().equals(addr), "address");
        ps.connectFailed(URI.create("http://example.com/"), addr, new java.io.IOException("boom"));

        // abstract contract via anonymous subclass
        final List<Proxy> fixed = Collections.singletonList(Proxy.NO_PROXY);
        ProxySelector custom = new ProxySelector() {
            public List<Proxy> select(URI uri) { return fixed; }
            public void connectFailed(URI uri, SocketAddress sa, java.io.IOException ioe) { }
        };
        check(custom.select(URI.create("http://x/")) == fixed, "custom select");

        // setDefault/getDefault roundtrip (restore previous)
        ProxySelector prev = ProxySelector.getDefault();
        try {
            ProxySelector.setDefault(custom);
            check(ProxySelector.getDefault() == custom, "setDefault/getDefault roundtrip");
        } finally {
            ProxySelector.setDefault(prev);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
