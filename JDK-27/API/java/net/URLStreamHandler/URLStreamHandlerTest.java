/*
 * @test
 * @summary java.net.URLStreamHandler conformance: white-box structure + black-box behavior.
 * @run main URLStreamHandlerTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class URLStreamHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    // expose protected members for testing
    static class H extends URLStreamHandler {
        protected URLConnection openConnection(URL u) { return null; }
        int defaultPort() { return getDefaultPort(); }
        URLConnection open(URL u) { return openConnection(u); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getDeclaredMethod("openConnection", URL.class) != null, "openConnection");
        check(c.getDeclaredMethod("parseURL", URL.class, String.class, int.class, int.class) != null, "parseURL");
        check(c.getDeclaredMethod("getDefaultPort") != null, "getDefaultPort");
        check(c.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class, String.class, String.class) != null, "setURL");
    }

    static void testBlackBox() throws Exception {
        H h = new H();
        check(h.defaultPort() == -1, "default getDefaultPort is -1");

        // default parseURL used by URL(String spec) with custom handler
        URLStreamHandler parser = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) { return null; }
        };
        URL u = new URL(null, "http://example.com:8080/path", parser);
        check(u.getProtocol().equals("http"), "parsed protocol");
        check(u.getHost().equals("example.com"), "parsed host");
        check(u.getPort() == 8080, "parsed port");
        check(u.getPath().equals("/path"), "parsed path");

        // openConnection of a custom handler
        final URLConnection[] conn = { null };
        H opener = new H() {
            protected URLConnection openConnection(URL url) { return conn[0]; }
        };
        check(opener.open(u) == null, "openConnection returns null");

        // sameFile / equals default behavior via two URLs with same handler
        URL a1 = new URL(null, "http://example.com/x", parser);
        URL a2 = new URL(null, "http://example.com/x", parser);
        check(a1.equals(a2), "same handler URLs equal");
        check(a1.sameFile(a2), "sameFile");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
