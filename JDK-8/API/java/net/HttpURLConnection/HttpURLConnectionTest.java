/*
 * @test
 * @summary java.net.HttpURLConnection conformance: white-box structure + black-box behavior.
 *          Deep contract coverage: complete HTTP_* status-code constant table, request-method
 *          matrix (valid/invalid/null/after-connect), streaming-mode state machine
 *          (fixed/chunked mutual exclusion, negative IAE, connected guard), redirect
 *          defaults (static + instance linkage), response-code/message paths on a minimal
 *          subclass, permissions, disconnect/usingProxy delegation. (Java 8 source level; setAuthenticator is JDK 9+ only.)
 * @run main HttpURLConnectionTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.security.Permission;

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

    static int connectCalls, disconnectCalls;
    static boolean proxyFlag = false;

    /** Minimal concrete subclass recording connect/disconnect/usingProxy. */
    static class H extends HttpURLConnection {
        H(URL u) { super(u); }
        public void disconnect() { disconnectCalls++; connected = false; }
        public boolean usingProxy() { return proxyFlag; }
        public void connect() { connectCalls++; connected = true; }
    }

    static URL url() throws Exception { return new URL("http://example.com/x"); }

    // ------------------------------------------------------------------
    // original structure checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testOriginalStructure() throws Exception {
        Class<?> c = Class.forName("java.net.HttpURLConnection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.net.URLConnection".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addRequestProperty"), "has method addRequestProperty");
        check(names.contains("connect"), "has method connect");
        check(names.contains("disconnect"), "has method disconnect");
    }

    // ------------------------------------------------------------------
    // white-box structure checks
    // ------------------------------------------------------------------
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpURLConnection");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(URLConnection.class.isAssignableFrom(c), "extends URLConnection");
        check(c.getDeclaredConstructor(URL.class) != null, "protected ctor(URL)");
        check(Modifier.isProtected(c.getDeclaredConstructor(URL.class).getModifiers()), "ctor(URL) protected");
        check(c.getMethod("setRequestMethod", String.class) != null, "setRequestMethod");
        check(c.getMethod("getResponseCode") != null, "getResponseCode");
        check(c.getMethod("disconnect") != null, "disconnect");
        check(Modifier.isAbstract(c.getMethod("disconnect").getModifiers()), "disconnect abstract");
        check(c.getMethod("usingProxy") != null, "usingProxy");
        check(Modifier.isAbstract(c.getMethod("usingProxy").getModifiers()), "usingProxy abstract");
        check(Modifier.isProtected(c.getDeclaredField("method").getModifiers()), "method field protected");
        check(Modifier.isProtected(c.getDeclaredField("responseCode").getModifiers()), "responseCode field protected");
        check(Modifier.isProtected(c.getDeclaredField("responseMessage").getModifiers()), "responseMessage protected");
        check(Modifier.isProtected(c.getDeclaredField("instanceFollowRedirects").getModifiers()),
                "instanceFollowRedirects protected");
        check(Modifier.isProtected(c.getDeclaredField("chunkLength").getModifiers()), "chunkLength protected");
        check(Modifier.isProtected(c.getDeclaredField("fixedContentLength").getModifiers()),
                "fixedContentLength protected");
        check(Modifier.isProtected(c.getDeclaredField("fixedContentLengthLong").getModifiers()),
                "fixedContentLengthLong protected");
    }

    // ------------------------------------------------------------------
    // complete HTTP_* status-code constant table (values verified on both JDKs)
    // ------------------------------------------------------------------
    static void testHttpConstants() {
        check(HttpURLConnection.HTTP_OK == 200, "HTTP_OK");
        check(HttpURLConnection.HTTP_CREATED == 201, "HTTP_CREATED");
        check(HttpURLConnection.HTTP_ACCEPTED == 202, "HTTP_ACCEPTED");
        check(HttpURLConnection.HTTP_NOT_AUTHORITATIVE == 203, "HTTP_NOT_AUTHORITATIVE");
        check(HttpURLConnection.HTTP_NO_CONTENT == 204, "HTTP_NO_CONTENT");
        check(HttpURLConnection.HTTP_RESET == 205, "HTTP_RESET");
        check(HttpURLConnection.HTTP_PARTIAL == 206, "HTTP_PARTIAL");
        check(HttpURLConnection.HTTP_MULT_CHOICE == 300, "HTTP_MULT_CHOICE");
        check(HttpURLConnection.HTTP_MOVED_PERM == 301, "HTTP_MOVED_PERM");
        check(HttpURLConnection.HTTP_MOVED_TEMP == 302, "HTTP_MOVED_TEMP");
        check(HttpURLConnection.HTTP_SEE_OTHER == 303, "HTTP_SEE_OTHER");
        check(HttpURLConnection.HTTP_NOT_MODIFIED == 304, "HTTP_NOT_MODIFIED");
        check(HttpURLConnection.HTTP_USE_PROXY == 305, "HTTP_USE_PROXY");
        check(HttpURLConnection.HTTP_BAD_REQUEST == 400, "HTTP_BAD_REQUEST");
        check(HttpURLConnection.HTTP_UNAUTHORIZED == 401, "HTTP_UNAUTHORIZED");
        check(HttpURLConnection.HTTP_PAYMENT_REQUIRED == 402, "HTTP_PAYMENT_REQUIRED");
        check(HttpURLConnection.HTTP_FORBIDDEN == 403, "HTTP_FORBIDDEN");
        check(HttpURLConnection.HTTP_NOT_FOUND == 404, "HTTP_NOT_FOUND");
        check(HttpURLConnection.HTTP_BAD_METHOD == 405, "HTTP_BAD_METHOD");
        check(HttpURLConnection.HTTP_NOT_ACCEPTABLE == 406, "HTTP_NOT_ACCEPTABLE");
        check(HttpURLConnection.HTTP_PROXY_AUTH == 407, "HTTP_PROXY_AUTH");
        check(HttpURLConnection.HTTP_CLIENT_TIMEOUT == 408, "HTTP_CLIENT_TIMEOUT");
        check(HttpURLConnection.HTTP_CONFLICT == 409, "HTTP_CONFLICT");
        check(HttpURLConnection.HTTP_GONE == 410, "HTTP_GONE");
        check(HttpURLConnection.HTTP_LENGTH_REQUIRED == 411, "HTTP_LENGTH_REQUIRED");
        check(HttpURLConnection.HTTP_PRECON_FAILED == 412, "HTTP_PRECON_FAILED");
        check(HttpURLConnection.HTTP_ENTITY_TOO_LARGE == 413, "HTTP_ENTITY_TOO_LARGE");
        check(HttpURLConnection.HTTP_REQ_TOO_LONG == 414, "HTTP_REQ_TOO_LONG");
        check(HttpURLConnection.HTTP_UNSUPPORTED_TYPE == 415, "HTTP_UNSUPPORTED_TYPE");
        check(HttpURLConnection.HTTP_SERVER_ERROR == 500, "HTTP_SERVER_ERROR");
        check(HttpURLConnection.HTTP_INTERNAL_ERROR == 500, "HTTP_INTERNAL_ERROR");
        check(HttpURLConnection.HTTP_NOT_IMPLEMENTED == 501, "HTTP_NOT_IMPLEMENTED");
        check(HttpURLConnection.HTTP_BAD_GATEWAY == 502, "HTTP_BAD_GATEWAY");
        check(HttpURLConnection.HTTP_UNAVAILABLE == 503, "HTTP_UNAVAILABLE");
        check(HttpURLConnection.HTTP_GATEWAY_TIMEOUT == 504, "HTTP_GATEWAY_TIMEOUT");
        check(HttpURLConnection.HTTP_VERSION == 505, "HTTP_VERSION");
        // grouping sanity
        check(HttpURLConnection.HTTP_OK >= 200 && HttpURLConnection.HTTP_PARTIAL < 300, "2xx range");
        check(HttpURLConnection.HTTP_MULT_CHOICE >= 300 && HttpURLConnection.HTTP_USE_PROXY < 400, "3xx range");
        check(HttpURLConnection.HTTP_BAD_REQUEST >= 400 && HttpURLConnection.HTTP_UNSUPPORTED_TYPE < 500, "4xx range");
        check(HttpURLConnection.HTTP_SERVER_ERROR >= 500 && HttpURLConnection.HTTP_VERSION < 600, "5xx range");
    }

    // ------------------------------------------------------------------
    // request method matrix
    // ------------------------------------------------------------------
    static void testRequestMethod() throws Exception {
        H h = new H(url());
        check("GET".equals(h.getRequestMethod()), "default method GET");
        String[] valid = {"GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"};
        for (String m : valid) {
            h.setRequestMethod(m);
            check(m.equals(h.getRequestMethod()), "setRequestMethod " + m);
        }
        h.setRequestMethod("GET");
        check("GET".equals(h.getRequestMethod()), "back to GET");
        // invalid: lowercase, empty, null, junk -> ProtocolException (NOT NPE for null; probed)
        try {
            h.setRequestMethod("get");
            check(false, "setRequestMethod(get) should throw");
        } catch (ProtocolException pe) {
            check(("Invalid HTTP method: get").equals(pe.getMessage()),
                    "invalid method message, got " + pe.getMessage());
        }
        try {
            h.setRequestMethod("");
            check(false, "setRequestMethod(\"\") should throw");
        } catch (ProtocolException pe) {
            check("Invalid HTTP method: ".equals(pe.getMessage()), "empty method message");
        }
        try {
            h.setRequestMethod(null);
            check(false, "setRequestMethod(null) should throw");
        } catch (ProtocolException pe) {
            check("Invalid HTTP method: null".equals(pe.getMessage()),
                    "null method -> ProtocolException not NPE (probed), got " + pe);
        }
        try {
            h.setRequestMethod("foo bar");
            check(false, "setRequestMethod with space should throw");
        } catch (ProtocolException pe) {
            check(true, "space method ProtocolException");
        }
        // method unchanged by failed sets
        check("GET".equals(h.getRequestMethod()), "method unchanged after failures");
        // after connect: ProtocolException "Can't reset method: already connected"
        H hc = new H(url());
        hc.connect();
        try {
            hc.setRequestMethod("POST");
            check(false, "setRequestMethod after connect should throw");
        } catch (ProtocolException pe) {
            check("Can't reset method: already connected".equals(pe.getMessage()),
                    "already connected method message");
        }
    }

    // ------------------------------------------------------------------
    // streaming-mode state machine
    // ------------------------------------------------------------------
    static void testStreamingModes() throws Exception {
        // negative fixed length -> IAE
        try {
            new H(url()).setFixedLengthStreamingMode(-1);
            check(false, "fixed(-1) should throw IAE");
        } catch (IllegalArgumentException iae) {
            check("invalid content length".equals(iae.getMessage()), "fixed(-1) message");
        }
        try {
            new H(url()).setFixedLengthStreamingMode(-1L);
            check(false, "fixed long -1 should throw IAE");
        } catch (IllegalArgumentException iae) {
            check("invalid content length".equals(iae.getMessage()), "fixed(-1L) message");
        }
        // legal fixed lengths incl. > Integer.MAX_VALUE via long
        new H(url()).setFixedLengthStreamingMode(0);
        new H(url()).setFixedLengthStreamingMode(100);
        new H(url()).setFixedLengthStreamingMode(1L + Integer.MAX_VALUE);
        check(true, "legal fixed lengths accepted");
        // fixed then chunked -> ISE
        H f1 = new H(url());
        f1.setFixedLengthStreamingMode(10);
        try {
            f1.setChunkedStreamingMode(10);
            check(false, "fixed then chunked should throw");
        } catch (IllegalStateException ise) {
            check("Fixed length streaming mode set".equals(ise.getMessage()), "fixed->chunked message");
        }
        // chunked then fixed (int) -> ISE
        H c1 = new H(url());
        c1.setChunkedStreamingMode(10);
        try {
            c1.setFixedLengthStreamingMode(10);
            check(false, "chunked then fixed int should throw");
        } catch (IllegalStateException ise) {
            check("Chunked encoding streaming mode set".equals(ise.getMessage()), "chunked->fixed(int) message");
        }
        // chunked then fixed (long) -> ISE
        H c2 = new H(url());
        c2.setChunkedStreamingMode(10);
        try {
            c2.setFixedLengthStreamingMode(10L);
            check(false, "chunked then fixed long should throw");
        } catch (IllegalStateException ise) {
            check("Chunked encoding streaming mode set".equals(ise.getMessage()), "chunked->fixed(long) message");
        }
        // re-setting fixed after fixed is allowed (probed: no exception)
        H f2 = new H(url());
        f2.setFixedLengthStreamingMode(5);
        f2.setFixedLengthStreamingMode(6);
        f2.setFixedLengthStreamingMode(7L);
        check(true, "fixed may be reset before connect");
        H f3 = new H(url());
        f3.setFixedLengthStreamingMode(5L);
        f3.setFixedLengthStreamingMode(6);
        check(true, "long fixed then int fixed allowed");
        // chunked with <=0 does NOT throw: implementation silently uses the default
        // chunk size (probed on both JDKs)
        new H(url()).setChunkedStreamingMode(-1);
        new H(url()).setChunkedStreamingMode(0);
        new H(url()).setChunkedStreamingMode(1);
        check(true, "chunked accepts non-positive (default size used)");
        // after connect -> ISE
        H conn = new H(url());
        conn.connect();
        try {
            conn.setFixedLengthStreamingMode(1);
            check(false, "fixed after connect should throw");
        } catch (IllegalStateException ise) {
            check("Already connected".equals(ise.getMessage()), "fixed after connect message");
        }
        H conn2 = new H(url());
        conn2.connect();
        try {
            conn2.setChunkedStreamingMode(1);
            check(false, "chunked after connect should throw");
        } catch (IllegalStateException ise) {
            check("Can't set streaming mode: already connected".equals(ise.getMessage()),
                    "chunked after connect message");
        }
    }

    // ------------------------------------------------------------------
    // redirects: static default/linkage and instance round-trip
    // ------------------------------------------------------------------
    static void testRedirects() throws Exception {
        boolean saved = HttpURLConnection.getFollowRedirects();
        check(HttpURLConnection.getFollowRedirects(), "static default true");
        HttpURLConnection.setFollowRedirects(false);
        check(!HttpURLConnection.getFollowRedirects(), "static set false round-trip");
        // new instances snapshot the static flag at construction (probed)
        check(!new H(url()).getInstanceFollowRedirects(), "instance inherits static false at ctor");
        HttpURLConnection.setFollowRedirects(true);
        check(new H(url()).getInstanceFollowRedirects(), "instance inherits static true at ctor");
        // instance flag round-trip is independent of the static flag afterwards
        H h = new H(url());
        check(h.getInstanceFollowRedirects(), "instance default true");
        h.setInstanceFollowRedirects(false);
        check(!h.getInstanceFollowRedirects(), "instance set false");
        h.setInstanceFollowRedirects(true);
        check(h.getInstanceFollowRedirects(), "instance set true");
        HttpURLConnection.setFollowRedirects(false);
        check(h.getInstanceFollowRedirects(), "instance flag unaffected by later static change");
        HttpURLConnection.setFollowRedirects(saved);
        check(HttpURLConnection.getFollowRedirects() == saved, "static restored");
    }

    // ------------------------------------------------------------------
    // response paths and defaults on a minimal subclass (no network)
    // ------------------------------------------------------------------
    static void testDefaults() throws Exception {
        H h = new H(url());
        check(h.getErrorStream() == null, "getErrorStream null on minimal subclass");
        check(h.getHeaderFieldKey(0) == null, "getHeaderFieldKey(0) null");
        check(h.getHeaderField(0) == null, "getHeaderField(0) null");
        check(h.getHeaderFieldDate("date", 77L) == 77L, "getHeaderFieldDate default passthrough");
        check(h.getHeaderFieldInt("code", -1) == -1, "getHeaderFieldInt default passthrough");
        // getResponseCode/Message call getInputStream() which defaults to UnknownServiceException
        try {
            new H(url()).getResponseCode();
            check(false, "getResponseCode should throw on minimal subclass");
        } catch (UnknownServiceException use) {
            check("protocol doesn't support input".equals(use.getMessage()),
                    "getResponseCode USE message (delegates to getInputStream)");
        }
        try {
            new H(url()).getResponseMessage();
            check(false, "getResponseMessage should throw on minimal subclass");
        } catch (UnknownServiceException use) {
            check("protocol doesn't support input".equals(use.getMessage()),
                    "getResponseMessage USE message");
        }
        // permissions: SocketPermission host:port connect,resolve
        Permission p1 = new H(url()).getPermission();
        check(p1 instanceof java.net.SocketPermission, "http permission is SocketPermission");
        check("example.com:80".equals(p1.getName()), "default port 80 used, got " + p1.getName());
        check("connect,resolve".equals(p1.getActions()), "permission actions connect,resolve");
        Permission p2 = new H(new URL("http://example.com:8080/x")).getPermission();
        check("example.com:8080".equals(p2.getName()), "explicit port in permission");
        check(p2 instanceof java.net.SocketPermission, "8080 permission type");
        // toString of the minimal subclass contains the URL
        check(h.toString().contains("http://example.com/x"), "toString contains URL");
        check(h.getURL().toString().equals("http://example.com/x"), "getURL");
        check(h.usingProxy() == false, "usingProxy delegates to subclass");
        proxyFlag = true;
        check(new H(url()).usingProxy(), "usingProxy overridden true");
        proxyFlag = false;
        check(h.getDoInput(), "doInput default inherited true");
        check(!h.getDoOutput(), "doOutput default inherited false");
    }

    // ------------------------------------------------------------------
    // connect/disconnect delegation and idempotency on the subclass
    // ------------------------------------------------------------------
    static void testConnectDisconnect() throws Exception {
        H h = new H(url());
        int beforeConnect = connectCalls;
        h.connect();
        h.connect(); // base class does not prevent repeated connect calls
        check(connectCalls - beforeConnect == 2, "connect called for each invocation (subclass contract)");
        int beforeDisc = disconnectCalls;
        h.disconnect();
        h.disconnect();
        check(disconnectCalls - beforeDisc == 2, "disconnect called for each invocation");
        // connect after disconnect works again on our subclass
        h.connect();
        check(connectCalls - beforeConnect == 3, "connect after disconnect");
    }

    // ------------------------------------------------------------------
    // existing black-box checks (kept from previous version)
    // ------------------------------------------------------------------
    static void testBlackBox() throws Exception {
        HttpURLConnection h = new HttpURLConnection(new URL("http://example.com/")) {
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        };
        check(h.getRequestMethod().equals("GET"), "default method GET");
        h.setRequestMethod("POST");
        check(h.getRequestMethod().equals("POST"), "setRequestMethod POST");
        h.setRequestMethod("PUT");
        check(h.getRequestMethod().equals("PUT"), "setRequestMethod PUT");
        fails(ProtocolException.class, "method with space", () -> h.setRequestMethod("foo bar"));

        // streaming modes (on separate instances to avoid mixed-mode errors)
        new HttpURLConnection(new URL("http://example.com/")) {
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        }.setFixedLengthStreamingMode(100L);
        new HttpURLConnection(new URL("http://example.com/")) {
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        }.setChunkedStreamingMode(1024);

        // permission and error stream
        java.security.Permission perm = h.getPermission();
        check(perm != null, "getPermission non-null");
        check(h.getErrorStream() == null, "error stream null before connect");
    }

    // ------------------------------------------------------------------
    // structural constant verification: every HTTP_* field is public static final int
    // ------------------------------------------------------------------
    static void testConstantStructure() throws Exception {
        int count = 0;
        for (Field f : HttpURLConnection.class.getFields()) {
            if (f.getName().startsWith("HTTP_")) {
                count++;
                check(Modifier.isPublic(f.getModifiers())
                        && Modifier.isStatic(f.getModifiers())
                        && Modifier.isFinal(f.getModifiers()), "constant flags " + f.getName());
                check(f.getType() == int.class, "constant is int " + f.getName());
                check(f.get(null) instanceof Integer, "constant readable " + f.getName());
            }
        }
        check(count == 36, "exactly 36 HTTP_* constants, got " + count);
    }

    // ------------------------------------------------------------------
    // inherited URLConnection behavior on the HttpURLConnection subclass
    // ------------------------------------------------------------------
    static void testInheritedConnectionBehavior() throws Exception {
        H h = new H(url());
        check(h.getContentLength() == -1, "inherited getContentLength -1");
        check(h.getContentLengthLong() == -1L, "inherited getContentLengthLong -1");
        check(h.getContentType() == null, "inherited getContentType null");
        check(h.getContentEncoding() == null, "inherited getContentEncoding null");
        check(h.getHeaderFields().isEmpty(), "inherited getHeaderFields empty");
        check(h.getConnectTimeout() == 0 && h.getReadTimeout() == 0, "inherited timeouts 0");
        h.setConnectTimeout(250);
        check(h.getConnectTimeout() == 250, "setConnectTimeout on H");
        h.setReadTimeout(350);
        check(h.getReadTimeout() == 350, "setReadTimeout on H");
        h.setRequestProperty("X-Test", "1");
        check("1".equals(h.getRequestProperty("X-Test")), "setRequestProperty on H");
        h.addRequestProperty("X-Test", "2");
        check("2".equals(h.getRequestProperty("X-Test")), "addRequestProperty last value on H");
        check(h.getRequestProperties().get("X-Test").size() == 2, "request props map on H");
        // connected guard inherited too
        h.connect();
        fails(IllegalStateException.class, "request property guard inherited", () -> h.setRequestProperty("a", "b"));
        fails(IllegalStateException.class, "getRequestProperty guard inherited", () -> h.getRequestProperty("a"));
        // streaming mode rejects after connect through the subclass as well (checked in
        // testStreamingModes); disconnect() on our subclass clears connected so requests
        // become mutable again
        h.disconnect();
        h.setRequestProperty("after", "disconnect");
        check("disconnect".equals(h.getRequestProperty("after")),
                "mutable again after subclass disconnect clears connected");
        // method may be reset after disconnect since connected is false again
        h.setRequestMethod("POST");
        check("POST".equals(h.getRequestMethod()), "method reset after disconnect");
        // getResponseCode still USE after disconnect (getInputStream not overridden)
        fails(UnknownServiceException.class, "getResponseCode USE after disconnect", () -> h.getResponseCode());
    }

    public static void main(String[] args) throws Exception {
        testOriginalStructure();
        testWhiteBox();
        testHttpConstants();
        testConstantStructure();
        testRequestMethod();
        testStreamingModes();
        testRedirects();
        testDefaults();
        testConnectDisconnect();
        testInheritedConnectionBehavior();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
