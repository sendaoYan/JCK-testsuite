/*
 * @test
 * @summary com.sun.net.httpserver.spi.HttpServerProvider conformance: white-box structure + black-box behavior.
 * @run main HttpServerProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;

public class HttpServerProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.spi.HttpServerProvider");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        check(m.contains("provider"), "provider");
        check(m.contains("createHttpServer"), "createHttpServer");
        check(m.contains("createHttpsServer"), "createHttpsServer");
        check(c.getConstructors().length == 0, "no public constructor (protected)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.net.httpserver.spi.HttpServerProvider p =
                com.sun.net.httpserver.spi.HttpServerProvider.provider();
        check(p != null, "provider non-null");
        check(p == com.sun.net.httpserver.spi.HttpServerProvider.provider(),
              "provider() returns same instance on repeat calls");

        // default provider creates a working HttpServer
        com.sun.net.httpserver.HttpServer s = p.createHttpServer(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            check(s != null, "createHttpServer non-null");
            check(s.getAddress().getPort() > 0, "createHttpServer ephemeral port");
        } finally {
            s.stop(0);
        }

        // default provider creates a working HttpsServer (bound, no TLS handshake)
        com.sun.net.httpserver.HttpsServer hs = p.createHttpsServer(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            check(hs != null, "createHttpsServer non-null");
            check(hs.getAddress().getPort() > 0, "createHttpsServer ephemeral port");
        } finally {
            hs.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
