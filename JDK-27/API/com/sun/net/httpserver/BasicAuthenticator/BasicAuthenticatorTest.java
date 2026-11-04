/*
 * @test
 * @summary com.sun.net.httpserver.BasicAuthenticator conformance: white-box structure + black-box behavior.
 * @run main BasicAuthenticatorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BasicAuthenticatorTest {
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
        Class<?> c = load("com.sun.net.httpserver.BasicAuthenticator");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.net.httpserver.Authenticator"), "extends Authenticator");
        Set<String> m = methodNames(c);
        check(m.contains("getRealm"), "getRealm");
        check(m.contains("checkCredentials"), "checkCredentials");
        check(m.contains("authenticate"), "authenticate");
        // two constructors: (String) and (String, Charset)
        check(c.getConstructor(String.class) != null, "(String) constructor");
        check(c.getConstructor(String.class, java.nio.charset.Charset.class) != null, "(String,Charset) constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // direct credential check
        com.sun.net.httpserver.BasicAuthenticator b = new com.sun.net.httpserver.BasicAuthenticator("realm") {
            public boolean checkCredentials(String u, String p) { return "user".equals(u) && "pass".equals(p); }
        };
        check(b.getRealm().equals("realm"), "realm");
        check(b.checkCredentials("user", "pass"), "checkCredentials true");
        check(!b.checkCredentials("user", "wrong"), "checkCredentials false");
        check(!b.checkCredentials("nobody", "pass"), "checkCredentials wrong user");

        // constructor validation
        boolean threw = false;
        try { new com.sun.net.httpserver.BasicAuthenticator("") {
            public boolean checkCredentials(String u, String p) { return true; } }; }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "empty realm throws IAE");
        threw = false;
        try { new com.sun.net.httpserver.BasicAuthenticator(null) {
            public boolean checkCredentials(String u, String p) { return true; } }; }
        catch (NullPointerException e) { threw = true; }
        check(threw, "null realm throws NPE");

        // full HTTP round-trip through the server's authentication filter
        com.sun.net.httpserver.BasicAuthenticator auth = new com.sun.net.httpserver.BasicAuthenticator(
                "realm", StandardCharsets.UTF_8) {
            public boolean checkCredentials(String u, String p) { return "user".equals(u) && "pass".equals(p); }
        };
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        com.sun.net.httpserver.HttpContext ctx = server.createContext("/", ex -> {
            com.sun.net.httpserver.HttpPrincipal pr = ex.getPrincipal();
            byte[] body = (pr == null ? "anonymous" : pr.getName()).getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        ctx.setAuthenticator(auth);
        server.start();
        try {
            int port = server.getAddress().getPort();

            // no credentials -> 401 + WWW-Authenticate challenge
            Resp r0 = request(port, "/", null);
            check(r0.code == 401, "no credentials yields 401");
            check(r0.wwwAuth.equals("Basic realm=\"realm\", charset=\"UTF-8\""),
                  "WWW-Authenticate challenge header");

            // wrong password -> 401
            Resp rBad = request(port, "/", auth("user", "wrong"));
            check(rBad.code == 401, "wrong credentials yield 401");

            // correct credentials -> 200 and authenticated principal
            Resp rOk = request(port, "/", auth("user", "pass"));
            check(rOk.code == 200, "correct credentials yield 200");
            check(rOk.body.equals("realm:user"), "handler sees authenticated principal");
        } finally {
            server.stop(0);
        }
    }

    static String auth(String u, String p) {
        String raw = u + ":" + p;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static final class Resp { final int code; final String body; final String wwwAuth;
        Resp(int c, String b, String w){code=c;body=b;wwwAuth=w;} }

    static Resp request(int port, String path, String authHeader) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        if (authHeader != null) c.setRequestProperty("Authorization", authHeader);
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        byte[] b = in == null ? new byte[0] : in.readAllBytes();
        if (in != null) in.close();
        String w = c.getHeaderField("WWW-Authenticate");
        c.disconnect();
        return new Resp(code, new String(b, StandardCharsets.UTF_8), w);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
