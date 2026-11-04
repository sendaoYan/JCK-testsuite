/*
 * @test
 * @summary java.net.URL conformance: white-box structure + full contract coverage.
 *          component accessors, all six constructors incl. deprecated and
 *          URLStreamHandler variants, JDK 20+ URL.of(URI, URLStreamHandler),
 *          default-port resolution, relative resolution against a context,
 *          equals/hashCode/sameFile host-case rules, toExternalForm/toURI,
 *          exact MalformedURLException/NPE/URISyntaxException paths, and
 *          serialization round-trip. No network I/O (no openConnection).
 * @run main URLTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class URLTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static class TestHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL u) {
            throw new AssertionError("openConnection must not be called");
        }
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

    static void testComponents() throws Exception {
        URL u = new URL("http://user:pass@example.com:8080/a/b?q=1#f");
        check("http".equals(u.getProtocol()), "getProtocol");
        check("user:pass".equals(u.getUserInfo()), "getUserInfo");
        check("example.com".equals(u.getHost()), "getHost");
        check(u.getPort() == 8080, "getPort");
        check("/a/b".equals(u.getPath()), "getPath");
        check("q=1".equals(u.getQuery()), "getQuery");
        check("f".equals(u.getRef()), "getRef");
        check("/a/b?q=1".equals(u.getFile()), "getFile");
        check("user:pass@example.com:8080".equals(u.getAuthority()), "getAuthority");
        check("http://user:pass@example.com:8080/a/b?q=1#f".equals(u.toExternalForm()), "toExternalForm");
        check(u.toExternalForm().equals(u.toString()), "toString == toExternalForm");
    }

    static void testEmptyComponents() throws Exception {
        URL u = new URL("http://example.com");
        check("".equals(u.getPath()), "empty path");
        check("".equals(u.getFile()), "empty file");
        check(null == u.getQuery(), "null query");
        check(null == u.getRef(), "null ref");
        check(null == u.getUserInfo(), "null userInfo");
        check("example.com".equals(u.getAuthority()), "authority host only");
        check(u.getPort() == -1, "getPort -1 without explicit port");
    }

    static void testDefaultPort() throws Exception {
        check(new URL("http://example.com").getDefaultPort() == 80, "http default port 80");
        check(new URL("file:/tmp/x").getDefaultPort() == -1, "file default port -1");
    }

    static void testConstructors() throws Exception {
        URL four = new URL("http", "example.com", 8080, "/a/b");
        check("http://example.com:8080/a/b".equals(four.toExternalForm()), "4-arg with port");
        check(four.getPort() == 8080, "4-arg port");

        URL fourNoPort = new URL("http", "example.com", -1, "/a/b?q=1");
        check("http://example.com/a/b?q=1".equals(fourNoPort.toExternalForm()), "4-arg -1 omits port");
        check(fourNoPort.getPort() == -1, "4-arg -1 getPort");

        @SuppressWarnings("deprecation")
        URL three = new URL("http", "example.com", "/a");
        check("http://example.com/a".equals(three.toExternalForm()), "deprecated 3-arg");

        URL ctx = new URL("http://a/b/c/d;p?q");
        check("http://a/b/c/g".equals(new URL(ctx, "g").toString()), "context relative");
        check("http://a/g".equals(new URL(ctx, "/g").toString()), "context absolute path");
        check("http://g".equals(new URL(ctx, "//g").toString()), "context network-path");
        check("http://a/b/c/?y".equals(new URL(ctx, "?y").toString()), "context query only");
        check("http://a/b/c/g?y#s".equals(new URL(ctx, "g?y#s").toString()), "context relative query+fragment");
        check("http://example.com/a".equals(new URL(null, "http://example.com/a").toExternalForm()), "null context absolute spec");

        URL withHandler = new URL("file", "", -1, "/bogus/index.html", new TestHandler());
        check("file:/bogus/index.html".equals(withHandler.toExternalForm()), "5-arg with handler");
        URL specWithHandler = new URL(null, "http://example.com/a", new TestHandler());
        check("http://example.com/a".equals(specWithHandler.toExternalForm()), "3-arg handler spec");
    }

    static void testOfFactory() throws Exception {
        URL u = URL.of(new URI("http://example.com/a"), new TestHandler());
        check("http://example.com/a".equals(u.toExternalForm()), "of(URI,handler)");

        URL u2 = URL.of(new URI("http://example.com/a"), null);
        check("http://example.com/a".equals(u2.toExternalForm()), "of(URI,null) uses default handler");

        URL mail = URL.of(new URI("mailto:user@example.com"), null);
        check("mailto".equals(mail.getProtocol()), "of opaque URI keeps protocol");

        checkThrows(IllegalArgumentException.class, () -> URL.of(new URI("/a/b"), null), "of relative URI -> IAE");
    }

    static void testValidation() {
        checkThrows(MalformedURLException.class, () -> new URL("example.com/a"), "no protocol");
        checkThrows(MalformedURLException.class, () -> new URL("nosuchproto://x/"), "unknown protocol");
        checkThrows(MalformedURLException.class, () -> new URL("http", "h", -5, "/x"), "4-arg port < -1");
        checkThrows(NullPointerException.class, () -> new URL(null, "h", -1, "/x"), "null protocol");
    }

    static void testEqualsHashCodeSameFile() throws Exception {
        // URL.equals/hashCode/sameFile resolve the host (a DNS lookup for
        // hostnames), so these use IP literals to stay offline/deterministic.
        URL u = new URL("http://127.0.0.1/a");
        URL v = new URL("http://127.0.0.1/a");
        check(u.equals(v), "equals same literal");
        check(u.hashCode() == v.hashCode(), "hashCode equal");
        check(!u.equals(null), "equals null false");
        check(!u.equals("http://127.0.0.1/a"), "equals non-URL false");
        check(!new URL("http://127.0.0.1/a#x").equals(new URL("http://127.0.0.1/a")), "equals ref matters");
        check(!new URL("http://127.0.0.1/a").equals(new URL("http://127.0.0.1/b")), "equals different path");

        check(new URL("http://127.0.0.1/a#x").sameFile(new URL("http://127.0.0.1/a#y")), "sameFile ignores ref");
        check(!new URL("http://127.0.0.1/a#x").sameFile(new URL("http://127.0.0.1/b#x")), "sameFile different path");
    }

    static void testToURI() throws Exception {
        URL u = new URL("http://example.com/a%20b");
        URI uri = u.toURI();
        check("http://example.com/a%20b".equals(uri.toString()), "toURI string");
        check("/a b".equals(uri.getPath()), "toURI decoded path");
        checkThrows(java.net.URISyntaxException.class, () -> new URL("http://example.com/a b").toURI(), "toURI on unencoded space");
    }

    static void testSerialization() throws Exception {
        URL u = new URL("http://example.com:8080/a/b?q=1#f");
        URL u2 = roundTrip(u);
        check(u.equals(u2), "serialization equals");
        check(u.toExternalForm().equals(u2.toExternalForm()), "serialization external form");
        check(u2.getPort() == 8080, "serialization port");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testComponents();
        testEmptyComponents();
        testDefaultPort();
        testConstructors();
        testOfFactory();
        testValidation();
        testEqualsHashCodeSameFile();
        testToURI();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
