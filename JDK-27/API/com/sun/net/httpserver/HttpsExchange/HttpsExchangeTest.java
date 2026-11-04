/*
 * @test
 * @summary com.sun.net.httpserver.HttpsExchange conformance: white-box structure + black-box behavior.
 * @run main HttpsExchangeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class HttpsExchangeTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpsExchange");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.net.httpserver.HttpExchange"), "extends HttpExchange");
        check(methodNames(c).contains("getSSLSession"), "getSSLSession");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Anonymous implementation: verify getSSLSession() is callable (returns null when unset).
        com.sun.net.httpserver.HttpsExchange ex = new com.sun.net.httpserver.HttpsExchange() {
            public SSLSession getSSLSession() { return null; }
            public com.sun.net.httpserver.Headers getRequestHeaders() { return new com.sun.net.httpserver.Headers(); }
            public com.sun.net.httpserver.Headers getResponseHeaders() { return new com.sun.net.httpserver.Headers(); }
            public URI getRequestURI() { return URI.create("/"); }
            public String getRequestMethod() { return "GET"; }
            public com.sun.net.httpserver.HttpContext getHttpContext() { return null; }
            public void close() { }
            public InputStream getRequestBody() { return null; }
            public OutputStream getResponseBody() { return null; }
            public void sendResponseHeaders(int c, long l) { }
            public InetSocketAddress getRemoteAddress() { return null; }
            public int getResponseCode() { return -1; }
            public InetSocketAddress getLocalAddress() { return null; }
            public String getProtocol() { return null; }
            public Object getAttribute(String n) { return null; }
            public void setAttribute(String n, Object v) { }
            public void setStreams(InputStream i, OutputStream o) { }
            public com.sun.net.httpserver.HttpPrincipal getPrincipal() { return null; }
        };
        check(ex.getSSLSession() == null, "anonymous getSSLSession callable (null)");
        check(ex instanceof com.sun.net.httpserver.HttpExchange, "HttpsExchange is an HttpExchange");
        check(ex instanceof com.sun.net.httpserver.Request, "HttpsExchange is a Request");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
