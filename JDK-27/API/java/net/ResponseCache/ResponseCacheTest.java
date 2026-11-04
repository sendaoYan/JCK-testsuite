/*
 * @test
 * @summary java.net.ResponseCache conformance: white-box structure + black-box behavior.
 * @run main ResponseCacheTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ResponseCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ResponseCache");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object directly");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(Modifier.isAbstract(c.getMethod("get", URI.class, String.class, Map.class).getModifiers()),
            "get abstract");
        check(Modifier.isAbstract(c.getMethod("put", URI.class, URLConnection.class).getModifiers()),
            "put abstract");
        check(Modifier.isStatic(c.getMethod("getDefault").getModifiers()), "getDefault static");
        check(Modifier.isStatic(c.getMethod("setDefault", ResponseCache.class).getModifiers()), "setDefault static");
        check(c.getMethod("getDefault").getReturnType() == ResponseCache.class, "getDefault -> ResponseCache");
        check(c.getMethod("setDefault", ResponseCache.class).getReturnType() == void.class, "setDefault void");
        check(c.getMethod("get", URI.class, String.class, Map.class).getReturnType() == CacheResponse.class,
            "get -> CacheResponse");
        check(c.getMethod("put", URI.class, URLConnection.class).getReturnType() == CacheRequest.class,
            "put -> CacheRequest");
        for (Class<?> t : c.getMethod("get", URI.class, String.class, Map.class).getExceptionTypes())
            check(t == java.io.IOException.class, "get declares IOException only");
        for (Class<?> t : c.getMethod("put", URI.class, URLConnection.class).getExceptionTypes())
            check(t == java.io.IOException.class, "put declares IOException only");
        Set<String> names = new HashSet<>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        check(names.size() == 4, "exactly 4 declared public methods");
        try {
            c.getDeclaredConstructor().newInstance();
            check(false, "newInstance should fail");
        } catch (InstantiationException e) {
            check(true, "InstantiationException");
        } catch (InvocationTargetException e) {
            check(true, "wrapped InstantiationException");
        }
    }

    static void testBlackBox() throws Exception {
        ResponseCache prev = ResponseCache.getDefault();
        try {
            final boolean[] getCalled = { false };
            final boolean[] putCalled = { false };
            ResponseCache rc = new ResponseCache() {
                public CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders) {
                    getCalled[0] = true;
                    return null;
                }
                public CacheRequest put(URI uri, URLConnection conn) {
                    putCalled[0] = true;
                    return null;
                }
            };
            ResponseCache.setDefault(rc);
            check(ResponseCache.getDefault() == rc, "setDefault/getDefault roundtrip");

            URI u = URI.create("http://example.com/");
            rc.get(u, "GET", new HashMap<>());
            check(getCalled[0], "get invoked");
            rc.put(u, null);
            check(putCalled[0], "put invoked");
        } finally {
            ResponseCache.setDefault(prev);
        }
    }

    // ===== deepened contract tests (batch D) =====

    static void testSubclassRoundTrip() throws Exception {
        final CacheResponse canned = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        final CacheRequest sink = new CacheRequest() {
            public OutputStream getBody() { return new ByteArrayOutputStream(); }
            public void abort() { }
        };
        final List<String> log = new ArrayList<>();
        ResponseCache rc = new ResponseCache() {
            public CacheResponse get(URI uri, String method, Map<String, List<String>> headers) {
                log.add("get:" + uri + ":" + method + ":" + headers.containsKey("K"));
                return canned;
            }
            public CacheRequest put(URI uri, URLConnection conn) {
                log.add("put:" + uri);
                return sink;
            }
        };
        URI u = URI.create("http://example.com/path");
        Map<String, List<String>> req = new HashMap<>();
        req.put("K", Collections.singletonList("v"));
        check(rc.get(u, "POST", req) == canned, "get returns exact CacheResponse instance");
        check(rc.get(u, "HEAD", req) == canned, "get repeatable");
        URL fileUrl = new URL("file:///tmp-nonexistent-batchD");
        URLConnection conn = fileUrl.openConnection();
        check(rc.put(u, conn) == sink, "put returns exact CacheRequest instance");
        check(log.size() == 3, "all calls recorded");
        check(log.get(0).equals("get:http://example.com/path:POST:true"), "get sees uri/method/headers");
        check(log.get(2).equals("put:http://example.com/path"), "put sees uri");
        // throwing subclass propagates IOException
        ResponseCache throwing = new ResponseCache() {
            public CacheResponse get(URI uri, String m, Map<String, List<String>> h) throws IOException {
                throw new IOException("no-cache");
            }
            public CacheRequest put(URI uri, URLConnection c) throws IOException {
                throw new IOException("no-store");
            }
        };
        try { throwing.get(u, "GET", req); check(false, "get IOException should propagate"); }
        catch (IOException e) { check(e.getMessage().equals("no-cache"), "get IOException propagated"); }
        try { throwing.put(u, conn); check(false, "put IOException should propagate"); }
        catch (IOException e) { check(e.getMessage().equals("no-store"), "put IOException propagated"); }
    }

    static void testDefaultLifecycle() throws Exception {
        ResponseCache prev = ResponseCache.getDefault();
        try {
            check(ResponseCache.getDefault() == null, "fresh JVM has no default cache");
            ResponseCache a = new ResponseCache() {
                public CacheResponse get(URI u, String m, Map<String, List<String>> h) { return null; }
                public CacheRequest put(URI u, URLConnection c) { return null; }
            };
            ResponseCache.setDefault(a);
            check(ResponseCache.getDefault() == a, "installed retrievable");
            ResponseCache b = new ResponseCache() {
                public CacheResponse get(URI u, String m, Map<String, List<String>> h) { return null; }
                public CacheRequest put(URI u, URLConnection c) { return null; }
            };
            ResponseCache.setDefault(b);
            check(ResponseCache.getDefault() == b, "setDefault replaces");
            check(ResponseCache.getDefault() != a, "previous released");
            ResponseCache.setDefault(null);
            check(ResponseCache.getDefault() == null, "setDefault(null) clears");
            ResponseCache.setDefault(a);
            check(ResponseCache.getDefault() == a, "reinstall works");
        } finally {
            ResponseCache.setDefault(prev);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSubclassRoundTrip();
        testDefaultLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
