/*
 * @test
 * @summary java.net.URLStreamHandler conformance: white-box structure + black-box behavior.
 *          Deep contract coverage: default parseURL decomposition via URL(null, spec, handler)
 *          (protocol/authority/userInfo/host/port/file/path/query/ref), relative and absolute
 *          context resolution, toExternalForm reconstruction, equals/hashCode/sameFile with
 *          literal-IP hosts (no DNS), getHostAddress/hostsEqual, default-port default and
 *          override, openConnection(URL, Proxy) default rejection, setURL (8-arg and
 *          deprecated 5-arg) field mutation, protected-member visibility.
          (Java 8 source level.)
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

    /** Handler exposing every protected member for testing; default parseURL kept. */
    static class H extends URLStreamHandler {
        protected URLConnection openConnection(URL u) { return null; }
        URLConnection open(URL u) { return openConnection(u); }
        URLConnection openProxy(URL u, java.net.Proxy p) throws IOException { return super.openConnection(u, p); }
        void doParse(URL u, String spec, int s, int e) { parseURL(u, spec, s, e); }
        void doSet8(URL u, String pr, String host, int port, String auth, String ui, String path, String q, String ref) {
            setURL(u, pr, host, port, auth, ui, path, q, ref);
        }
        @SuppressWarnings("deprecation")
        void doSet5(URL u, String pr, String host, int port, String file, String ref) {
            setURL(u, pr, host, port, file, ref);
        }
        String doExt(URL u) { return toExternalForm(u); }
        boolean doEq(URL a, URL b) { return equals(a, b); }
        int doHash(URL u) { return hashCode(u); }
        boolean doSame(URL a, URL b) { return sameFile(a, b); }
        InetAddress doAddr(URL u) { return getHostAddress(u); }
        boolean doHostsEq(URL a, URL b) { return hostsEqual(a, b); }
        int doDefPort() { return getDefaultPort(); }
    }

    /** Handler overriding getDefaultPort to 99. */
    static class H99 extends URLStreamHandler {
        protected URLConnection openConnection(URL u) { return null; }
        protected int getDefaultPort() { return 99; }
    }

    // ------------------------------------------------------------------
    // original structure checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginalStructure() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandler");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLStreamHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getConstructor() != null, "no-arg ctor");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "ctor public");
        check(Modifier.isProtected(c.getDeclaredMethod("openConnection", URL.class).getModifiers()), "openConnection protected");
        check(Modifier.isAbstract(c.getDeclaredMethod("openConnection", URL.class).getModifiers()), "openConnection abstract");
        check(Modifier.isProtected(c.getDeclaredMethod("openConnection", URL.class, java.net.Proxy.class).getModifiers()),
                "openConnection(URL,Proxy) protected");
        check(Modifier.isProtected(c.getDeclaredMethod("parseURL", URL.class, String.class, int.class, int.class).getModifiers()),
                "parseURL protected");
        check(Modifier.isProtected(c.getDeclaredMethod("getDefaultPort").getModifiers()), "getDefaultPort protected");
        check(Modifier.isProtected(c.getDeclaredMethod("equals", URL.class, URL.class).getModifiers()), "equals protected");
        check(Modifier.isProtected(c.getDeclaredMethod("hashCode", URL.class).getModifiers()), "hashCode protected");
        check(Modifier.isProtected(c.getDeclaredMethod("sameFile", URL.class, URL.class).getModifiers()), "sameFile protected");
        check(Modifier.isProtected(c.getDeclaredMethod("getHostAddress", URL.class).getModifiers()), "getHostAddress protected");
        check(Modifier.isProtected(c.getDeclaredMethod("hostsEqual", URL.class, URL.class).getModifiers()), "hostsEqual protected");
        check(Modifier.isProtected(c.getDeclaredMethod("toExternalForm", URL.class).getModifiers()), "toExternalForm protected");
        check(Modifier.isProtected(c.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class,
                String.class, String.class, String.class, String.class, String.class).getModifiers()), "setURL8 protected");
        check(Modifier.isProtected(c.getDeclaredMethod("setURL", URL.class, String.class, String.class, int.class,
                String.class, String.class).getModifiers()), "setURL5 protected deprecated");
        check(!Modifier.isAbstract(c.getDeclaredMethod("getDefaultPort").getModifiers()), "getDefaultPort concrete");
    }

    // ------------------------------------------------------------------
    // default parseURL decomposition (no network: parsing is pure)
    // ------------------------------------------------------------------
    static void testDefaultParseURL() throws Exception {
        H h = new H();
        URL u = new URL(null, "myproto://u:p@h.example:81/a/b?x=1#f", h);
        check("myproto".equals(u.getProtocol()), "parsed protocol");
        check("h.example".equals(u.getHost()), "parsed host");
        check(u.getPort() == 81, "parsed port");
        check("u:p".equals(u.getUserInfo()), "parsed userInfo incl colon");
        check("u:p@h.example:81".equals(u.getAuthority()), "parsed authority");
        check("/a/b?x=1".equals(u.getFile()), "parsed file (path+query)");
        check("/a/b".equals(u.getPath()), "parsed path");
        check("x=1".equals(u.getQuery()), "parsed query");
        check("f".equals(u.getRef()), "parsed ref");
        check(h.doDefPort() == -1, "default getDefaultPort is -1");
        check(u.getDefaultPort() == -1, "URL.getDefaultPort delegates (-1 default)");
        // URL without port
        URL np = new URL(null, "myproto://h.example/a", h);
        check(np.getPort() == -1, "no port -> -1");
        check("h.example".equals(np.getHost()), "no-port host");
        check("/a".equals(np.getPath()), "no-query path");
        // default port override visible through URL.getDefaultPort()
        H99 h99 = new H99();
        URL u99 = new URL(null, "myproto://h.example/a", h99);
        check(u99.getDefaultPort() == 99, "getDefaultPort override honored");
    }

    // ------------------------------------------------------------------
    // relative / absolute context resolution through the same handler
    // ------------------------------------------------------------------
    static void testContextResolution() throws Exception {
        H h = new H();
        URL ctx = new URL(null, "myproto://u@h.example:81/a/b/c?x=1#f", h);
        URL rel = new URL(ctx, "../d?y=2#new", h);
        check("/a/d".equals(rel.getPath()), "relative path resolved, got " + rel.getPath());
        check("y=2".equals(rel.getQuery()), "relative query replaced");
        check("new".equals(rel.getRef()), "relative ref replaced");
        check("h.example".equals(rel.getHost()) && rel.getPort() == 81, "context host/port inherited");
        check("u@h.example:81".equals(rel.getAuthority()), "context authority inherited");
        URL same = new URL(ctx, "c?x=2", h);
        check("/a/b/c".equals(same.getPath()), "sibling resolution");
        URL absPath = new URL(ctx, "/root", h);
        check("/root".equals(absPath.getPath()), "absolute path resets path");
        check("h.example".equals(absPath.getHost()), "absolute path keeps host");
        URL abs = new URL(ctx, "http://other.example/z", h);
        check("http".equals(abs.getProtocol()), "absolute spec overrides protocol");
        check("other.example".equals(abs.getHost()), "absolute spec overrides host");
        check("/z".equals(abs.getPath()), "absolute spec overrides path");
        check(abs.getRef() == null, "absolute spec has no ref");
        URL noScheme = new URL(ctx, "//alt.example/q", h);
        check("alt.example".equals(noScheme.getHost()), "network-path reference replaces authority");
        check("myproto".equals(noScheme.getProtocol()), "network-path keeps protocol");
        // query-only relative spec: directory gets a trailing slash (probed on both JDKs)
        URL queryOnly = new URL(ctx, "?z=9", h);
        check("/a/b/".equals(queryOnly.getPath()), "query-only relative path gets trailing slash");
        check("z=9".equals(queryOnly.getQuery()), "query-only query");
        // fragment-only relative spec: path and query kept, ref replaced
        URL fragOnly = new URL(ctx, "#frag", h);
        check("/a/b/c".equals(fragOnly.getPath()), "fragment-only keeps path");
        check("x=1".equals(fragOnly.getQuery()), "fragment-only keeps query");
        check("frag".equals(fragOnly.getRef()), "fragment-only replaces ref");
    }

    // ------------------------------------------------------------------
    // toExternalForm reconstruction
    // ------------------------------------------------------------------
    static void testToExternalForm() throws Exception {
        H h = new H();
        URL u = new URL(null, "myproto://u:p@h.example:81/a/b?x=1#f", h);
        check("myproto://u:p@h.example:81/a/b?x=1#f".equals(h.doExt(u)), "toExternalForm full reconstruction");
        check(u.toExternalForm().equals(h.doExt(u)), "URL.toExternalForm delegates to handler");
        URL bare = new URL(null, "myproto:///path", h);
        check(bare.toExternalForm().contains("myproto:"), "hostless external form keeps protocol");
        check(bare.getHost() != null && bare.getHost().isEmpty(), "hostless host is empty string");
        URL noRef = new URL(null, "myproto://h.example/p", h);
        check("myproto://h.example/p".equals(noRef.toExternalForm()), "no ref/query form");
    }

    // ------------------------------------------------------------------
    // equals/hashCode/sameFile/hostsEqual with literal-IP hosts (no DNS lookups)
    // ------------------------------------------------------------------
    static void testEquality() throws Exception {
        H h = new H();
        URL a1 = new URL(null, "http://127.0.0.1:80/a", h);
        URL a2 = new URL(null, "http://127.0.0.1:80/a", h);
        check(h.doEq(a1, a2), "equals same components");
        check(h.doHash(a1) == h.doHash(a2), "hashCode consistent with equals");
        check(a1.hashCode() == a2.hashCode(), "URL.hashCode delegates consistently");
        check(!h.doEq(a1, new URL(null, "http://127.0.0.1:80/b", h)), "different path not equal");
        check(!h.doEq(a1, new URL(null, "http://127.0.0.1:81/a", h)), "different port not equal");
        check(!h.doEq(a1, new URL(null, "http://127.0.0.2:80/a", h)), "different host not equal");
        URL withRef = new URL(null, "http://127.0.0.1:80/a#x", h);
        check(!h.doEq(a1, withRef), "different ref not equal (ref participates in equals)");
        check(h.doSame(a1, withRef), "sameFile ignores ref");
        check(h.doSame(a1, a2), "sameFile identical");
        check(!h.doSame(a1, new URL(null, "http://127.0.0.1:80/b", h)), "sameFile different path false");
        // hostsEqual via resolved literal addresses
        check(h.doHostsEq(new URL(null, "http://127.0.0.1/x", h), new URL(null, "http://127.0.0.1:80/y", h)),
                "hostsEqual same literal host");
        check(!h.doHostsEq(new URL(null, "http://127.0.0.1/x", h), new URL(null, "http://127.0.0.2/x", h)),
                "hostsEqual different literal hosts");
        // getHostAddress
        InetAddress addr = h.doAddr(a1);
        check(addr != null && addr.getHostAddress().equals("127.0.0.1"), "getHostAddress literal");
        check(h.doAddr(new URL(null, "myproto:///a", h)) == null, "getHostAddress null host -> null");
    }

    // ------------------------------------------------------------------
    // openConnection defaults
    // ------------------------------------------------------------------
    static void testOpenConnection() throws Exception {
        H h = new H();
        URL u = new URL(null, "myproto://h.example/a", h);
        check(h.open(u) == null, "subclass openConnection used verbatim");
        // base openConnection(URL, Proxy) is not implemented and rejects delegation
        try {
            h.openProxy(u, java.net.Proxy.NO_PROXY);
            check(false, "openConnection(URL,Proxy) default should throw UOE");
        } catch (UnsupportedOperationException uoe) {
            check(true, "openConnection(URL,Proxy) default UOE");
        }
        // a handler returning a real connection instance is passed through
        final URLConnection sentinel = new URLConnection(u) {
            public void connect() { connected = true; }
        };
        H hs = new H() {
            protected URLConnection openConnection(URL uu) { return sentinel; }
        };
        check(hs.open(u) == sentinel, "overridden openConnection returns sentinel");
    }

    // ------------------------------------------------------------------
    // setURL: 8-arg and deprecated 5-arg mutate URL fields
    // ------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    static void testSetURL() throws Exception {
        H h = new H();
        URL u5 = new URL(null, "myproto://hh.example:1/f", h);
        h.doSet5(u5, "p5", "h5.example", 7, "/ff?q=1", "r5");
        // probed: the deprecated 5-arg setURL ignores its protocol argument and keeps
        // the existing protocol of the URL
        check("myproto".equals(u5.getProtocol()), "setURL5 keeps existing protocol");
        check("h5.example".equals(u5.getHost()), "setURL5 host");
        check(u5.getPort() == 7, "setURL5 port");
        check("/ff?q=1".equals(u5.getFile()), "setURL5 file");
        check("r5".equals(u5.getRef()), "setURL5 ref");
        check(u5.toExternalForm().contains("myproto://"), "setURL5 external form");
        URL u8 = new URL(null, "myproto://zz.example:1/f", h);
        h.doSet8(u8, "p8", "h8.example", 8, "h8.example:8", "ui8", "/p8", "q8", "r8");
        // setURL deliberately keeps the URL's protocol ("ensure that no one can reset
        // the protocol on a given URL" - URLStreamHandler.setURL source, probed)
        check("myproto".equals(u8.getProtocol()), "setURL8 keeps existing protocol");
        check("h8.example".equals(u8.getHost()), "setURL8 host");
        check(u8.getPort() == 8, "setURL8 port");
        check("h8.example:8".equals(u8.getAuthority()), "setURL8 authority");
        check("ui8".equals(u8.getUserInfo()), "setURL8 userInfo");
        check("/p8".equals(u8.getPath()), "setURL8 path");
        check("q8".equals(u8.getQuery()), "setURL8 query");
        check("r8".equals(u8.getRef()), "setURL8 ref");
        check("/p8?q8".equals(u8.getFile()), "setURL8 file from path+query");
    }

    // ------------------------------------------------------------------
    // original black-box checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testBlackBoxOriginal() throws Exception {
        H h = new H();
        check(h.doDefPort() == -1, "default getDefaultPort is -1");

        URLStreamHandler parser = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) { return null; }
        };
        URL u = new URL(null, "http://example.com:8080/path", parser);
        check(u.getProtocol().equals("http"), "parsed protocol");
        check(u.getHost().equals("example.com"), "parsed host");
        check(u.getPort() == 8080, "parsed port");
        check(u.getPath().equals("/path"), "parsed path");

        final URLConnection[] conn = { null };
        H opener = new H() {
            protected URLConnection openConnection(URL url) { return conn[0]; }
        };
        check(opener.open(u) == null, "openConnection returns null");

        URL a1 = new URL(null, "http://example.com/x", parser);
        URL a2 = new URL(null, "http://example.com/x", parser);
        check(a1.equals(a2), "same handler URLs equal");
        check(a1.sameFile(a2), "sameFile");
    }

    public static void main(String[] args) throws Exception {
        testOriginalStructure();
        testWhiteBox();
        testDefaultParseURL();
        testContextResolution();
        testToExternalForm();
        testEquality();
        testOpenConnection();
        testSetURL();
        testBlackBoxOriginal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
