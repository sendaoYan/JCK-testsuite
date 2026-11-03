/*
 * @test
 * @summary com.sun.net.httpserver.HttpContext conformance: white-box structure + black-box behavior.
 * @run main HttpContextTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;

public class HttpContextTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpContext");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"getHandler","setHandler","getPath","getServer","getAttributes","getFilters","setAuthenticator","getAuthenticator"};
        for (String n : names) check(m.contains(n), n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        com.sun.net.httpserver.HttpHandler handler = ex -> {
            try { ex.sendResponseHeaders(200, -1); ex.close(); } catch (java.io.IOException e) { throw new RuntimeException(e); }
        };
        try {
            // context created with a handler exposes path/server/handler
            com.sun.net.httpserver.HttpContext ctx = server.createContext("/ctx", handler);
            check(ctx.getPath().equals("/ctx"), "getPath");
            check(ctx.getServer() == server, "getServer same instance");
            check(ctx.getHandler() == handler, "getHandler returns set handler");

            // attributes map is mutable and holds values
            Map<String, Object> attrs = ctx.getAttributes();
            check(attrs != null, "getAttributes non-null");
            attrs.put("k", "v");
            check("v".equals(attrs.get("k")), "attributes put/get");
            check(attrs.containsKey("k"), "attributes containsKey");
            attrs.remove("k");
            check(!attrs.containsKey("k"), "attributes remove");

            // filters list mutable
            List<com.sun.net.httpserver.Filter> filters = ctx.getFilters();
            check(filters != null, "getFilters non-null");
            int initial = filters.size();
            com.sun.net.httpserver.Filter f = com.sun.net.httpserver.Filter.beforeHandler("d", ex -> {});
            filters.add(f);
            check(filters.size() == initial + 1 && filters.contains(f), "filters add");
            filters.remove(f);
            check(filters.size() == initial, "filters remove");

            // authenticator default null, set/get with previous return
            check(ctx.getAuthenticator() == null, "authenticator initially null");
            com.sun.net.httpserver.Authenticator auth = new com.sun.net.httpserver.Authenticator() {
                public com.sun.net.httpserver.Authenticator.Result authenticate(com.sun.net.httpserver.HttpExchange t) {
                    return new com.sun.net.httpserver.Authenticator.Failure(401);
                }
            };
            com.sun.net.httpserver.Authenticator prev = ctx.setAuthenticator(auth);
            check(prev == null, "setAuthenticator returns previous (null)");
            check(ctx.getAuthenticator() == auth, "getAuthenticator returns set");
            check(ctx.setAuthenticator(null) == auth, "setAuthenticator returns previous");
            check(ctx.getAuthenticator() == null, "authenticator cleared");

            // createContext(String) gives null handler until set
            com.sun.net.httpserver.HttpContext c2 = server.createContext("/ctx2");
            check(c2.getHandler() == null, "createContext(path) has null handler");
            c2.setHandler(handler);
            check(c2.getHandler() == handler, "setHandler sets null handler");

            // setHandler validation: null rejected; second set rejected
            boolean threw = false;
            try { c2.setHandler(null); } catch (NullPointerException e) { threw = true; }
            check(threw, "setHandler null throws NPE");
            threw = false;
            try { c2.setHandler(handler); } catch (IllegalArgumentException e) { threw = true; }
            check(threw, "setHandler twice throws IAE");
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
