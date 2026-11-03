/*
 * @test
 * @summary com.sun.net.httpserver.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;

public class FilterTest {
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
        Class<?> c = load("com.sun.net.httpserver.Filter");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"doFilter","description","beforeHandler","afterHandler","adaptRequest"};
        for (String n : names) check(m.contains(n), n);
        check(load("com.sun.net.httpserver.Filter$Chain") != null, "nested Chain exists");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // factory descriptions + NPE validation
        com.sun.net.httpserver.Filter before = com.sun.net.httpserver.Filter.beforeHandler("desc-before", ex -> {});
        check(before.description().equals("desc-before"), "beforeHandler description");
        com.sun.net.httpserver.Filter after = com.sun.net.httpserver.Filter.afterHandler("desc-after", ex -> {});
        check(after.description().equals("desc-after"), "afterHandler description");
        com.sun.net.httpserver.Filter adapt = com.sun.net.httpserver.Filter.adaptRequest("desc-adapt", req -> req);
        check(adapt.description().equals("desc-adapt"), "adaptRequest description");
        boolean threw = false;
        try { com.sun.net.httpserver.Filter.beforeHandler(null, ex -> {}); } catch (NullPointerException e) { threw = true; }
        check(threw, "beforeHandler null description throws NPE");
        threw = false;
        try { com.sun.net.httpserver.Filter.beforeHandler("d", null); } catch (NullPointerException e) { threw = true; }
        check(threw, "beforeHandler null operation throws NPE");

        // beforeHandler runs before the exchange handler
        String body1 = runServer("/f1", (ctx) -> {
            ctx.getFilters().add(com.sun.net.httpserver.Filter.beforeHandler("set attr", ex -> ex.setAttribute("probe", "BEFORE")));
            ctx.setHandler(ex -> {
                String v = (String) ex.getAttribute("probe");
                byte[] b = v.getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, b.length);
                ex.getResponseBody().write(b);
                ex.close();
            });
        });
        check(body1.equals("BEFORE"), "beforeHandler sets exchange attribute before handler");

        // afterHandler runs after the handler (observes response code)
        final AtomicInteger observed = new AtomicInteger(-1);
        String body2 = runServer("/f2", (ctx) -> {
            ctx.getFilters().add(com.sun.net.httpserver.Filter.afterHandler("observe", ex -> observed.set(ex.getResponseCode())));
            ctx.setHandler(ex -> {
                byte[] b = "ok".getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(202, b.length);
                ex.getResponseBody().write(b);
                ex.close();
            });
        });
        check(body2.equals("ok"), "afterHandler request body");
        check(observed.get() == 202, "afterHandler observes response code");

        // adaptRequest alters effective request headers
        String body3 = runServer("/f3", (ctx) -> {
            ctx.getFilters().add(com.sun.net.httpserver.Filter.adaptRequest("add header",
                    req -> req.with("X-Foo", java.util.List.of("bar"))));
            ctx.setHandler(ex -> {
                String v = ex.getRequestHeaders().getFirst("X-Foo");
                byte[] b = (v == null ? "null" : v).getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, b.length);
                ex.getResponseBody().write(b);
                ex.close();
            });
        });
        check(body3.equals("bar"), "adaptRequest exposes adapted header to handler");
    }

    interface CtxSetup { void setup(com.sun.net.httpserver.HttpContext ctx); }

    static String runServer(String path, CtxSetup setup) throws Exception {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        setup.setup(server.createContext(path));
        server.start();
        try {
            int port = server.getAddress().getPort();
            HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
            InputStream in = c.getInputStream();
            byte[] b = in.readAllBytes();
            in.close();
            c.disconnect();
            return new String(b, StandardCharsets.UTF_8);
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
