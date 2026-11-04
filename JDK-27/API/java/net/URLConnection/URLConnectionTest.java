/*
 * @test
 * @summary java.net.URLConnection conformance: white-box structure + black-box behavior.
 * @run main URLConnectionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class URLConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getDeclaredConstructor(URL.class) != null, "protected ctor(URL)");
        check(c.getMethod("connect") != null, "connect");
        check(c.getMethod("getURL") != null, "getURL");
        check(c.getMethod("setConnectTimeout", int.class) != null, "setConnectTimeout");
        check(c.getMethod("setReadTimeout", int.class) != null, "setReadTimeout");
        check(c.getMethod("guessContentTypeFromName", String.class) != null, "guessContentTypeFromName");
    }

    static void testBlackBox() throws Exception {
        URLConnection uc = new URLConnection(new URL("http://example.com/")) {
            public void connect() { connected = true; }
        };
        check(uc.getURL().toString().equals("http://example.com/"), "getURL");

        uc.setConnectTimeout(5000);
        check(uc.getConnectTimeout() == 5000, "connect timeout");
        uc.setReadTimeout(7000);
        check(uc.getReadTimeout() == 7000, "read timeout");

        check(uc.getDoInput(), "doInput default true");
        uc.setDoInput(false);
        check(!uc.getDoInput(), "doInput set");

        check(!uc.getDoOutput(), "doOutput default false");
        uc.setDoOutput(true);
        check(uc.getDoOutput(), "doOutput set");

        check(uc.getUseCaches(), "useCaches default true");
        uc.setUseCaches(false);
        check(!uc.getUseCaches(), "useCaches set");

        uc.setIfModifiedSince(1234567890L);
        check(uc.getIfModifiedSince() == 1234567890L, "ifModifiedSince");

        check(!uc.getAllowUserInteraction(), "allowUserInteraction default false");
        uc.setAllowUserInteraction(true);
        check(uc.getAllowUserInteraction(), "allowUserInteraction set");

        // request properties: set / add / get / getRequestProperties
        uc.setRequestProperty("X-One", "1");
        uc.addRequestProperty("X-Multi", "a");
        uc.addRequestProperty("X-Multi", "b");
        check(uc.getRequestProperty("X-One").equals("1"), "getRequestProperty");
        check(uc.getRequestProperty("X-Multi").equals("b"), "getRequestProperty last of multi");
        Map<String, List<String>> props = uc.getRequestProperties();
        check(props.containsKey("X-One"), "request props contains key");
        check(props.get("X-Multi").size() == 2, "request props multi values");

        // header access before connect returns null / empty
        check(uc.getContentType() == null, "contentType null before connect");
        check(uc.getContentEncoding() == null, "contentEncoding null before connect");

        // static pure functions
        check(URLConnection.guessContentTypeFromName("x.txt").equals("text/plain"), "guess .txt");
        check(URLConnection.guessContentTypeFromName("x.html").equals("text/html"), "guess .html");
        check(URLConnection.guessContentTypeFromName("x.pdf").equals("application/pdf"), "guess .pdf");
        check(URLConnection.guessContentTypeFromName("noextension") == null, "guess unknown");
        check(URLConnection.getFileNameMap() != null, "getFileNameMap non-null");

        // connect() is callable on the concrete subclass without error
        uc.connect();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
