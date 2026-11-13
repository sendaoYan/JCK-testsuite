/*
 * @test
 * @summary java.net.URLConnection conformance: white-box structure + black-box behavior.
 *          Deep contract coverage: minimal-subclass default-value matrix, timeout/flag
 *          round-trips, request-property state machine (null/case/multi/connected guard),
 *          file: real connections (length/type/lastModified/stream identity/content),
 *          guessContentTypeFromName/FromStream tables, FileNameMap, per-protocol default
 *          use-caches (JDK 9+), ContentHandlerFactory integration and its set-once Error.
 * @run main URLConnectionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.security.Permission;
import java.util.*;

public class URLConnectionTest {
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
    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    /** Minimal concrete subclass: only implements connect(), counting invocations. */
    static class Probe extends URLConnection {
        int connects;
        Probe(URL u) { super(u); }
        public void connect() { connects++; connected = true; }
        URL urlField() { return url; }
        boolean connectedField() { return connected; }
    }

    static URL httpUrl() throws Exception {
        return new URL("http://example.com/x");
    }

    // ------------------------------------------------------------------
    // existing white-box structure checks
    // ------------------------------------------------------------------
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getDeclaredConstructor(URL.class) != null, "protected ctor(URL)");
        check(Modifier.isProtected(c.getDeclaredConstructor(URL.class).getModifiers()), "ctor(URL) protected");
        check(c.getMethod("connect") != null, "connect");
        check(Modifier.isAbstract(c.getMethod("connect").getModifiers()), "connect abstract");
        check(c.getMethod("getURL") != null, "getURL");
        check(c.getMethod("setConnectTimeout", int.class) != null, "setConnectTimeout");
        check(c.getMethod("setReadTimeout", int.class) != null, "setReadTimeout");
        check(c.getMethod("guessContentTypeFromName", String.class) != null, "guessContentTypeFromName");
        check(c.getMethod("guessContentTypeFromStream", InputStream.class) != null, "guessContentTypeFromStream");
        check(c.getDeclaredField("url") != null, "url field");
        check(Modifier.isProtected(c.getDeclaredField("url").getModifiers()), "url field protected");
        check(c.getDeclaredField("connected") != null, "connected field");
        check(Modifier.isProtected(c.getDeclaredField("connected").getModifiers()), "connected protected");
        check(c.getDeclaredField("doInput") != null, "doInput field protected");
        check(c.getDeclaredField("doOutput") != null, "doOutput field protected");
        check(Modifier.isProtected(c.getDeclaredMethod("getContentLengthLong").getModifiers()) == false, "getContentLengthLong public");
    }

    // ------------------------------------------------------------------
    // default values of every overridable accessor on a minimal subclass
    // ------------------------------------------------------------------
    static void testMinimalDefaults() throws Exception {
        URL u = httpUrl();
        Probe p = new Probe(u);
        check(p.getURL() == u, "getURL returns constructor URL (same object)");
        check(p.urlField() == u, "protected url field is the constructor argument");
        check(!p.connectedField(), "connected false before connect");
        check(p.toString().equals(p.getClass().getName() + ":" + u.toString()),
                "toString = className:url, got " + p.toString());
        check(p.getContentLength() == -1, "getContentLength default -1");
        check(p.getContentLengthLong() == -1L, "getContentLengthLong default -1");
        check(p.getContentType() == null, "getContentType default null");
        check(p.getContentEncoding() == null, "getContentEncoding default null");
        check(p.getExpiration() == 0L, "getExpiration default 0");
        check(p.getDate() == 0L, "getDate default 0");
        check(p.getLastModified() == 0L, "getLastModified default 0");
        check(p.getHeaderField(0) == null, "getHeaderField(0) default null");
        check(p.getHeaderField(1) == null, "getHeaderField(1) default null");
        check(p.getHeaderField(-1) == null, "getHeaderField(-1) default null");
        check(p.getHeaderField(99) == null, "getHeaderField(99) out-of-range null");
        check(p.getHeaderFieldKey(0) == null, "getHeaderFieldKey(0) default null");
        check(p.getHeaderFieldKey(-1) == null, "getHeaderFieldKey(-1) default null");
        check(p.getHeaderFieldKey(99) == null, "getHeaderFieldKey(99) default null");
        check(p.getHeaderField("anything") == null, "getHeaderField(String) default null");
        check(p.getHeaderField("Content-Type") == null, "getHeaderField known name default null");
        Map<String, List<String>> hf = p.getHeaderFields();
        check(hf != null, "getHeaderFields default non-null");
        check(hf.isEmpty(), "getHeaderFields default empty map");
        check(p.getHeaderFieldInt("x", 42) == 42, "getHeaderFieldInt returns default");
        check(p.getHeaderFieldInt("x", -7) == -7, "getHeaderFieldInt negative default");
        check(p.getHeaderFieldLong("x", 42L) == 42L, "getHeaderFieldLong returns default");
        check(p.getHeaderFieldDate("date", 123L) == 123L, "getHeaderFieldDate returns default");
        // default stream/content accessors throw UnknownServiceException
        try {
            p.getInputStream();
            check(false, "getInputStream should throw");
        } catch (UnknownServiceException use) {
            check("protocol doesn't support input".equals(use.getMessage()),
                    "getInputStream USE message, got " + use.getMessage());
        }
        try {
            p.getOutputStream();
            check(false, "getOutputStream should throw");
        } catch (UnknownServiceException use) {
            check("protocol doesn't support output".equals(use.getMessage()),
                    "getOutputStream USE message, got " + use.getMessage());
        }
        fails(UnknownServiceException.class, "getContent default", () -> new Probe(httpUrl()).getContent());
        fails(UnknownServiceException.class, "getContent(Class[]) default",
                () -> new Probe(httpUrl()).getContent(new Class<?>[]{String.class}));
        // default permission for a non-file protocol is AllPermission (SecurityConstants.ALL_PERMISSION)
        Permission perm = new Probe(httpUrl()).getPermission();
        check(perm instanceof java.security.AllPermission, "default getPermission AllPermission, got " + perm);
    }

    // ------------------------------------------------------------------
    // timeout setters: negative IAE (exact message), 0 and positive round-trip
    // ------------------------------------------------------------------
    static void testTimeouts() throws Exception {
        check(new Probe(httpUrl()).getConnectTimeout() == 0, "default connect timeout 0");
        check(new Probe(httpUrl()).getReadTimeout() == 0, "default read timeout 0");
        try {
            new Probe(httpUrl()).setConnectTimeout(-1);
            check(false, "setConnectTimeout(-1) should throw");
        } catch (IllegalArgumentException iae) {
            check("timeout can not be negative".equals(iae.getMessage()),
                    "setConnectTimeout(-1) message, got " + iae.getMessage());
        }
        try {
            new Probe(httpUrl()).setReadTimeout(-1);
            check(false, "setReadTimeout(-1) should throw");
        } catch (IllegalArgumentException iae) {
            check("timeout can not be negative".equals(iae.getMessage()),
                    "setReadTimeout(-1) message, got " + iae.getMessage());
        }
        Probe p = new Probe(httpUrl());
        p.setConnectTimeout(0);
        check(p.getConnectTimeout() == 0, "connect timeout 0 allowed");
        p.setConnectTimeout(5000);
        check(p.getConnectTimeout() == 5000, "connect timeout round-trip");
        p.setConnectTimeout(Integer.MAX_VALUE);
        check(p.getConnectTimeout() == Integer.MAX_VALUE, "connect timeout MAX_VALUE");
        p.setReadTimeout(0);
        check(p.getReadTimeout() == 0, "read timeout 0 allowed");
        p.setReadTimeout(7000);
        check(p.getReadTimeout() == 7000, "read timeout round-trip");
        p.setReadTimeout(1);
        check(p.getReadTimeout() == 1, "read timeout overwrite");
    }

    // ------------------------------------------------------------------
    // flags: doInput/doOutput/allowUserInteraction (instance + static), useCaches,
    // defaultUseCaches, ifModifiedSince
    // ------------------------------------------------------------------
    static void testFlagsAndCaches() throws Exception {
        Probe p = new Probe(httpUrl());
        check(p.getDoInput(), "doInput default true");
        p.setDoInput(false);
        check(!p.getDoInput(), "doInput set false");
        p.setDoInput(true);
        check(p.getDoInput(), "doInput back to true");
        check(!p.getDoOutput(), "doOutput default false");
        p.setDoOutput(true);
        check(p.getDoOutput(), "doOutput set true");
        p.setDoOutput(false);
        check(!p.getDoOutput(), "doOutput back false");
        check(!p.getAllowUserInteraction(), "allowUserInteraction default false");
        p.setAllowUserInteraction(true);
        check(p.getAllowUserInteraction(), "allowUserInteraction set true");
        check(p.getUseCaches(), "useCaches default true");
        p.setUseCaches(false);
        check(!p.getUseCaches(), "useCaches set false");
        p.setIfModifiedSince(1234567890L);
        check(p.getIfModifiedSince() == 1234567890L, "ifModifiedSince round-trip");
        p.setIfModifiedSince(0L);
        check(p.getIfModifiedSince() == 0L, "ifModifiedSince zero");
        p.setIfModifiedSince(-5L);
        check(p.getIfModifiedSince() == -5L, "ifModifiedSince negative allowed (no validation)");
        // instance-level defaultUseCaches: flip affects NEW instances, restore afterwards
        boolean savedDefault = new Probe(httpUrl()).getDefaultUseCaches();
        check(savedDefault, "getDefaultUseCaches default true");
        Probe pd = new Probe(httpUrl());
        pd.setDefaultUseCaches(false);
        check(!pd.getDefaultUseCaches(), "instance getDefaultUseCaches false after set");
        check(!new Probe(httpUrl()).getUseCaches(), "new instance inherits changed default");
        pd.setDefaultUseCaches(true);
        check(new Probe(httpUrl()).getUseCaches(), "new instance inherits restored default");
        // static default allowUserInteraction: save/restore
        boolean savedAui = URLConnection.getDefaultAllowUserInteraction();
        check(!savedAui, "static defaultAllowUserInteraction default false");
        URLConnection.setDefaultAllowUserInteraction(true);
        check(URLConnection.getDefaultAllowUserInteraction(), "static allowUserInteraction round-trip");
        check(new Probe(httpUrl()).getAllowUserInteraction(), "new instance inherits static default");
        URLConnection.setDefaultAllowUserInteraction(false);
        check(!new Probe(httpUrl()).getAllowUserInteraction(), "new instance inherits static false");
        URLConnection.setDefaultAllowUserInteraction(savedAui);
        check(URLConnection.getDefaultAllowUserInteraction() == savedAui, "static allowUserInteraction restored");
    }

    // ------------------------------------------------------------------
    // request properties: set/add/get, null key NPE, case-insensitivity,
    // multi-value, unmodifiable snapshot
    // ------------------------------------------------------------------
    static void testRequestProperties() throws Exception {
        Probe p = new Probe(httpUrl());
        p.setRequestProperty("X", "y");
        check("y".equals(p.getRequestProperty("X")), "set/get request property");
        check(p.getRequestProperty("X-Other") == null, "unknown request property null");
        check(p.getRequestProperty("x") == null || "y".equals(p.getRequestProperty("x")),
                "exact-case lookup (case handling per impl)");
        // null key on set/add -> NPE with fixed message; null key on get -> returns null
        try {
            p.setRequestProperty(null, "v");
            check(false, "setRequestProperty(null,..) should throw NPE");
        } catch (NullPointerException npe) {
            check("key is null".equals(npe.getMessage()), "setRequestProperty null-key message");
        }
        try {
            p.addRequestProperty(null, "v");
            check(false, "addRequestProperty(null,..) should throw NPE");
        } catch (NullPointerException npe) {
            check("key is null".equals(npe.getMessage()), "addRequestProperty null-key message");
        }
        check(p.getRequestProperty(null) == null, "getRequestProperty(null) returns null (no NPE)");
        // null value accepted; getter then yields null
        Probe pv = new Probe(httpUrl());
        pv.setRequestProperty("K", null);
        check(pv.getRequestProperty("K") == null, "null value accepted, getter null");
        Probe pa = new Probe(httpUrl());
        pa.addRequestProperty("K", null);
        check(pa.getRequestProperty("K") == null, "null value via add accepted");
        // key matching is case-insensitive (MessageHeader semantics)
        Probe pc = new Probe(httpUrl());
        pc.setRequestProperty("Content-Type", "text/plain");
        check("text/plain".equals(pc.getRequestProperty("content-type")), "case-insensitive lookup");
        pc.setRequestProperty("content-type", "text/html");
        check("text/html".equals(pc.getRequestProperty("Content-Type")), "case-insensitive overwrite");
        // multi-value add: getRequestProperty returns the most recently added value
        Probe pm = new Probe(httpUrl());
        pm.addRequestProperty("M", "a");
        pm.addRequestProperty("M", "b");
        check("b".equals(pm.getRequestProperty("M")), "getRequestProperty returns last added value");
        // set replaces, add appends
        pm.setRequestProperty("R", "1");
        check("1".equals(pm.getRequestProperty("R")), "set then get");
        pm.addRequestProperty("R", "2");
        Map<String, List<String>> props = pm.getRequestProperties();
        check(props.containsKey("R") && props.containsKey("M"), "snapshot contains both keys");
        check(props.get("R").size() == 2, "R has 2 values after add");
        boolean orderOK = props.get("R").contains("1") && props.get("R").contains("2");
        check(orderOK, "R values contain 1 and 2 (order version-dependent)");
        check(props.get("M").size() == 2 && props.get("M").contains("a") && props.get("M").contains("b"),
                "M values contain a and b");
        // snapshot is unmodifiable
        final Map<String, List<String>> up = pm.getRequestProperties();
        fails(UnsupportedOperationException.class, "request props unmodifiable", () -> up.put("z", null));
        fails(UnsupportedOperationException.class, "request props values unmodifiable", () -> up.get("R").add("3"));
        check(pm.getRequestProperties().containsKey("R"), "snapshot still consistent");
        // keys with space / newline / empty are accepted by both JDK-8 and JDK-27 (probed)
        Probe pw = new Probe(httpUrl());
        pw.setRequestProperty("X Y", "v");
        check("v".equals(pw.getRequestProperty("X Y")), "key with space accepted (JDK behavior)");
        pw.setRequestProperty("X\nY", "v");
        check("v".equals(pw.getRequestProperty("X\nY")), "key with newline accepted (JDK behavior)");
        pw.setRequestProperty("", "v");
        check("v".equals(pw.getRequestProperty("")), "empty key accepted (JDK behavior)");
    }

    // ------------------------------------------------------------------
    // connected guard: which setters/getters throw IllegalStateException
    // ------------------------------------------------------------------
    static void testConnectedGuard() throws Exception {
        Probe p = new Probe(httpUrl());
        p.connect();
        check(p.connects == 1, "connect called once");
        p.connect(); // connect() itself is idempotent at URLConnection level
        check(p.connects == 2, "connect not guarded in base class (subclass may make it idempotent)");
        fails(IllegalStateException.class, "setRequestProperty after connect", () -> p.setRequestProperty("a", "b"));
        fails(IllegalStateException.class, "addRequestProperty after connect", () -> p.addRequestProperty("a", "b"));
        try {
            p.getRequestProperty("a");
            check(false, "getRequestProperty after connect should throw");
        } catch (IllegalStateException ise) {
            check("Already connected".equals(ise.getMessage()), "getRequestProperty ISE message");
        }
        fails(IllegalStateException.class, "getRequestProperties after connect", () -> p.getRequestProperties());
        fails(IllegalStateException.class, "setDoInput after connect", () -> p.setDoInput(false));
        fails(IllegalStateException.class, "setDoOutput after connect", () -> p.setDoOutput(true));
        fails(IllegalStateException.class, "setUseCaches after connect", () -> p.setUseCaches(false));
        fails(IllegalStateException.class, "setIfModifiedSince after connect", () -> p.setIfModifiedSince(1L));
        fails(IllegalStateException.class, "setAllowUserInteraction after connect", () -> p.setAllowUserInteraction(true));
        // timeout setters are NOT guarded by connected (probed on both JDKs)
        p.setConnectTimeout(100);
        check(p.getConnectTimeout() == 100, "setConnectTimeout allowed after connect");
        p.setReadTimeout(200);
        check(p.getReadTimeout() == 200, "setReadTimeout allowed after connect");
        check(p.getDoInput() && !p.getDoOutput() && p.getUseCaches(), "flags unchanged by failed guards");
    }

    // ------------------------------------------------------------------
    // guessContentTypeFromName table
    // ------------------------------------------------------------------
    static void testGuessContentTypeFromName() throws Exception {
        check("text/plain".equals(URLConnection.guessContentTypeFromName("x.txt")), ".txt");
        check("text/html".equals(URLConnection.guessContentTypeFromName("x.html")), ".html");
        check("text/html".equals(URLConnection.guessContentTypeFromName("x.htm")), ".htm");
        check("text/plain".equals(URLConnection.guessContentTypeFromName("path/to/a/b.txt")), "extension after path");
        check("text/plain".equals(URLConnection.guessContentTypeFromName("x.TXT")), "case-insensitive extension");
        check("text/plain".equals(URLConnection.guessContentTypeFromName("a.b.txt")), "last extension wins");
        check("image/gif".equals(URLConnection.guessContentTypeFromName("x.gif")), ".gif");
        check("image/jpeg".equals(URLConnection.guessContentTypeFromName("x.jpg")), ".jpg");
        check("image/jpeg".equals(URLConnection.guessContentTypeFromName("x.jpeg")), ".jpeg");
        check("image/png".equals(URLConnection.guessContentTypeFromName("x.png")), ".png");
        check("application/pdf".equals(URLConnection.guessContentTypeFromName("x.pdf")), ".pdf");
        check("application/xml".equals(URLConnection.guessContentTypeFromName("x.xml")), ".xml");
        check("application/zip".equals(URLConnection.guessContentTypeFromName("x.zip")), ".zip");
        // JDK-8 returns null for .css, JDK-27 returns text/css (content-types.properties differs)
        check("text/css".equals(URLConnection.guessContentTypeFromName("x.css")),
                ".css (JDK-27 table, JDK-8 returns null)");
        check(URLConnection.guessContentTypeFromName("noextension") == null, "no extension -> null");
        check(URLConnection.guessContentTypeFromName("x.class") == null, ".class not in table -> null");
        check(URLConnection.guessContentTypeFromName("") == null, "empty name -> null");
        check(URLConnection.guessContentTypeFromName(".hidden") == null, "dotfile -> null");
        fails(NullPointerException.class, "guessContentTypeFromName(null)", () -> URLConnection.guessContentTypeFromName(null));
    }

    // ------------------------------------------------------------------
    // guessContentTypeFromStream: magic-byte sniffing
    // ------------------------------------------------------------------
    static void testGuessContentTypeFromStream() throws Exception {
        byte[] gif = {'G','I','F','8','7','a', 0,0,0,0,0,0,0,0,0,0,0,0};
        check("image/gif".equals(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(gif))), "GIF87a");
        byte[] gif89 = {'G','I','F','8','9','a', 0,0,0,0,0,0,0,0,0,0,0,0};
        check("image/gif".equals(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(gif89))), "GIF89a");
        byte[] jpg = {(byte)0xFF,(byte)0xD8,(byte)0xFF,(byte)0xE0, 0,0,0,0,0,0,0,0,0,0,0,0};
        check("image/jpeg".equals(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(jpg))), "JPEG SOI");
        byte[] png = {(byte)0x89,'P','N','G',(byte)0x0D,(byte)0x0A,(byte)0x1A,(byte)0x0A, 0,0,0,0,0,0,0,0,0,0};
        check("image/png".equals(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(png))), "PNG signature");
        check("text/html".equals(URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream("<html><body>xxxx</body></html>".getBytes("US-ASCII")))), "html text");
        check(URLConnection.guessContentTypeFromStream(
                new ByteArrayInputStream("just some plain text here!".getBytes("US-ASCII"))) == null,
                "unrecognized content -> null");
        check(URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(new byte[20])) == null,
                "all-zero content -> null");
        // stream that does not support mark: implementation catches the IOException and yields null
        InputStream noMark = new InputStream() {
            public int read() throws IOException { return -1; }
        };
        check(URLConnection.guessContentTypeFromStream(noMark) == null, "non-markable stream -> null");
        fails(NullPointerException.class, "guessContentTypeFromStream(null)",
                () -> URLConnection.guessContentTypeFromStream(null));
    }

    // ------------------------------------------------------------------
    // real file: URL connection
    // ------------------------------------------------------------------
    static final String CONTENT = "hello world"; // 11 bytes

    static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        in.close();
        return new String(bo.toByteArray(), "UTF-8");
    }

    static void testFileConnection(File dir) throws Exception {
        File f = new File(dir, "hello.txt");
        Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        w.write(CONTENT);
        w.close();
        URL fu = f.toURI().toURL();
        URLConnection fc = fu.openConnection();
        fc.connect();
        fc.connect(); // idempotent for file: handler
        check(fc.getContentLength() == CONTENT.length(), "file content length int");
        check(fc.getContentLengthLong() == (long) CONTENT.length(), "file content length long");
        check("text/plain".equals(fc.getContentType()), "file .txt content type");
        check(fc.getLastModified() == f.lastModified(), "lastModified equals File.lastModified()");
        check(fc.getDate() == 0L, "file getDate 0 (no Date header)");
        check(fc.getExpiration() == 0L, "file getExpiration 0");
        check(fc.getContentEncoding() == null, "file getContentEncoding null");
        InputStream s1 = fc.getInputStream();
        InputStream s2 = fc.getInputStream();
        check(s1 == s2, "getInputStream returns same stream object while unread (probed)");
        check(CONTENT.equals(readAll(s1)), "file content round-trip");
        check("11".equals(fc.getHeaderField("content-length")), "header content-length");
        check("text/plain".equals(fc.getHeaderField("content-type")), "header content-type");
        check("content-type".equals(fc.getHeaderFieldKey(0)), "headerFieldKey(0) is content-type");
        check("text/plain".equals(fc.getHeaderField(0)), "headerField(0) is content-type value");
        check(fc.getHeaderFieldInt("content-length", -1) == 11, "getHeaderFieldInt parses file header");
        check(fc.getHeaderFieldLong("content-length", -1L) == 11L, "getHeaderFieldLong parses file header");
        check(fc.getHeaderField("no-such-header") == null, "unknown file header null");
        Permission perm = fc.getPermission();
        check(perm instanceof java.io.FilePermission, "file permission is FilePermission");
        check(perm.getName().equals(f.getPath()), "file permission name is path");
        check(perm.getActions().equals("read"), "file permission action read");
        check(fc.getURL().equals(fu), "file getURL");
        // lazy connect: no explicit connect() needed
        URLConnection lazy = fu.openConnection();
        check(lazy.getContentLength() == CONTENT.length(), "lazy getContentLength connects implicitly");
        // content handlers
        Object c = fu.openConnection().getContent();
        check(c instanceof InputStream, "file getContent() is InputStream (PlainTextInputStream), got " + c);
        Object cs = fu.openConnection().getContent(new Class<?>[]{String.class});
        check(cs == null, "getContent(String.class) null (plain text handler is not a String)");
        Object ci = fu.openConnection().getContent(new Class<?>[]{InputStream.class});
        check(ci instanceof InputStream, "getContent(InputStream.class) matches");
        check(ci instanceof InputStream && CONTENT.equals(readAll((InputStream) ci)), "typed content readable");
    }

    // JDK-8: getHeaderFields() on a file connection is an empty map;
    // JDK-27 exposes content-length/last-modified/content-type headers.
    static void testFileHeaderFields(File dir) throws Exception {
        File f = new File(dir, "hello.txt");
        URLConnection fc = f.toURI().toURL().openConnection();
        fc.connect();
        Map<String, List<String>> hf = fc.getHeaderFields();
        check(hf != null, "header fields non-null");
        check(hf.containsKey("content-length"), "header fields contain content-length (JDK-27)");
        check("11".equals(hf.get("content-length").get(0)), "header field content-length value");
        check(hf.containsKey("content-type"), "header fields contain content-type");
        check("text/plain".equals(hf.get("content-type").get(0)), "header field content-type value");
        check(hf.containsKey("last-modified"), "header fields contain last-modified");
        check(hf.get("last-modified").get(0) != null, "last-modified value non-null");
        fails(UnsupportedOperationException.class, "file header fields unmodifiable", () -> hf.put("z", null));
    }

    static void testDirectoryAndMissing(File dir) throws Exception {
        // directory URL
        URLConnection dc = dir.toURI().toURL().openConnection();
        dc.connect();
        check(dc.getContentLength() >= 0, "directory content length non-negative (platform size)");
        check("text/plain".equals(dc.getContentType()), "directory content type text/plain");
        Object dcs = dc.getInputStream();
        check(dcs instanceof InputStream, "directory input stream");
        ((InputStream) dcs).close();
        // missing file: connect/getInputStream -> FileNotFoundException
        File missing = new File(dir, "missing.txt");
        URLConnection mc = missing.toURI().toURL().openConnection();
        fails(FileNotFoundException.class, "missing file connect", () -> mc.connect());
        URLConnection mc2 = missing.toURI().toURL().openConnection();
        fails(FileNotFoundException.class, "missing file getInputStream", () -> mc2.getInputStream());
        URLConnection mc3 = missing.toURI().toURL().openConnection();
        check(mc3.getContentLengthLong() == 0L,
                "missing file getContentLengthLong 0 (probed: FileURLConnection returns 0, not -1)");
        URLConnection mc4 = missing.toURI().toURL().openConnection();
        check("text/plain".equals(mc4.getContentType()),
                "missing file getContentType is name-based guess, no connect, no exception");
    }

    // ------------------------------------------------------------------
    // FileNameMap default instance and setter round-trip (restored)
    // ------------------------------------------------------------------
    static void testFileNameMap() throws Exception {
        FileNameMap original = URLConnection.getFileNameMap();
        check(original != null, "default FileNameMap non-null");
        check("text/plain".equals(original.getContentTypeFor("a.txt")), "default map .txt");
        check("text/html".equals(original.getContentTypeFor("a.html")), "default map .html");
        FileNameMap custom = new FileNameMap() {
            public String getContentTypeFor(String fileName) { return "application/x-custom"; }
        };
        URLConnection.setFileNameMap(custom);
        check(URLConnection.getFileNameMap() == custom, "setFileNameMap round-trip identity");
        check("application/x-custom".equals(URLConnection.guessContentTypeFromName("z.txt")),
                "guessContentTypeFromName honors installed FileNameMap");
        URLConnection.setFileNameMap(original);
        check(URLConnection.getFileNameMap() == original, "FileNameMap restored");
        check("text/plain".equals(URLConnection.guessContentTypeFromName("z.txt")), "guess works after restore");
    }

    // ------------------------------------------------------------------
    // deprecated static default request properties are no-ops returning null
    // ------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    static void testDefaultRequestPropertyDeprecated() throws Exception {
        check(URLConnection.getDefaultRequestProperty("X") == null, "getDefaultRequestProperty null");
        URLConnection.setDefaultRequestProperty("X", "v"); // no-op, no exception
        check(URLConnection.getDefaultRequestProperty("X") == null, "still null after set (no-op)");
    }

    // ------------------------------------------------------------------
    // JDK 9+ per-protocol default use caches
    // ------------------------------------------------------------------
    static void testPerProtocolDefaultUseCaches() throws Exception {
        check(URLConnection.getDefaultUseCaches("http"), "per-protocol http default true");
        check(URLConnection.getDefaultUseCaches("file"), "per-protocol file default true");
        URLConnection.setDefaultUseCaches("http", false);
        check(!URLConnection.getDefaultUseCaches("http"), "per-protocol http set false");
        check(URLConnection.getDefaultUseCaches("file"), "file unaffected by http change");
        URLConnection.setDefaultUseCaches("http", true);
        check(URLConnection.getDefaultUseCaches("http"), "per-protocol restored");
        URLConnection.setDefaultUseCaches("myproto", false);
        check(!URLConnection.getDefaultUseCaches("myproto"), "arbitrary protocol settable");
        URLConnection.setDefaultUseCaches("myproto", true);
    }

    // ------------------------------------------------------------------
    // ContentHandlerFactory integration: must run LAST in this JVM because
    // (a) installing the factory is once-per-JVM, (b) getContentHandler caches
    // handlers per mime type, so an earlier getContent("text/plain") would
    // shadow the factory. We use a .html file whose handler was never cached.
    // ------------------------------------------------------------------
    static void testContentHandlerFactory(File dir) throws Exception {
        final List<String> mimes = new ArrayList<String>();
        final Object sentinel = "CH-FACTORY";
        URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
            public ContentHandler createContentHandler(String mime) {
                mimes.add(mime);
                return new ContentHandler() {
                    public Object getContent(URLConnection c) { return sentinel; }
                };
            }
        });
        File h = new File(dir, "x.html");
        Writer w = new OutputStreamWriter(new FileOutputStream(h), "UTF-8");
        w.write("<html/>");
        w.close();
        Object got = h.toURI().toURL().openConnection().getContent();
        check(got == sentinel, "getContent routed through registered ContentHandlerFactory");
        check(mimes.contains("text/html"), "factory received text/html mime, saw " + mimes);
        // second installation fails with Error (probed: java.lang.Error "factory already defined")
        try {
            URLConnection.setContentHandlerFactory(new ContentHandlerFactory() {
                public ContentHandler createContentHandler(String m) { return null; }
            });
            check(false, "second setContentHandlerFactory should throw Error");
        } catch (Error e) {
            check("factory already defined".equals(e.getMessage()), "factory already defined message");
        }
    }

    // ------------------------------------------------------------------
    // existing black-box checks (kept from previous version)
    // ------------------------------------------------------------------
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

    // ------------------------------------------------------------------
    // complete public API surface (structural contract)
    // ------------------------------------------------------------------
    static void testPublicApiSurface() throws Exception {
        Class<?> c = Class.forName("java.net.URLConnection");
        String[] methodNames = {
            "connect", "setConnectTimeout", "getConnectTimeout", "setReadTimeout", "getReadTimeout",
            "getURL", "getContentLength", "getContentLengthLong", "getContentType", "getContentEncoding",
            "getExpiration", "getDate", "getLastModified", "getHeaderField", "getHeaderFields",
            "getHeaderFieldInt", "getHeaderFieldLong", "getHeaderFieldDate", "getHeaderFieldKey",
            "getContent", "getPermission", "getInputStream", "getOutputStream", "toString",
            "setDoInput", "getDoInput", "setDoOutput", "getDoOutput", "setAllowUserInteraction",
            "getAllowUserInteraction", "setDefaultAllowUserInteraction", "getDefaultAllowUserInteraction",
            "setUseCaches", "getUseCaches", "setIfModifiedSince", "getIfModifiedSince",
            "getDefaultUseCaches", "setDefaultUseCaches",
            "setRequestProperty", "addRequestProperty", "getRequestProperty", "getRequestProperties",
            "setDefaultRequestProperty", "getDefaultRequestProperty",
            "setContentHandlerFactory", "getFileNameMap", "setFileNameMap",
            "guessContentTypeFromName", "guessContentTypeFromStream",
        };
        java.util.Set<String> present = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) present.add(m.getName());
        for (String name : methodNames) check(present.contains(name), "public method present: " + name);
        // signature spot checks
        check(c.getMethod("connect").getReturnType() == void.class, "connect returns void");
        check(c.getMethod("connect").getExceptionTypes().length == 1
                && c.getMethod("connect").getExceptionTypes()[0] == IOException.class,
                "connect throws IOException");
        check(c.getMethod("getContentLengthLong").getReturnType() == long.class, "getContentLengthLong returns long");
        check(c.getMethod("getContentLength").getReturnType() == int.class, "getContentLength returns int");
        check(c.getMethod("getHeaderFields").getReturnType() == Map.class, "getHeaderFields returns Map");
        check(java.lang.reflect.Modifier.isStatic(c.getMethod("guessContentTypeFromName", String.class).getModifiers()),
                "guessContentTypeFromName is static");
        check(!java.lang.reflect.Modifier.isStatic(c.getMethod("connect").getModifiers()), "connect is not static");
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("getDefaultUseCaches", String.class).getModifiers()),
                "per-protocol getDefaultUseCaches is static (JDK 9+)");
    }

    // ------------------------------------------------------------------
    // extra probed semantics: overwrite, stream position, connect counting
    // ------------------------------------------------------------------
    static void testExtraSemantics() throws Exception {
        // setRequestProperty overwrites (single value kept)
        Probe p = new Probe(httpUrl());
        p.setRequestProperty("X", "y");
        p.setRequestProperty("X", "z");
        check("z".equals(p.getRequestProperty("X")), "setRequestProperty overwrites");
        check(p.getRequestProperties().get("X").size() == 1, "overwrite keeps single value");
        // guessContentTypeFromStream resets the stream: full content still readable
        byte[] doc = "<html>rest of document body".getBytes("US-ASCII");
        ByteArrayInputStream in = new ByteArrayInputStream(doc);
        check("text/html".equals(URLConnection.guessContentTypeFromStream(in)), "guess from prefix");
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        int n; byte[] buf = new byte[64];
        while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
        check(java.util.Arrays.equals(doc, bo.toByteArray()), "stream position preserved after guess");
        // default getInputStream does not delegate to connect()
        Probe pc = new Probe(httpUrl());
        pc.connect();
        check(pc.connects == 1, "one connect");
        fails(UnknownServiceException.class, "getInputStream after connect still USE", () -> pc.getInputStream());
        check(pc.connects == 1, "getInputStream did not re-invoke connect");
        // getOutputStream likewise
        fails(UnknownServiceException.class, "getOutputStream after connect still USE", () -> pc.getOutputStream());
        // header lookups do not require connection on minimal subclass
        Probe ph = new Probe(httpUrl());
        check(ph.getHeaderField("date") == null && ph.getHeaderFields().isEmpty(),
                "header reads fine on unconnected probe");
        check(ph.getContentType() == null, "contentType null before connect on probe");
        check(ph.getContentLength() == -1, "contentLength -1 before connect on probe");
    }

    public static void main(String[] args) throws Exception {
        // Absolute temp directory: "/tmp" is drive-relative on Windows, and
        // FilePermission names are derived from the absolute file URL.
        File dir = new File(System.getProperty("java.io.tmpdir"), "net-c-URLConnection");
        dir.mkdirs();
        try {
            testWhiteBox();
            testPublicApiSurface();
            testMinimalDefaults();
            testTimeouts();
            testFlagsAndCaches();
            testRequestProperties();
            testConnectedGuard();
            testGuessContentTypeFromName();
            testGuessContentTypeFromStream();
            testFileConnection(dir);
            testFileHeaderFields(dir);
            testDirectoryAndMissing(dir);
            testFileNameMap();
            testDefaultRequestPropertyDeprecated();
            testPerProtocolDefaultUseCaches();
            testExtraSemantics();
            testBlackBox();
            testContentHandlerFactory(dir); // last: poisons JVM-global ContentHandlerFactory
        } finally {
            for (File x : dir.listFiles()) x.delete();
            dir.delete();
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
