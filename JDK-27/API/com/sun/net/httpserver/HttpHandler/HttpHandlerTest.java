/*
 * @test
 * @summary com.sun.net.httpserver.HttpHandler conformance: white-box structure + black-box behavior.
 * @run main HttpHandlerTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class HttpHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpHandler");
        check(c.isInterface(), "interface");
        check(methodNames(c).contains("handle"), "handle");
        Method m = c.getMethod("handle", Class.forName("com.sun.net.httpserver.HttpExchange"));
        check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == IOException.class,
              "handle declares IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // HttpHandler is a functional interface (single abstract method)
        com.sun.net.httpserver.HttpHandler h = (ex) -> {};
        check(h instanceof com.sun.net.httpserver.HttpHandler, "lambda instance");

        // real round trip: handler writes request method + body
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/h", ex -> {
            byte[] body = ("handled:" + ex.getRequestMethod()).getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            check(port > 0, "ephemeral port");
            String resp = getBody(port, "/h", "GET");
            check(resp.equals("handled:GET"), "handler invoked and wrote body");
        } finally {
            server.stop(0);
        }
    }

    static String getBody(int port, String path, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        c.setRequestMethod(method);
        int code = c.getResponseCode();
        check(code == 200, "status 200");
        InputStream in = c.getInputStream();
        byte[] b = in.readAllBytes();
        in.close();
        c.disconnect();
        return new String(b, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
