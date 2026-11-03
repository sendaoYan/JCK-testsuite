/*
 * @test
 * @summary java.net.URI conformance: white-box structure + black-box behavior.
 * @run main URITest
 */

import java.lang.reflect.*;
import java.net.*;

public class URITest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URI");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class, String.class, int.class, String.class, String.class, String.class) != null, "ctor(7 args)");
        check(c.getMethod("create", String.class) != null, "create");
        check(c.getMethod("resolve", String.class) != null, "resolve(String)");
        check(c.getMethod("normalize") != null, "normalize");
        check(c.getMethod("toASCIIString") != null, "toASCIIString");
        check(c.getMethod("compareTo", URI.class) != null, "compareTo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        URI u = URI.create("http://user@example.com:8080/a/b?x=1#frag");
        check(u.getScheme().equals("http"), "scheme");
        check(u.getHost().equals("example.com"), "host");
        check(u.getPort() == 8080, "port");
        check(u.getPath().equals("/a/b"), "path");
        check(u.getQuery().equals("x=1"), "query");
        check(u.getFragment().equals("frag"), "fragment");
        check(u.getUserInfo().equals("user"), "userInfo");
        check(u.getAuthority().equals("user@example.com:8080"), "authority");
        check(u.isAbsolute(), "isAbsolute");
        check(!u.isOpaque(), "not opaque");

        // 7-arg component constructor composes the same string
        URI built = new URI("http", "user", "example.com", 8080, "/a/b", "x=1", "frag");
        check(built.toString().equals("http://user@example.com:8080/a/b?x=1#frag"), "7-arg build");
        check(built.equals(u), "equals of built and parsed");
        check(built.hashCode() == u.hashCode(), "hashCode equal");

        // opaque URI (scheme + ssp + fragment)
        URI mail = new URI("mailto", "java-net@example.com", null);
        check(mail.isOpaque(), "mailto opaque");
        check(mail.getSchemeSpecificPart().equals("java-net@example.com"), "opaque ssp");
        check(mail.getScheme().equals("mailto"), "opaque scheme");
        check(mail.isAbsolute(), "mailto absolute (has scheme)");

        // parseServerAuthority on server-based URI
        check(u.parseServerAuthority().equals(u), "parseServerAuthority");

        // normalize removes dot segments
        check(URI.create("http://a/b/../c/./d").normalize().toString().equals("http://a/c/d"), "normalize");

        // resolve against base
        check(URI.create("http://a/b/c/d;p?q").resolve("../g").toString().equals("http://a/b/g"), "resolve ../");
        check(u.resolve("/c").toString().equals("http://user@example.com:8080/c"), "resolve absolute path");

        // relativize
        check(URI.create("a/b").relativize(URI.create("a/b/c")).toString().equals("c"), "relativize");

        // toURL roundtrip
        check(u.toURL().toString().equals("http://user@example.com:8080/a/b?x=1#frag"), "toURL");

        // ASCII encoding of non-ASCII path
        check(new URI("http://example.com/ü").toASCIIString().equals("http://example.com/%C3%BC"), "toASCIIString");

        // equals/hashCode/compareTo ignore case in scheme and host
        URI v = URI.create("HTTP://EXAMPLE.COM/p");
        check(v.equals(URI.create("http://example.com/p")), "equals case-insensitive host");
        check(v.compareTo(URI.create("http://example.com/p")) == 0, "compareTo case-insensitive");
        check(URI.create("a").compareTo(URI.create("b")) < 0, "compareTo ordering");

        // getRaw* preserve percent-encoding, decoded get* do not
        URI enc = URI.create("http://example.com/a%20b");
        check(enc.getRawPath().equals("/a%20b"), "rawPath");
        check(enc.getPath().equals("/a b"), "decoded path");

        // exceptions
        fails(URISyntaxException.class, "scheme starts with digit", () -> new URI("1http://x/"));
        fails(URISyntaxException.class, "space in host", () -> new URI("http://exa mple/"));
        fails(IllegalArgumentException.class, "create invalid", () -> URI.create("http://exa mple/"));
        fails(NullPointerException.class, "create null", () -> URI.create(null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
