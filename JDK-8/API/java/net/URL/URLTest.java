/*
 * @test
 * @summary java.net.URL conformance: full contract coverage. component
 *          accessors (protocol/host/port/file/path/query/ref/userInfo/authority),
 *          all six constructors incl. deprecated and URLStreamHandler variants,
 *          default-port resolution, relative resolution against a context,
 *          port/protocol validation boundaries, IPv6 literal hosts, jar: URLs,
 *          equals/hashCode/sameFile default-port normalization, toExternalForm/toURI,
 *          exact MalformedURLException/NPE/URISyntaxException paths, serialization
 *          round-trip, file: URL real connections against temp files, a custom
 *          URLStreamHandlerFactory registration, and URLConnection state machine
 *          via a handler-provided connection. Network access is limited to
 *          localhost file: URLs only (no sockets to remote hosts).
 * @run main URLTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

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

    // ---------- deepening: port boundaries ----------

    static void testPortBoundaries() throws Exception {
        URL p65535 = new URL("http", "127.0.0.1", 65535, "/p");
        check(p65535.getPort() == 65535, "port 65535 legal");
        check("http://127.0.0.1:65535/p".equals(p65535.toExternalForm()), "port 65535 external form");

        URL p0 = new URL("http", "127.0.0.1", 0, "/p");
        check(p0.getPort() == 0, "port 0 legal 4-arg");
        check("http://127.0.0.1:0/p".equals(p0.toExternalForm()), "port 0 external form");

        URL specBig = new URL("http://127.0.0.1:65536/p");
        check(specBig.getPort() == 65536, "spec port 65536 retained");
        check("http://127.0.0.1:65536/p".equals(specBig.toExternalForm()), "spec port 65536 external form");

        URL big4 = new URL("http", "127.0.0.1", 65536, "/p");
        check(big4.getPort() == 65536, "4-arg port 65536 retained (no range check in constructor)");

        URL spec99999 = new URL("http://127.0.0.1:99999/p");
        check(spec99999.getPort() == 99999, "spec port 99999 retained");

        URL specMinus1 = new URL("http://127.0.0.1:-1/p");
        check(specMinus1.getPort() == -1, "spec port -1 means default");
        check("http://127.0.0.1:-1/p".equals(specMinus1.toExternalForm()), "spec port -1 external form keeps :-1");

        checkThrows(MalformedURLException.class, () -> new URL("http", "h", -2, "/p"), "4-arg port -2");
        checkThrows(MalformedURLException.class, () -> new URL("http://h:-2/p"), "spec port -2");
        try {
            new URL("http", "h", -2, "/p");
        } catch (MalformedURLException e) {
            check("Invalid port number :-2".equals(e.getMessage()), "4-arg port -2 message: " + e.getMessage());
        }
        try {
            new URL("http://h:-2/p");
        } catch (MalformedURLException e) {
            check("Invalid port number :-2".equals(e.getMessage()), "spec port -2 message");
        }
        // non-numeric port in spec
        try {
            new URL("http://h:xx/p");
            check(false, "port xx must throw");
        } catch (MalformedURLException e) {
            check(e.getMessage() != null && e.getMessage().contains("xx"), "port xx message mentions value");
        }
    }

    // ---------- deepening: protocol validation matrix ----------

    static void testProtocolValidationDeep() throws Exception {
        // null protocol -> NPE (4-arg)
        checkThrows(NullPointerException.class, () -> new URL(null, "h", -1, "/x"), "4-arg null protocol NPE");
        // null spec -> MalformedURLException (NOT NPE) on both single-arg and context form
        try {
            new URL((String) null);
            check(false, "null spec must throw");
        } catch (MalformedURLException e) {
            passed++; // type verified; message differs JDK8 ("null") vs JDK27 (helpful NPE text)
        }
        try {
            new URL(new URL("http://h/a"), null);
            check(false, "context null spec must throw");
        } catch (MalformedURLException e) {
            passed++;
        }
        // invalid protocol names via 4-arg ctor -> "unknown protocol: X"
        String[] badProtos = {"ht tp", "1http", ""};
        for (String bp : badProtos) {
            final String fbp = bp;
            try {
                new URL(fbp, "h", -1, "/x");
                check(false, "bad protocol must throw: [" + fbp + "]");
            } catch (MalformedURLException e) {
                check(("unknown protocol: " + fbp).equals(e.getMessage()),
                        "4-arg bad proto message: [" + fbp + "] -> " + e.getMessage());
            }
        }
        // spec forms: digit-first and space are "no protocol", alpha+plus is "unknown protocol"
        String[] noProtocol = {"://h", "1http://h/", "ht tp://h/", "http", "1:2"};
        for (String s : noProtocol) {
            final String fs = s;
            try {
                new URL(fs);
                check(false, "no-protocol spec must throw: [" + fs + "]");
            } catch (MalformedURLException e) {
                check(("no protocol: " + fs).equals(e.getMessage()),
                        "no-protocol message: [" + fs + "] -> " + e.getMessage());
            }
        }
        try {
            new URL("a1+b:c");
            check(false, "a1+b:c must throw");
        } catch (MalformedURLException e) {
            check("unknown protocol: a1+b".equals(e.getMessage()), "a1+b parsed as protocol name");
        }
        try {
            new URL("unknownproto://h/");
            check(false, "unknownproto must throw");
        } catch (MalformedURLException e) {
            check("unknown protocol: unknownproto".equals(e.getMessage()), "unknown proto message");
        }
        // bare "http:" is legal (empty rest)
        URL bare = new URL("http:");
        check("http".equals(bare.getProtocol()), "bare http: protocol");
        check("".equals(bare.getHost()), "bare http: empty host");
        // protocol case is normalized to lowercase
        check("http".equals(new URL("HTTP://example.com/a").getProtocol()), "protocol lowercased");
        check("http".equals(new URL("HTTP://example.com/a").getProtocol()), "protocol case idempotent");
        URL ftpUrl = new URL("FTP://h/x");
        check("ftp".equals(ftpUrl.getProtocol()), "ftp protocol lowercased");
        check(ftpUrl.getDefaultPort() == 21, "ftp default port via lowercased protocol");
    }

    // ---------- deepening: host null / empty / IPv6 literal ----------

    static void testHostForms() throws Exception {
        URL fh = new URL("file", null, "/x");
        check(fh.getHost() == null, "4-arg file null host -> getHost null");
        check("file:/x".equals(fh.toExternalForm()), "file null host external form");

        URL hh = new URL("http", null, -1, "/x");
        check(hh.getHost() == null, "4-arg http null host -> getHost null");
        check("http:/x".equals(hh.toExternalForm()), "http null host external form");
        check(hh.getAuthority() == null, "http null host authority null");

        URL he = new URL("http", "", 8080, "/x");
        check("".equals(he.getHost()), "4-arg empty host");
        check(":8080".equals(he.getAuthority()), "empty host authority is :port");
        check("http://:8080/x".equals(he.toExternalForm()), "empty host external form");

        URL specEmptyHost = new URL("http:///p");
        check("".equals(specEmptyHost.getHost()), "spec triple-slash host empty string");
        check("".equals(specEmptyHost.getAuthority()), "spec triple-slash authority empty");
        check("http:/p".equals(specEmptyHost.toExternalForm()), "spec triple-slash external form");

        URL noSlashFile = new URL("http", null, -1, "q/w");
        check("q/w".equals(noSlashFile.getFile()), "no-slash file preserved");
        check("http:q/w".equals(noSlashFile.toExternalForm()), "no-slash file external form");

        // IPv6 literals
        URL v6 = new URL("http", "[::1]", 80, "/p");
        check("[::1]".equals(v6.getHost()), "IPv6 4-arg host keeps brackets");
        check("[::1]:80".equals(v6.getAuthority()), "IPv6 authority brackets+port");
        check("http://[::1]:80/p".equals(v6.toExternalForm()), "IPv6 external form");

        URL v6s = new URL("http://[::1]:8080/p");
        check("[::1]".equals(v6s.getHost()), "IPv6 spec host");
        check(8080 == v6s.getPort(), "IPv6 spec port");
        check("[::1]:8080".equals(v6s.getAuthority()), "IPv6 spec authority");

        URL v6full = new URL("http", "[2001:db8::1]", -1, "/p");
        check("[2001:db8::1]".equals(v6full.getHost()), "IPv6 full form host");
        check("http://[2001:db8::1]/p".equals(v6full.toExternalForm()), "IPv6 no port external form");

        URL v6eq1 = new URL("http://[::1]/a");
        URL v6eq2 = new URL("http://[0:0:0:0:0:0:0:1]/a");
        // both resolve to the same literal address; equals uses InetAddress
        check(v6eq1.equals(v6eq2), "IPv6 compressed equals expanded (same address, no DNS)");
        check(v6eq1.sameFile(v6eq2), "IPv6 sameFile compressed vs expanded");
    }

    // ---------- deepening: full context-resolution matrix ----------

    static void testContextResolutionMatrix() throws Exception {
        URL ctx = new URL("http://a/b/c/d;p?q");
        // fragment-only: keeps path+query, adds/replaces fragment
        URL frag = new URL(ctx, "#f");
        check("http://a/b/c/d;p?q#f".equals(frag.toExternalForm()), "ctx #f keeps query");
        check("f".equals(frag.getRef()), "ctx #f ref");
        check("q".equals(frag.getQuery()), "ctx #f query preserved");
        check("/b/c/d;p".equals(frag.getPath()), "ctx #f path preserved");
        // empty spec copies context (including ref-less original)
        check("http://a/b/c/d;p?q".equals(new URL(ctx, "").toExternalForm()), "ctx empty spec copies");
        // dot segments
        check("http://a/b/g".equals(new URL(ctx, "../g").toExternalForm()), "ctx ../g");
        check("http://a/g".equals(new URL(ctx, "../../g").toExternalForm()), "ctx ../../g");
        check("http://a/../g".equals(new URL(ctx, "../../../g").toExternalForm()), "ctx ../../../g keeps ..");
        check("http://a/b/c/g".equals(new URL(ctx, "./g").toExternalForm()), "ctx ./g");
        check("http://a/b/c/g".equals(new URL(ctx, "g").toExternalForm()), "ctx g sibling");
        check("http://a/b/c/a/b".equals(new URL(ctx, "a/b").toExternalForm()), "ctx a/b appends");
        check("http://a/g".equals(new URL(ctx, "/g").toExternalForm()), "ctx /g absolute path");
        check("http://g".equals(new URL(ctx, "//g").toExternalForm()), "ctx //g network path");
        // query-only: replaces query, path becomes directory
        check("http://a/b/c/?y".equals(new URL(ctx, "?y").toExternalForm()), "ctx ?y");
        check("y".equals(new URL(ctx, "?y").getQuery()), "ctx ?y query");
        check("http://a/b/c/g?y#s".equals(new URL(ctx, "g?y#s").toExternalForm()), "ctx g?y#s");
        // absolute specs override the context entirely
        check("https://x/y".equals(new URL(ctx, "https://x/y").toExternalForm()), "ctx https override");
        check("ftp://x/y".equals(new URL(ctx, "ftp://x/y").toExternalForm()), "ctx ftp override");
        check("mailto:u@x".equals(new URL(ctx, "mailto:u@x").toExternalForm()), "ctx mailto override");
        // directory contexts
        URL dirCtx = new URL("http://a/b/c/d/");
        check("http://a/b/c/d/e".equals(new URL(dirCtx, "e").toExternalForm()), "dir ctx e");
        check("http://a/b/c/f".equals(new URL(dirCtx, "../f").toExternalForm()), "dir ctx ../f");
        // root context: .. beyond root is preserved verbatim
        URL rootCtx = new URL("http://a/");
        check("http://a/..".equals(new URL(rootCtx, "..").toExternalForm()), "root ctx .. preserved");
        check("http://a/".equals(new URL(rootCtx, ".").toExternalForm()), "root ctx . normalized");
        check("http://a/../..".equals(new URL(rootCtx, "../..").toExternalForm()), "root ctx ../.. preserved");
        // context with fragment: fragment is dropped for relative specs
        URL fragCtx = new URL("http://a/b/c#z");
        check("http://a/b/g".equals(new URL(fragCtx, "g").toExternalForm()), "frag ctx g drops ctx ref");
        check("http://a/b/?q".equals(new URL(fragCtx, "?q").toExternalForm()), "frag ctx ?q");
        check(null == new URL(fragCtx, "g").getRef(), "frag ctx g ref null");
        // shallow context
        URL shallow = new URL("http://a/b");
        check("http://a/c".equals(new URL(shallow, "c").toExternalForm()), "shallow ctx c");
        // context with port/userinfo inherited for relative specs
        URL authCtx = new URL("http://u@a:81/b/c");
        URL rel = new URL(authCtx, "d");
        check("http://u@a:81/b/d".equals(rel.toExternalForm()), "ctx inherits userinfo+port");
        check("u".equals(rel.getUserInfo()), "ctx inherits userinfo");
        check(rel.getPort() == 81, "ctx inherits port");
        check("a".equals(rel.getHost()), "ctx inherits host");
        // null context with absolute spec behaves like single-arg
        URL nullCtx = new URL((URL) null, "http://example.com/a");
        check("http://example.com/a".equals(nullCtx.toExternalForm()), "null ctx absolute spec");
        // relative spec with null context -> MUE (no protocol anywhere)
        checkThrows(MalformedURLException.class, () -> new URL((URL) null, "a/b"), "null ctx relative spec");
    }

    // ---------- deepening: 4-arg file with query/fragment split ----------

    static void testFourArgFileQuerySplit() throws Exception {
        URL fwq = new URL("http", "h", 81, "/p?q=1");
        check("/p".equals(fwq.getPath()), "4-arg file with query path");
        check("q=1".equals(fwq.getQuery()), "4-arg file with query query");
        check("/p?q=1".equals(fwq.getFile()), "4-arg file with query file");
        check("http://h:81/p?q=1".equals(fwq.toExternalForm()), "4-arg file with query external");

        URL fwqf = new URL("http", "h", 81, "/p?q=1#f");
        check("/p".equals(fwqf.getPath()), "4-arg file with frag path");
        check("q=1".equals(fwqf.getQuery()), "4-arg file with frag query");
        check("f".equals(fwqf.getRef()), "4-arg file with frag ref");
        check("/p?q=1".equals(fwqf.getFile()), "4-arg file with frag file excludes ref");
        check("http://h:81/p?q=1#f".equals(fwqf.toExternalForm()), "4-arg file with frag external");

        URL fonly = new URL("http", "h", 81, "/p");
        check("/p".equals(fonly.getFile()), "4-arg plain file");
        check(null == fonly.getQuery(), "4-arg plain file query null");
        check(null == fonly.getRef(), "4-arg plain file ref null");

        URL ffrag = new URL("http", "h", 81, "/p#f");
        check("/p".equals(ffrag.getPath()), "4-arg file with only frag path");
        check("f".equals(ffrag.getRef()), "4-arg file with only frag ref");
        check("/p".equals(ffrag.getFile()), "4-arg file with only frag file");
    }

    // ---------- deepening: default ports / jar URLs / userinfo ----------

    static void testDefaultPortsMatrix() throws Exception {
        check(new URL("http://h/").getDefaultPort() == 80, "http 80");
        check(new URL("https://h/").getDefaultPort() == 443, "https 443");
        check(new URL("ftp://h/").getDefaultPort() == 21, "ftp 21");
        check(new URL("file:/x").getDefaultPort() == -1, "file -1");
        check(new URL("jar:file:/x.jar!/y").getDefaultPort() == -1, "jar -1");
        check(new URL("mailto:a@b").getDefaultPort() == -1, "mailto -1");
        check(new URL("http", "h", -1, "/p").getDefaultPort() == 80, "4-arg http default 80");
        check(new URL("https", "h", -1, "/p").getDefaultPort() == 443, "4-arg https default 443");
        check(new URL("ftp", "h", -1, "/p").getDefaultPort() == 21, "4-arg ftp default 21");
    }

    static void testJarUrls() throws Exception {
        URL jar = new URL("jar:file:/x.jar!/y.txt");
        check("jar".equals(jar.getProtocol()), "jar protocol");
        check("file:/x.jar!/y.txt".equals(jar.getFile()), "jar file is inner spec");
        check("file:/x.jar!/y.txt".equals(jar.getPath()), "jar path is inner spec");
        check(null == jar.getAuthority(), "jar authority null");
        check("".equals(jar.getHost()), "jar host empty");
        check("jar:file:/x.jar!/y.txt".equals(jar.toExternalForm()), "jar external form");
        check(-1 == jar.getPort(), "jar port -1");

        URL jarFrag = new URL("jar:file:/x.jar!/y#f");
        check("f".equals(jarFrag.getRef()), "jar ref extracted");
        check("file:/x.jar!/y".equals(jarFrag.getFile()), "jar file excludes ref");

        URL jarQ = new URL("jar:file:/x.jar!/y?q");
        check("file:/x.jar!/y?q".equals(jarQ.getFile()), "jar query kept in file");
        check("q".equals(jarQ.getQuery()), "jar query exposed via getQuery");
        check("file:/x.jar!/y".equals(jarQ.getPath()), "jar path excludes query");
        check(null == jarQ.getRef(), "jar query-only ref null");

        // nested jar URLs are rejected
        try {
            new URL("jar:jar:file:/a.jar!/b.jar!/c");
            check(false, "nested jar must throw");
        } catch (MalformedURLException e) {
            check("Nested JAR URLs are not supported".equals(e.getMessage()), "nested jar message");
        }
    }

    static void testUserInfoAuthority() throws Exception {
        URL ui = new URL("http://u@h:81/p");
        check("u".equals(ui.getUserInfo()), "userinfo without colon");
        check("u@h:81".equals(ui.getAuthority()), "authority includes userinfo");
        URL uiPw = new URL("http://u:pw@h/p");
        check("u:pw".equals(uiPw.getUserInfo()), "userinfo user:pw");
        check("u:pw@h".equals(uiPw.getAuthority()), "authority user:pw@h");
        check(null == uiPw.getRef(), "userinfo url ref null");
        URL uiEmptyUser = new URL("http://:pw@h/p");
        check(":pw".equals(uiEmptyUser.getUserInfo()), "userinfo empty user");
        URL uiEmptyPw = new URL("http://u:@h/p");
        check("u:".equals(uiEmptyPw.getUserInfo()), "userinfo empty password");
        URL uiB64 = new URL("http://dXNlcjpwdw==@h/p");
        check("dXNlcjpwdw==".equals(uiB64.getUserInfo()), "userinfo b64 token untouched");
        // authority without userinfo
        URL noUi = new URL("http://h:81/p");
        check("h:81".equals(noUi.getAuthority()), "authority host:port");
        check(null == noUi.getUserInfo(), "authority no userinfo");
        // userinfo with encoded chars is not decoded
        URL uiEnc = new URL("http://a%40b:c@h/p");
        check("a%40b:c".equals(uiEnc.getUserInfo()), "userinfo percent kept raw");
    }

    // ---------- deepening: equals/hashCode/sameFile default-port normalization ----------

    static void testEqualsHashCodeDeep() throws Exception {
        URL implicit = new URL("http://127.0.0.1/a");
        URL explicit80 = new URL("http://127.0.0.1:80/a");
        check(implicit.equals(explicit80), "equals normalizes default port implicit vs 80");
        check(explicit80.equals(implicit), "equals normalizes default port 80 vs implicit");
        check(implicit.sameFile(explicit80), "sameFile normalizes default port");
        check(explicit80.sameFile(implicit), "sameFile normalizes default port reverse");
        check(implicit.hashCode() == explicit80.hashCode(), "hashCode agrees with port normalization");

        URL explicit8080 = new URL("http://127.0.0.1:8080/a");
        check(!implicit.equals(explicit8080), "equals different explicit port");
        check(!implicit.sameFile(explicit8080), "sameFile different explicit port");
        check(!explicit8080.sameFile(explicit80), "sameFile 8080 vs 80 false");

        // query participates in equals AND sameFile
        URL q1 = new URL("http://127.0.0.1/a?x=1");
        URL q2 = new URL("http://127.0.0.1/a?x=2");
        check(!q1.equals(q2), "equals different query");
        check(!q1.sameFile(q2), "sameFile different query");
        check(q1.equals(new URL("http://127.0.0.1:80/a?x=1")), "equals same query port-normalized");

        // userinfo is NOT part of equals/sameFile comparison
        URL withUser = new URL("http://u@127.0.0.1/a");
        URL withUser2 = new URL("http://v@127.0.0.1/a");
        check(withUser.equals(withUser2), "equals ignores userinfo");
        check(withUser.equals(implicit), "equals ignores userinfo vs none");
        check(withUser.sameFile(implicit), "sameFile ignores userinfo");

        // ref participates in equals but not sameFile
        URL ref1 = new URL("http://127.0.0.1/a#x");
        check(!ref1.equals(implicit), "equals ref matters");
        check(ref1.sameFile(implicit), "sameFile ignores ref");

        // protocol participates
        check(!new URL("http://127.0.0.1/a").equals(new URL("ftp://127.0.0.1/a")), "equals protocol matters");
        check(!new URL("http://127.0.0.1/a").sameFile(new URL("ftp://127.0.0.1/a")), "sameFile protocol matters");

        // equals consistency: equal objects have equal hashCodes, across port forms
        URL a1 = new URL("http://127.0.0.1:80/a?x=1");
        URL a2 = new URL("http://127.0.0.1/a?x=1");
        check(a1.equals(a2) && a1.hashCode() == a2.hashCode(), "hashCode equals contract");

        // equals is reflexive/symmetric on identical literal URLs
        URL b1 = new URL("http://127.0.0.1/p/q");
        URL b2 = new URL("http://127.0.0.1/p/q");
        check(b1.equals(b1), "equals reflexive");
        check(b1.equals(b2) && b2.equals(b1), "equals symmetric");

        // https 443 normalization
        URL hs1 = new URL("https://127.0.0.1/a");
        URL hs2 = new URL("https://127.0.0.1:443/a");
        check(hs1.equals(hs2), "https default port normalization");
        check(hs1.hashCode() == hs2.hashCode(), "https hashCode normalization");
        check(!new URL("https://127.0.0.1:8443/a").equals(hs1), "https non-default port distinct");

        // file: URLs (hostless) equals
        URL f1 = new URL("file:/x/y");
        URL f2 = new URL("file:/x/y");
        URL f3 = new URL("file:/x/z");
        check(f1.equals(f2), "file equals same path");
        check(!f1.equals(f3), "file equals different path");
        check(f1.hashCode() == f2.hashCode(), "file hashCode");

        // sameFile on file: URLs
        check(f1.sameFile(f2), "file sameFile");
        check(!f1.sameFile(f3), "file sameFile different");
    }

    // ---------- deepening: file: URL real connections ----------

    static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[512];
        int n;
        while ((n = is.read(b)) != -1) bos.write(b, 0, n);
        is.close();
        return bos.toByteArray();
    }

    static void testFileUrlConnections() throws Exception {
        // java.io.tmpdir is absolute on every platform ("/tmp" is drive-relative
        // on Windows). The per-run suffix matters when several configurations of
        // this test run at once: a peer JVM rewriting the same fixture changes
        // its lastModified between openConnection() and File.lastModified().
        File dir = new File(System.getProperty("java.io.tmpdir"),
                "net-B-url-" + System.nanoTime() + "/file");
        dir.mkdirs();
        File hello = new File(dir, "hello.txt");
        byte[] helloBytes = "hello world 123".getBytes("UTF-8");
        FileOutputStream fos = new FileOutputStream(hello);
        fos.write(helloBytes);
        fos.close();

        URL u = hello.toURI().toURL();
        check("file".equals(u.getProtocol()), "file url protocol");
        URLConnection c = u.openConnection();
        check(c != null, "connection created");
        check(c.getContentLength() == helloBytes.length, "file getContentLength exact bytes");
        check(c.getContentLengthLong() == (long) helloBytes.length, "file getContentLengthLong");
        check("text/plain".equals(c.getContentType()), "file .txt content type text/plain");
        check(null == c.getContentEncoding(), "file content encoding null");
        check(c.getLastModified() == hello.lastModified(), "file lastModified matches File.lastModified");
        check(c.getLastModified() > 0, "file lastModified positive");

        InputStream is = c.getInputStream();
        check(is != null, "file getInputStream non-null");
        byte[] read = readAll(is);
        check(helloBytes.length == read.length, "file stream length");
        check(new String(read, "UTF-8").equals("hello world 123"), "file stream content");
        // second getInputStream on same connection returns the same stream object
        InputStream is2 = c.getInputStream();
        check(is2 == is, "file second getInputStream same object");
        // openStream reads the whole content
        byte[] viaStream = readAll(u.openStream());
        check(new String(viaStream, "UTF-8").equals("hello world 123"), "openStream content");
        // getContent returns a plain-text content object
        Object content = u.openConnection().getContent();
        check(content != null, "file getContent non-null");
        check(content instanceof InputStream, "file getContent is InputStream");
        check(new String(readAll((InputStream) content), "UTF-8").equals("hello world 123"), "getContent content");

        // unknown extension -> application/octet-stream
        File bin = new File(dir, "x.bin");
        fos = new FileOutputStream(bin);
        fos.write(new byte[]{0, 1, 2});
        fos.close();
        URLConnection cb = bin.toURI().toURL().openConnection();
        check("application/octet-stream".equals(cb.getContentType()), ".bin content type octet-stream");
        check(cb.getContentLength() == 3, ".bin length 3");

        // empty file
        File empty = new File(dir, "empty.txt");
        empty.createNewFile();
        URLConnection ce = empty.toURI().toURL().openConnection();
        check(ce.getContentLength() == 0, "empty file length 0");
        InputStream es = ce.getInputStream();
        check(es.read() == -1, "empty file reads EOF");

        // nonexistent file -> FileNotFoundException from connect/getInputStream
        URL ne = new File(dir, "nope.txt").toURI().toURL();
        try {
            ne.openConnection().getInputStream();
            check(false, "nonexistent must throw");
        } catch (java.io.FileNotFoundException e) {
            passed++;
        }
        try {
            ne.openConnection().connect();
            check(false, "nonexistent connect must throw");
        } catch (java.io.IOException e) {
            check(e instanceof java.io.FileNotFoundException, "nonexistent connect FileNotFoundException");
        }
        // each openConnection gives a fresh connection object
        URLConnection c1 = u.openConnection();
        URLConnection c2 = u.openConnection();
        check(c1 != c2, "openConnection returns fresh instance");

        // cleanup
        hello.delete(); bin.delete(); empty.delete(); dir.delete();
        dir.getParentFile().delete(); // the per-run parent created by mkdirs()
    }

    // ---------- deepening: custom URLStreamHandlerFactory ----------

    static class ProbeFactoryHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL u) {
            throw new UnsupportedOperationException("probe-no-conn");
        }
    }

    static void testStreamHandlerFactory() throws Exception {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if ("bproto".equals(protocol)) return new ProbeFactoryHandler();
                return null; // fall back to default lookup
            }
        });
        // custom protocol now constructs and parses
        URL mu = new URL("bproto://host:1234/a/b?q#f");
        check("bproto".equals(mu.getProtocol()), "factory protocol parsed");
        check("host".equals(mu.getHost()), "factory url host");
        check(mu.getPort() == 1234, "factory url port");
        check("bproto://host:1234/a/b?q#f".equals(mu.toExternalForm()), "factory url external form");
        check(mu.getDefaultPort() == -1, "factory protocol default port -1");
        check("f".equals(mu.getRef()), "factory url ref");
        // openConnection delegates to our handler
        try {
            mu.openConnection();
            check(false, "factory openConnection must delegate");
        } catch (UnsupportedOperationException e) {
            check("probe-no-conn".equals(e.getMessage()), "factory handler reached");
        }
        try {
            mu.openStream();
            check(false, "factory openStream must delegate");
        } catch (UnsupportedOperationException e) {
            passed++;
        } catch (IOException e) {
            check(false, "factory openStream: unexpected " + e);
        }
        try {
            mu.getContent();
            check(false, "factory getContent must delegate");
        } catch (UnsupportedOperationException e) {
            passed++;
        } catch (IOException e) {
            check(false, "factory getContent: unexpected " + e);
        }
        // factory returning null falls back to built-in handlers
        URL std = new URL("http://example.com/");
        check("http".equals(std.getProtocol()), "null-returning factory falls back to http");
        // second set attempt -> Error (factory already defined)
        try {
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
                public URLStreamHandler createURLStreamHandler(String protocol) { return null; }
            });
            check(false, "second setURLStreamHandlerFactory must throw");
        } catch (Throwable t) {
            check(t instanceof Error, "second factory set throws Error: " + t);
            check("factory already defined".equals(t.getMessage()), "factory double-set message");
        }
    }

    // ---------- deepening: URLConnection state machine via custom handler ----------

    static class StubConnection extends URLConnection {
        int connectCalls;
        StubConnection(URL u) { super(u); }
        public void connect() {
            connectCalls++;
            connected = true;
        }
    }

    static void testCustomHandlerURLConnection() throws Exception {
        // handler supplied via 5-arg ctor is retained and used by openConnection
        final StubConnection[] created = new StubConnection[1];
        URLStreamHandler h = new URLStreamHandler() {
            protected URLConnection openConnection(URL u) {
                StubConnection sc = new StubConnection(u);
                created[0] = sc;
                return sc;
            }
        };
        URL cu = new URL("cproto", "h", -1, "/x", h);
        check("cproto://h/x".equals(cu.toExternalForm()), "custom 5-arg external form");
        URLConnection rc = cu.openConnection();
        check(rc == created[0], "openConnection used our handler (returned our stub)");
        check(rc.getURL() == cu, "connection getURL is the same URL object");

        // timeout defaults and round-trips
        check(rc.getConnectTimeout() == 0, "default connect timeout 0");
        check(rc.getReadTimeout() == 0, "default read timeout 0");
        rc.setConnectTimeout(5000);
        rc.setReadTimeout(3000);
        check(rc.getConnectTimeout() == 5000, "connect timeout round-trip");
        check(rc.getReadTimeout() == 3000, "read timeout round-trip");
        rc.setConnectTimeout(0);
        rc.setReadTimeout(0);
        check(rc.getConnectTimeout() == 0, "connect timeout reset to 0");
        check(rc.getReadTimeout() == 0, "read timeout reset to 0");
        try { rc.setConnectTimeout(-1); check(false, "negative connect timeout"); }
        catch (IllegalArgumentException e) { check("timeout can not be negative".equals(e.getMessage()), "negative connect timeout message"); }
        try { rc.setReadTimeout(-5); check(false, "negative read timeout"); }
        catch (IllegalArgumentException e) { check("timeout can not be negative".equals(e.getMessage()), "negative read timeout message"); }

        // request properties before connect
        rc.setRequestProperty("X-Probe", "v1");
        check("v1".equals(rc.getRequestProperty("X-Probe")), "request property round-trip");
        rc.setRequestProperty("X-Probe", "v2");
        check("v2".equals(rc.getRequestProperty("X-Probe")), "setRequestProperty replaces");
        check(null == rc.getRequestProperty("X-Other"), "unknown request property null");
        rc.addRequestProperty("X-Multi", "a");
        rc.addRequestProperty("X-Multi", "b");
        // multi-valued property holds both values (list order differs JDK8 vs JDK27)
        java.util.List<String> multi = rc.getRequestProperties().get("X-Multi");
        check(multi != null && multi.size() == 2, "addRequestProperty accumulates 2 values");
        check(multi.contains("a") && multi.contains("b"), "addRequestProperty both values present");
        check(null == rc.getRequestProperties().get("X-Other"), "unknown property not in map");

        // defaults for unconnected stub
        check(rc.getContentLength() == -1, "stub content length -1");
        check(rc.getContentLengthLong() == -1L, "stub content length long -1");
        check(null == rc.getContentType(), "stub content type null");
        check(null == rc.getContentEncoding(), "stub content encoding null");
        check(rc.getLastModified() == 0, "stub lastModified 0");
        check(rc.getExpiration() == 0, "stub expiration 0");
        check(rc.getDate() == 0, "stub date 0");
        check(null == rc.getHeaderField("X-Any"), "stub header field null");
        check(null == rc.getHeaderField(0), "stub header field by index null");
        check(null == rc.getHeaderFieldKey(0), "stub header key by index null");

        // connect then state machine violations
        rc.connect();
        check(((StubConnection) rc).connectCalls == 1, "connect reached stub");
        rc.connect();
        // URLConnection.connect() is the subclass hook itself: it is invoked each time
        check(((StubConnection) rc).connectCalls == 2, "connect is the raw subclass hook, invoked per call");
        try { rc.setRequestProperty("X-Late", "y"); check(false, "setRequestProperty after connect"); }
        catch (IllegalStateException e) { check("Already connected".equals(e.getMessage()), "set after connect message"); }
        try { rc.addRequestProperty("X-Late", "y"); check(false, "addRequestProperty after connect"); }
        catch (IllegalStateException e) { check("Already connected".equals(e.getMessage()), "add after connect message"); }
        // getOutputStream unsupported by default URLConnection
        try { rc.getOutputStream(); check(false, "getOutputStream must throw"); }
        catch (java.net.UnknownServiceException e) {
            check("protocol doesn't support output".equals(e.getMessage()), "output unsupported message");
        }

        // two URLs sharing one handler instance still work independently
        URL cu2 = new URL("cproto", "h", -1, "/x", h);
        check(cu.equals(cu2), "same-spec URLs with shared custom handler equal");
        URLConnection rc2 = cu2.openConnection();
        check(rc2 != rc, "second openConnection fresh stub");
        check(rc2.getURL() == cu2, "second stub bound to second URL");
    }

    // ---------- deepening: toExternalForm round-trip and toURI matrix ----------

    static void testExternalFormRoundTrips() throws Exception {
        // specs that must survive parse + toExternalForm unchanged (canonical forms)
        String[] canonical = {
            "http://example.com/",
            "http://example.com/a/b/c",
            "http://example.com:8080/p?q=1#f",
            "http://u:p@example.com/a",
            "https://example.com/",
            "ftp://example.com/pub/file",
            "file:/absolute/path",
            "file:/x/y.txt",
            "jar:file:/x.jar!/entry.txt",
            "mailto:user@example.com",
            "http://127.0.0.1:65535/deep/path",
            "http://[::1]:8080/v6",
        };
        for (String s : canonical) {
            URL u = new URL(s);
            check(s.equals(u.toExternalForm()), "canonical round-trip: " + s);
            check(s.equals(u.toString()), "toString round-trip: " + s);
            // reparsing the external form yields an equal URL
            check(u.equals(new URL(u.toExternalForm())), "reparse equals: " + s);
        }
        // non-canonical inputs normalize predictably
        check("http://example.com/a".equals(new URL("http://example.com/a").toExternalForm()), "plain path");
        check("http://example.com".equals(new URL("http://example.com").toExternalForm()), "no trailing slash added");
        URL qRoot = new URL("http://example.com?q");
        check("http://example.com?q".equals(qRoot.toExternalForm()), "query-only keeps empty path");
        check("".equals(qRoot.getPath()), "query-only path stays empty");
        check("?q".equals(qRoot.getFile()), "query-only file is ?q");
        URL fragOnly = new URL("http://example.com#f");
        check("f".equals(fragOnly.getRef()), "fragment-only ref");
        check("".equals(fragOnly.getPath()), "fragment-only empty path");
        check("".equals(fragOnly.getFile()), "fragment-only empty file");
    }

    static void testToURIMatrix() throws Exception {
        URL u1 = new URL("http://example.com/a%20b?q=1#f");
        URI uri1 = u1.toURI();
        check("http".equals(uri1.getScheme()), "toURI scheme");
        check("example.com".equals(uri1.getHost()), "toURI host");
        check("/a b".equals(uri1.getPath()), "toURI decoded path");
        check("a%20b".equals(uri1.getRawPath().substring(1)), "toURI raw path preserved");
        check("q=1".equals(uri1.getQuery()), "toURI query");
        check("f".equals(uri1.getFragment()), "toURI fragment");
        // round-trip URI <-> URL for clean specs
        URL u2 = new URL("http://example.com/x");
        check(u2.equals(u2.toURI().toURL()), "URL->URI->URL identity");
        // unencoded space breaks toURI
        try {
            new URL("http://example.com/a b").toURI();
            check(false, "toURI on space must throw");
        } catch (java.net.URISyntaxException e) {
            check(e.getIndex() >= 0, "URISyntaxException index set");
            check("http://example.com/a b".equals(e.getInput()), "URISyntaxException input");
        }
        // opaque URL (mailto) converts to an opaque URI
        URI mailto = new URL("mailto:user@example.com").toURI();
        check(mailto.isOpaque(), "mailto URI opaque");
        check("user@example.com".equals(mailto.getSchemeSpecificPart()), "mailto ssp");
    }

    public static void main(String[] args) throws Exception {
        testComponents();
        testEmptyComponents();
        testDefaultPort();
        testConstructors();
        testValidation();
        testEqualsHashCodeSameFile();
        testToURI();
        testSerialization();
        testPortBoundaries();
        testProtocolValidationDeep();
        testHostForms();
        testContextResolutionMatrix();
        testFourArgFileQuerySplit();
        testDefaultPortsMatrix();
        testJarUrls();
        testUserInfoAuthority();
        testEqualsHashCodeDeep();
        testFileUrlConnections();
        testStreamHandlerFactory();
        testCustomHandlerURLConnection();
        testExternalFormRoundTrips();
        testToURIMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
