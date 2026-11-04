/*
 * @test
 * @summary com.sun.net.httpserver.HttpHandlers conformance: white-box structure + black-box behavior.
 * @run main HttpHandlersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HttpHandlersTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpHandlers");
        check(Modifier.isFinal(c.getModifiers()), "final");
        Set<String> m = methodNames(c);
        check(m.contains("of"), "of");
        check(m.contains("handleOrElse"), "handleOrElse");
        // private constructor (no public constructors)
        check(c.getConstructors().length == 0, "no public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // of(): static response handler
        com.sun.net.httpserver.Headers hdr = com.sun.net.httpserver.Headers.of("Content-Type", "text/plain");
        com.sun.net.httpserver.HttpHandler ok = com.sun.net.httpserver.HttpHandlers.of(200, hdr, "body");
        check(ok != null, "of non-null");

        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", ok);
        server.createContext("/empty", com.sun.net.httpserver.HttpHandlers.of(204, new com.sun.net.httpserver.Headers(), ""));
        server.start();
        try {
            int port = server.getAddress().getPort();
            Resp r = request(port, "/ok", "GET");
            check(r.code == 200, "of status 200");
            check(r.body.equals("body"), "of body");
            check("text/plain".equals(r.ct), "of copies Content-Type header");

            // empty body sends no body
            Resp e = request(port, "/empty", "GET");
            check(e.code == 204, "empty body status");
            check(e.body.isEmpty(), "empty body has no content");

            // HEAD: sets Content-Length, sends no body
            Resp hd = request(port, "/ok", "HEAD");
            check(hd.code == 200, "HEAD status");
            check(hd.body.isEmpty(), "HEAD has no body");

            // handleOrElse predicate routing
            com.sun.net.httpserver.HttpHandler yes = com.sun.net.httpserver.HttpHandlers.of(200, new com.sun.net.httpserver.Headers(), "yes");
            com.sun.net.httpserver.HttpHandler no = com.sun.net.httpserver.HttpHandlers.of(405, new com.sun.net.httpserver.Headers(), "no");
            com.sun.net.httpserver.HttpHandler chooser = com.sun.net.httpserver.HttpHandlers.handleOrElse(
                    req -> req.getRequestMethod().equals("GET"), yes, no);
            server.createContext("/choose", chooser);
            Resp g = request(port, "/choose", "GET");
            check(g.code == 200 && g.body.equals("yes"), "handleOrElse GET branch");
            Resp p = request(port, "/choose", "POST");
            check(p.code == 405 && p.body.equals("no"), "handleOrElse fallback branch");
        } finally {
            server.stop(0);
        }

        // of() argument validation
        boolean threw = false;
        try { com.sun.net.httpserver.HttpHandlers.of(99, new com.sun.net.httpserver.Headers(), ""); }
        catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "of status < 100 throws IAE");
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.of(1000, new com.sun.net.httpserver.Headers(), ""); }
        catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "of status > 999 throws IAE");
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.of(200, null, ""); } catch (NullPointerException ex) { threw = true; }
        check(threw, "of null headers throws NPE");
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.of(200, new com.sun.net.httpserver.Headers(), null); } catch (NullPointerException ex) { threw = true; }
        check(threw, "of null body throws NPE");

        // handleOrElse null validation
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.handleOrElse(null, ok, ok); } catch (NullPointerException ex) { threw = true; }
        check(threw, "handleOrElse null predicate throws NPE");
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.handleOrElse(req -> true, null, ok); } catch (NullPointerException ex) { threw = true; }
        check(threw, "handleOrElse null handler throws NPE");
        threw = false;
        try { com.sun.net.httpserver.HttpHandlers.handleOrElse(req -> true, ok, null); } catch (NullPointerException ex) { threw = true; }
        check(threw, "handleOrElse null fallback throws NPE");
    }

    static final class Resp { final int code; final String body; final String ct;
        Resp(int c, String b, String t){code=c;body=b;ct=t;} }

    static Resp request(int port, String path, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        c.setRequestMethod(method);
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        byte[] b = in == null ? new byte[0] : in.readAllBytes();
        if (in != null) in.close();
        String ct = c.getHeaderField("Content-Type");
        c.disconnect();
        return new Resp(code, new String(b, StandardCharsets.UTF_8), ct);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
