/*
 * @test
 * @summary com.sun.net.httpserver.HttpExchange conformance: white-box structure + black-box behavior.
 * @run main HttpExchangeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpExchangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.HttpExchange");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.net.httpserver.Request"), "implements Request");
        check(interfaces(c).contains("java.lang.AutoCloseable"), "implements AutoCloseable");
        Set<String> m = methodNames(c);
        String[] names = {"getRequestHeaders","getResponseHeaders","getRequestURI","getRequestMethod","getHttpContext","close","getRequestBody","getResponseBody","sendResponseHeaders","getRemoteAddress","getResponseCode","getLocalAddress","getProtocol","getAttribute","setAttribute","setStreams","getPrincipal"};
        for (String n : names) check(m.contains(n), n);

        // JDK-27 response-body length constants
        Field empty = c.getField("RSPBODY_EMPTY");
        Field chunked = c.getField("RSPBODY_CHUNKED");
        check(empty.getLong(null) == -1L, "RSPBODY_EMPTY == -1");
        check(chunked.getLong(null) == 0L, "RSPBODY_CHUNKED == 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Cap cap = new Cap();
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/x", ex -> {
            try {
                cap.method = ex.getRequestMethod();
                cap.path = ex.getRequestURI().getPath();
                cap.proto = ex.getProtocol();
                cap.reqHeadersPresent = ex.getRequestHeaders() != null;
                cap.respHeadersPresent = ex.getResponseHeaders() != null;
                ex.getResponseHeaders().set("X-Cap", "1");
                cap.remotePresent = ex.getRemoteAddress() != null;
                cap.localPresent = ex.getLocalAddress() != null;
                cap.ctxPath = ex.getHttpContext().getPath();
                cap.principalNull = ex.getPrincipal() == null;
                cap.codeBefore = ex.getResponseCode();
                ex.setAttribute("k", "v");
                cap.attrRoundtrip = (String) ex.getAttribute("k");
                cap.reqBodyStream = ex.getRequestBody();
                ex.sendResponseHeaders(200, 0);   // 0 == chunked
                cap.codeAfter = ex.getResponseCode();
                byte[] b = "ok".getBytes(StandardCharsets.UTF_8);
                ex.getResponseBody().write(b);
                ex.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + "/x").openConnection();
            int code = c.getResponseCode();
            byte[] b = c.getInputStream().readAllBytes();
            c.disconnect();
            check(code == 200, "client sees 200");
            check(new String(b, StandardCharsets.UTF_8).equals("ok"), "client sees body");

            check("GET".equals(cap.method), "getRequestMethod");
            check("/x".equals(cap.path), "getRequestURI path");
            check("HTTP/1.1".equals(cap.proto), "getProtocol");
            check(cap.reqHeadersPresent, "getRequestHeaders non-null");
            check(cap.respHeadersPresent, "getResponseHeaders non-null");
            check(cap.remotePresent, "getRemoteAddress non-null");
            check(cap.localPresent, "getLocalAddress non-null");
            check("/x".equals(cap.ctxPath), "getHttpContext path");
            check(cap.principalNull, "getPrincipal null without authenticator");
            check(cap.codeBefore == -1, "getResponseCode -1 before send");
            check(cap.codeAfter == 200, "getResponseCode 200 after send");
            check("v".equals(cap.attrRoundtrip), "setAttribute/getAttribute round-trip");
            check(cap.reqBodyStream != null, "getRequestBody non-null");
        } finally {
            server.stop(0);
        }
    }

    static final class Cap {
        volatile String method, path, proto, ctxPath, attrRoundtrip;
        volatile boolean reqHeadersPresent, respHeadersPresent, remotePresent, localPresent, principalNull;
        volatile int codeBefore, codeAfter;
        volatile InputStream reqBodyStream;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
