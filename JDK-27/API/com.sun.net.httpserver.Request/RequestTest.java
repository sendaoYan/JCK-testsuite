/*
 * @test
 * @summary com.sun.net.httpserver.Request conformance: white-box structure + black-box behavior.
 * @run main RequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class RequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.Request");
        check(c.isInterface(), "interface");
        Set<String> m = methodNames(c);
        check(m.contains("getRequestURI"), "getRequestURI");
        check(m.contains("getRequestMethod"), "getRequestMethod");
        check(m.contains("getRequestHeaders"), "getRequestHeaders");
        Method w = c.getMethod("with", String.class, List.class);
        check(w.isDefault(), "with is a default method");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // anonymous implementation exercises the default method with()
        final com.sun.net.httpserver.Headers base = new com.sun.net.httpserver.Headers();
        base.add("X-A", "1");
        final java.net.URI uri = java.net.URI.create("/path");
        com.sun.net.httpserver.Request req = new com.sun.net.httpserver.Request() {
            public java.net.URI getRequestURI() { return uri; }
            public String getRequestMethod() { return "GET"; }
            public com.sun.net.httpserver.Headers getRequestHeaders() { return base; }
        };

        com.sun.net.httpserver.Request adapted = req.with("X-B", Arrays.asList("2", "3"));
        check(adapted != null, "with returns non-null");
        check(adapted.getRequestURI() == uri, "with preserves URI");
        check(adapted.getRequestMethod().equals("GET"), "with preserves method");
        check(adapted.getRequestHeaders().getFirst("X-B").equals("2"), "with adds new header");
        check(adapted.getRequestHeaders().get("X-B").size() == 2, "with keeps all values");
        check(adapted.getRequestHeaders().getFirst("X-A").equals("1"), "with keeps original headers");

        // with() does not replace an existing header of the same name
        com.sun.net.httpserver.Request notReplaced = adapted.with("X-A", Arrays.asList("9"));
        check(notReplaced.getRequestHeaders().getFirst("X-A").equals("1"), "with does not replace existing header");

        // null rejection
        boolean threw = false;
        try { req.with(null, Arrays.asList("x")); } catch (NullPointerException e) { threw = true; }
        check(threw, "with null name throws NPE");
        threw = false;
        try { req.with("X", null); } catch (NullPointerException e) { threw = true; }
        check(threw, "with null values throws NPE");

        // real exchange also satisfies Request
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        final String[] observed = new String[1];
        server.createContext("/r", ex -> {
            com.sun.net.httpserver.Request r = ex;
            com.sun.net.httpserver.Request r2 = r.with("X-T", Arrays.asList("tv"));
            observed[0] = r2.getRequestMethod() + "|" + r2.getRequestHeaders().getFirst("X-T");
            byte[] body = observed[0].getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/r").openConnection();
            byte[] b = c.getInputStream().readAllBytes();
            c.disconnect();
            String body = new String(b, StandardCharsets.UTF_8);
            check(body.equals("GET|tv"), "HttpExchange implements Request.with");
        } finally {
            server.stop(0);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
