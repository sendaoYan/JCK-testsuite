/*
 * @test
 * @summary java.net.URI conformance: white-box structure + full contract coverage.
 *          component decomposition (raw vs decoded), all six constructors +
 *          create(), hierarchical vs opaque URIs, resolve/relativize/normalize,
 *          parseServerAuthority, toURL/toASCIIString, equals/hashCode/compareTo
 *          case rules, null semantics, exact URISyntaxException/IAE/NPE paths,
 *          and serialization round-trip.
 * @run main URITest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;

public class URITest {
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

    static void testSingleStringComponents() throws Exception {
        URI u = new URI("http://user@example.com:8080/a/b?q=1#frag");
        check("http".equals(u.getScheme()), "getScheme");
        check("user".equals(u.getUserInfo()), "getUserInfo");
        check("example.com".equals(u.getHost()), "getHost");
        check(u.getPort() == 8080, "getPort");
        check("/a/b".equals(u.getPath()), "getPath");
        check("q=1".equals(u.getQuery()), "getQuery");
        check("frag".equals(u.getFragment()), "getFragment");
        check("user@example.com:8080".equals(u.getAuthority()), "getAuthority");
        check(u.isAbsolute(), "isAbsolute true");
        check(!u.isOpaque(), "not opaque");
        check("http://user@example.com:8080/a/b?q=1#frag".equals(u.toString()), "toString");
    }

    static void testRawVsDecoded() throws Exception {
        URI u = new URI("http://user%40x@example.com:80/a%2Fb?q%3D1#f%20g");
        check("user%40x".equals(u.getRawUserInfo()), "rawUserInfo");
        check("user@x".equals(u.getUserInfo()), "decoded userInfo");
        check("/a%2Fb".equals(u.getRawPath()), "rawPath");
        check("/a/b".equals(u.getPath()), "decoded path");
        check("q%3D1".equals(u.getRawQuery()), "rawQuery");
        check("q=1".equals(u.getQuery()), "decoded query");
        check("f%20g".equals(u.getRawFragment()), "rawFragment");
        check("f g".equals(u.getFragment()), "decoded fragment");
        check("user%40x@example.com:80".equals(u.getRawAuthority()), "rawAuthority");
        check("user@x@example.com:80".equals(u.getAuthority()), "decoded authority");
        check("//user%40x@example.com:80/a%2Fb?q%3D1".equals(u.getRawSchemeSpecificPart()), "rawSsp");
        check("//user@x@example.com:80/a/b?q=1".equals(u.getSchemeSpecificPart()), "decoded ssp");
    }

    static void testNonAsciiAndAscii() throws Exception {
        URI u = new URI("http://example.com/ä");
        check("/ä".equals(u.getPath()), "non-ascii decoded path");
        check("/ä".equals(u.getRawPath()), "non-ascii raw path preserves unicode");
        check("http://example.com/ä".equals(u.toString()), "toString preserves unicode");
        check("http://example.com/%C3%A4".equals(u.toASCIIString()), "toASCIIString percent-encodes");
    }

    static void testConstructorValidation() {
        checkThrows(URISyntaxException.class, () -> new URI("1http://x/"), "scheme starts with digit");
        checkThrows(URISyntaxException.class, () -> new URI("http://exa mple.com/x"), "space in authority");
        checkThrows(URISyntaxException.class, () -> new URI("http://example.com/bad space"), "space in path");
        checkThrows(NullPointerException.class, () -> new URI((String) null), "single-string null");
        checkThrows(IllegalArgumentException.class, () -> URI.create("http://exa mple.com/x"), "create wraps URISyntaxException");
        checkThrows(NullPointerException.class, () -> URI.create(null), "create null");
    }

    static void testMultiArgConstructors() throws Exception {
        URI opaque = new URI("mailto", "java-net@example.com", null);
        check(opaque.isOpaque(), "3-arg opaque");
        check(opaque.isAbsolute(), "3-arg absolute");
        check("mailto".equals(opaque.getScheme()), "3-arg scheme");
        check("java-net@example.com".equals(opaque.getSchemeSpecificPart()), "3-arg ssp");
        check(null == opaque.getHost(), "opaque has no host");
        check(null == opaque.getPath(), "opaque has no path");

        URI four = new URI("http", "example.com", "/a", "f");
        check("http".equals(four.getScheme()), "4-arg scheme");
        check("example.com".equals(four.getHost()), "4-arg host");
        check("/a".equals(four.getPath()), "4-arg path");
        check("f".equals(four.getFragment()), "4-arg fragment");
        check("http://example.com/a#f".equals(four.toString()), "4-arg toString");

        URI five = new URI("http", "user@example.com:80", "/a", "q", "f");
        check("user".equals(five.getUserInfo()), "5-arg userInfo");
        check("example.com".equals(five.getHost()), "5-arg host");
        check(five.getPort() == 80, "5-arg port");
        check("http://user@example.com:80/a?q#f".equals(five.toString()), "5-arg toString");

        URI seven = new URI("http", "user", "example.com", 8080, "/a/b", "x=1", "frag");
        check("http://user@example.com:8080/a/b?x=1#frag".equals(seven.toString()), "7-arg build");
        URI noport = new URI("http", "u", "h", -1, "/p", "q", "f");
        check(noport.getPort() == -1, "7-arg port -1 undefined");
        check("http://u@h/p?q#f".equals(noport.toString()), "7-arg -1 omits port");
        URI zeroport = new URI("http", "u", "h", 0, "/p", "q", "f");
        check(zeroport.getPort() == 0, "7-arg port 0");
        check("http://u@h:0/p?q#f".equals(zeroport.toString()), "7-arg port 0 string");
        checkThrows(URISyntaxException.class, () -> new URI("http", "u", "h", -2, "/p", "q", "f"), "7-arg port < -1");
    }

    static void testNullSemantics() throws Exception {
        URI base = new URI("http://a/b/c");
        checkThrows(NullPointerException.class, () -> base.resolve((URI) null), "resolve(URI null)");
        checkThrows(NullPointerException.class, () -> base.relativize((URI) null), "relativize null");
        checkThrows(NullPointerException.class, () -> base.resolve((String) null), "resolve(String null)");
        check("http://a/b/d".equals(base.resolve("d").toString()), "resolve(String) works");
    }

    static void testResolveRelativizeNormalize() throws Exception {
        URI base = new URI("http://a/b/c/d;p?q");
        check("http://a/b/c/g".equals(base.resolve("g").toString()), "resolve relative");
        check("http://a/g".equals(base.resolve("/g").toString()), "resolve absolute path");
        check("http://a/b/c/?y".equals(base.resolve("?y").toString()), "resolve query only");
        check("http://a/b/c/g#s".equals(base.resolve("g#s").toString()), "resolve with fragment");

        check("http://example.com/a/c/d".equals(new URI("http://example.com/a/b/../c/./d").normalize().toString()), "normalize dot segments");

        URI doc = new URI("http://a/b/c/d;p?q");
        check("?q#f".equals(doc.relativize(new URI("http://a/b/c/d;p?q#f")).toString()), "relativize fragment-only");
        check("http://a/b/c/e".equals(doc.relativize(new URI("http://a/b/c/e")).toString()), "relativize different authority path");
        check("c".equals(new URI("a/b").relativize(new URI("a/b/c")).toString()), "relativize nested");
    }

    static void testEqualsHashCodeCompareTo() throws Exception {
        URI u = new URI("http://example.com/a");
        check(u.equals(new URI("http://EXAMPLE.com/a")), "equals host case-insensitive");
        check(u.equals(new URI("HTTP://example.com/a")), "equals scheme case-insensitive");
        check(!u.equals(new URI("http://example.com/a#f")), "equals fragment matters");
        check(!u.equals(null), "equals null false");
        check(!u.equals("http://example.com/a"), "equals non-URI false");
        check(u.hashCode() == new URI("http://EXAMPLE.com/a").hashCode(), "hashCode host case-insensitive");
        check(u.compareTo(new URI("http://example.com/a")) == 0, "compareTo equal");
        check(new URI("http://a/b").compareTo(new URI("http://a/c")) < 0, "compareTo ordering");
        check(new URI("http://a/c").compareTo(new URI("http://a/b")) > 0, "compareTo reverse");
        check(u.compareTo(u) == 0, "compareTo self");
    }

    static void testToURLAndParseServerAuthority() throws Exception {
        URI u = new URI("http://example.com/a");
        check("http://example.com/a".equals(u.toURL().toString()), "toURL");
        checkThrows(IllegalArgumentException.class, () -> new URI("/a/b").toURL(), "toURL relative -> IAE");
        URI server = new URI("http://user@example.com:80/x");
        check(server.parseServerAuthority().equals(server), "parseServerAuthority idempotent");
        check("example.com".equals(server.parseServerAuthority().getHost()), "parseServerAuthority host");
    }

    static void testSerialization() throws Exception {
        URI u = new URI("http://user@example.com:8080/a%20b?q=1#f");
        URI u2 = roundTrip(u);
        check(u.equals(u2), "serialization equals");
        check(u.toString().equals(u2.toString()), "serialization toString");
        check("/a b".equals(u2.getPath()), "serialized decoded path");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleStringComponents();
        testRawVsDecoded();
        testNonAsciiAndAscii();
        testConstructorValidation();
        testMultiArgConstructors();
        testNullSemantics();
        testResolveRelativizeNormalize();
        testEqualsHashCodeCompareTo();
        testToURLAndParseServerAuthority();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
