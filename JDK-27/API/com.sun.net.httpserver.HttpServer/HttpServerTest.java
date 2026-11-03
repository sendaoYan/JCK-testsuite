/*
 * @test
 * @summary com.sun.net.httpserver.HttpServer conformance: white-box structure + black-box behavior.
 * @run main HttpServerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class HttpServerTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpServer");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"create","bind","start","stop","createContext","removeContext","getAddress","setExecutor","getExecutor"};
        for (String n : names) check(m.contains(n), n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // create with ephemeral port; bind/start/stop lifecycle
        com.sun.net.httpserver.HttpServer s = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        check(s != null, "create non-null");
        check(s.getAddress().getPort() > 0, "ephemeral port > 0");
        s.stop(0);

        // create() no-arg returns an unbound server; bind() attaches an address
        com.sun.net.httpserver.HttpServer s2 = com.sun.net.httpserver.HttpServer.create();
        check(s2 != null, "create() non-null");
        check(s2.getAddress() == null, "create() initially unbound");
        s2.bind(new InetSocketAddress("127.0.0.1", 0), 0);
        check(s2.getAddress().getPort() > 0, "bind attaches ephemeral port");
        s2.stop(0);

        // full round-trip with contexts
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        com.sun.net.httpserver.HttpHandler h = ex -> {
            byte[] b = ("hello:" + ex.getRequestURI().getPath()).getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        };
        com.sun.net.httpserver.HttpContext ctx = server.createContext("/a", h);
        check(ctx.getPath().equals("/a"), "createContext path");
        check(server.createContext("/b").getHandler() == null, "createContext(path) no handler");
        server.start();
        try {
            int port = server.getAddress().getPort();
            String body = getBody(port, "/a");
            check(body.equals("hello:/a"), "round-trip context a");

            // removing a context makes the path return 404 from the server
            server.removeContext("/b");
            int code = getCode(port, "/b");
            check(code == 404, "removeContext(String) yields 404");

            // removeContext(HttpContext)
            server.removeContext(ctx);
            check(getCode(port, "/a") == 404, "removeContext(HttpContext) yields 404");

            // removing unknown context throws IAE
            boolean threw = false;
            try { server.removeContext("/nope"); } catch (IllegalArgumentException e) { threw = true; }
            check(threw, "removeContext unknown throws IAE");
        } finally {
            server.stop(0);
        }

        // executor set/get
        com.sun.net.httpserver.HttpServer s3 = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            check(s3.getExecutor() == null, "executor initially null");
            s3.setExecutor(exec);
            check(s3.getExecutor() == exec, "setExecutor/getExecutor round-trip");
            s3.setExecutor(null);
            check(s3.getExecutor() == null, "executor can be cleared");
        } finally {
            s3.stop(0);
            exec.shutdownNow();
        }
    }

    static String getBody(int port, String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        InputStream in = c.getInputStream();
        byte[] b = in.readAllBytes();
        in.close();
        c.disconnect();
        return new String(b, StandardCharsets.UTF_8);
    }

    static int getCode(int port, String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        try {
            int code = c.getResponseCode();
            InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
            if (in != null) in.close();
            c.disconnect();
            return code;
        } catch (IOException e) {
            c.disconnect();
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
