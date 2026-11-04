/*
 * @test
 * @summary java.net.URL conformance: white-box structure + black-box behavior.
 * @run main URLTest
 */

import java.lang.reflect.*;
import java.net.*;

public class URLTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URL");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(URL.class, String.class) != null, "ctor(URL, String)");
        check(c.getMethod("getProtocol") != null, "getProtocol");
        check(c.getMethod("openConnection") != null, "openConnection");
        check(c.getMethod("toURI") != null, "toURI");
        check(c.getMethod("of", URI.class, URLStreamHandler.class) != null, "of");
    }

    static void testBlackBox() throws Exception {
        URL u = new URL("http://user:pw@example.com:8080/a/b?x=1#f");
        check(u.getProtocol().equals("http"), "protocol");
        check(u.getHost().equals("example.com"), "host");
        check(u.getPort() == 8080, "port");
        check(u.getDefaultPort() == 80, "defaultPort");
        check(u.getPath().equals("/a/b"), "path");
        check(u.getQuery().equals("x=1"), "query");
        check(u.getRef().equals("f"), "ref");
        check(u.getUserInfo().equals("user:pw"), "userInfo");
        check(u.getAuthority().equals("user:pw@example.com:8080"), "authority");
        check(u.getFile().equals("/a/b?x=1"), "file");

        // port defaults to -1 when not specified
        URL noPort = new URL("http://example.com/");
        check(noPort.getPort() == -1, "port -1 default");
        check(noPort.getDefaultPort() == 80, "http default 80");

        // multi-arg constructors
        URL c1 = new URL("http", "example.com", "/x");
        check(c1.toString().equals("http://example.com/x"), "ctor(protocol,host,file)");
        URL c2 = new URL("http", "example.com", 8080, "/x");
        check(c2.toString().equals("http://example.com:8080/x"), "ctor(protocol,host,port,file)");

        // relative URL construction from a base
        URL rel = new URL(u, "../other");
        check(rel.toString().equals("http://user:pw@example.com:8080/other"), "relative resolve");

        // equals / sameFile
        check(u.equals(new URL("http://user:pw@example.com:8080/a/b?x=1#f")), "equals");
        check(u.hashCode() == new URL("http://user:pw@example.com:8080/a/b?x=1#f").hashCode(), "hashCode");
        check(u.sameFile(new URL("http://user:pw@example.com:8080/a/b?x=1#g")), "sameFile");
        check(!u.sameFile(new URL("http://user:pw@example.com:8080/a/b?y=2")), "not sameFile different query");

        // toExternalForm / toString / toURI
        check(u.toExternalForm().equals("http://user:pw@example.com:8080/a/b?x=1#f"), "toExternalForm");
        check(u.toString().equals(u.toExternalForm()), "toString==toExternalForm");
        check(u.toURI().equals(URI.create("http://user:pw@example.com:8080/a/b?x=1#f")), "toURI");

        // of(URI) requires absolute, hierarchical
        URL ofU = URL.of(URI.create("http://example.com/a"), null);
        check(ofU.toString().equals("http://example.com/a"), "URL.of");

        // exceptions
        fails(MalformedURLException.class, "unknown protocol", () -> new URL("nosuchproto://x/"));
        fails(MalformedURLException.class, "non-numeric port", () -> new URL("http://example.com:abc/"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
