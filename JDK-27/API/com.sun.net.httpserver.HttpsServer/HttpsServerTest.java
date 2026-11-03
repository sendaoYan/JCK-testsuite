/*
 * @test
 * @summary com.sun.net.httpserver.HttpsServer conformance: white-box structure + black-box behavior.
 * @run main HttpsServerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;

public class HttpsServerTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpsServer");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.net.httpserver.HttpServer"), "extends HttpServer");
        Set<String> m = methodNames(c);
        String[] names = {"create","setHttpsConfigurator","getHttpsConfigurator"};
        for (String n : names) check(m.contains(n), n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // create binds an ephemeral port immediately; release it in finally
        com.sun.net.httpserver.HttpsServer s = com.sun.net.httpserver.HttpsServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            check(s != null, "create non-null");
            check(s.getAddress().getPort() > 0, "ephemeral port > 0");

            // configurator default null; set/get round-trip
            check(s.getHttpsConfigurator() == null, "configurator initially null");
            com.sun.net.httpserver.HttpsConfigurator cfg =
                    new com.sun.net.httpserver.HttpsConfigurator(SSLContext.getDefault());
            s.setHttpsConfigurator(cfg);
            check(s.getHttpsConfigurator() == cfg, "setHttpsConfigurator/getHttpsConfigurator");
            boolean threw = false;
            try { s.setHttpsConfigurator(null); } catch (NullPointerException e) { threw = true; }
            check(threw, "setHttpsConfigurator null throws NPE");
        } finally {
            s.stop(0);
        }

        // create() no-arg returns an unbound server; bind() attaches an address
        com.sun.net.httpserver.HttpsServer s2 = com.sun.net.httpserver.HttpsServer.create();
        try {
            check(s2 != null, "create() non-null");
            check(s2.getAddress() == null, "create() initially unbound");
            s2.bind(new InetSocketAddress("127.0.0.1", 0), 0);
            check(s2.getAddress().getPort() > 0, "bind attaches ephemeral port");
        } finally {
            s2.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
