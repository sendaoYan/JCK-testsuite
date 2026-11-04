/*
 * @test
 * @summary java.net.HttpURLConnection conformance: white-box structure + black-box behavior.
 * @run main HttpURLConnectionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;

public class HttpURLConnectionTest {
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

    static HttpURLConnection make() throws Exception {
        return new HttpURLConnection(new URL("http://example.com/")) {
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        };
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpURLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(URLConnection.class.isAssignableFrom(c), "extends URLConnection");
        check(c.getDeclaredConstructor(URL.class) != null, "protected ctor(URL)");
        check(c.getMethod("setRequestMethod", String.class) != null, "setRequestMethod");
        check(c.getMethod("getResponseCode") != null, "getResponseCode");
        check(c.getMethod("disconnect") != null, "disconnect");
        check(c.getMethod("usingProxy") != null, "usingProxy");
    }

    static void testBlackBox() throws Exception {
        HttpURLConnection h = make();
        check(h.getRequestMethod().equals("GET"), "default method GET");
        h.setRequestMethod("POST");
        check(h.getRequestMethod().equals("POST"), "setRequestMethod POST");
        h.setRequestMethod("PUT");
        check(h.getRequestMethod().equals("PUT"), "setRequestMethod PUT");
        fails(ProtocolException.class, "method with space", () -> h.setRequestMethod("foo bar"));

        // HTTP status code constants
        check(HttpURLConnection.HTTP_OK == 200, "HTTP_OK");
        check(HttpURLConnection.HTTP_CREATED == 201, "HTTP_CREATED");
        check(HttpURLConnection.HTTP_NOT_FOUND == 404, "HTTP_NOT_FOUND");
        check(HttpURLConnection.HTTP_MOVED_PERM == 301, "HTTP_MOVED_PERM");
        check(HttpURLConnection.HTTP_MOVED_TEMP == 302, "HTTP_MOVED_TEMP");
        check(HttpURLConnection.HTTP_BAD_REQUEST == 400, "HTTP_BAD_REQUEST");
        check(HttpURLConnection.HTTP_INTERNAL_ERROR == 500, "HTTP_INTERNAL_ERROR");

        // static follow redirects (restore)
        boolean prev = HttpURLConnection.getFollowRedirects();
        HttpURLConnection.setFollowRedirects(!prev);
        check(HttpURLConnection.getFollowRedirects() == !prev, "setFollowRedirects");
        HttpURLConnection.setFollowRedirects(prev);

        // instance follow redirects
        check(h.getInstanceFollowRedirects(), "instance follow redirects default true");
        h.setInstanceFollowRedirects(false);
        check(!h.getInstanceFollowRedirects(), "setInstanceFollowRedirects");

        // streaming modes (on separate instances to avoid mixed-mode errors)
        make().setFixedLengthStreamingMode(100L);
        make().setChunkedStreamingMode(1024);

        // permission and error stream
        java.security.Permission perm = h.getPermission();
        check(perm != null, "getPermission non-null");
        check(h.getErrorStream() == null, "error stream null before connect");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
