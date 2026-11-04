/*
 * @test
 * @summary java.net.CacheResponse conformance: white-box structure + black-box behavior.
 * @run main CacheResponseTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CacheResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CacheResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Java-8-safe full read (no InputStream.readAllBytes before JDK-9). */
    static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) != -1) buf.write(chunk, 0, n);
        return buf.toByteArray();
    }

    // ===== original assertions preserved verbatim =====
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.CacheResponse");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBody"), "has method getBody");
        check(names.contains("getHeaders"), "has method getHeaders");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CacheResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(c.getMethod("getHeaders") != null, "getHeaders");
        check(c.getMethod("getBody") != null, "getBody");
        check(Modifier.isAbstract(c.getMethod("getHeaders").getModifiers()), "getHeaders abstract");
        check(Modifier.isAbstract(c.getMethod("getBody").getModifiers()), "getBody abstract");
        check(c.getMethod("getHeaders").getReturnType() == Map.class, "getHeaders -> Map");
        check(c.getMethod("getBody").getReturnType() == java.io.InputStream.class, "getBody -> InputStream");
        for (Class<?> t : c.getMethod("getHeaders").getExceptionTypes())
            check(t == java.io.IOException.class, "getHeaders declares IOException only");
        for (Class<?> t : c.getMethod("getBody").getExceptionTypes())
            check(t == java.io.IOException.class, "getBody declares IOException only");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        check(names.size() == 2, "exactly 2 declared public methods");
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
        final byte[] payload = "body".getBytes("UTF-8");
        final Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Content-Type", Collections.singletonList("text/plain"));
        CacheResponse resp = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return headers; }
            public InputStream getBody() { return new ByteArrayInputStream(payload); }
        };
        check(resp.getHeaders() == headers, "getHeaders");
        check(resp.getHeaders().get("Content-Type").get(0).equals("text/plain"), "header value");
        byte[] read = readAll(resp.getBody());
        check(Arrays.equals(read, payload), "getBody bytes");
    }

    // ===== deepened contract tests (batch D) =====

    static void testBodyStreamSemantics() throws Exception {
        final byte[] payload = {1, 2, 3, 4, 5};
        // independent stream per call: each getBody starts at offset 0
        CacheResponse resp = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(payload); }
        };
        InputStream s1 = resp.getBody();
        InputStream s2 = resp.getBody();
        check(s1 != s2, "getBody returns fresh stream per call");
        check(s1.read() == 1, "first stream at start");
        check(s2.read() == 1, "second stream independent of first");
        // full round-trip via Java-8-safe read
        byte[] all = readAll(resp.getBody());
        check(Arrays.equals(all, payload), "full-read round-trip");
        // exhausted stream reads -1
        InputStream ex = resp.getBody();
        while (ex.read() != -1) { }
        check(ex.read() == -1, "exhausted stream stays at EOF");
        // empty body
        CacheResponse empty = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(empty.getBody().read() == -1, "empty body immediate EOF");
        check(readAll(empty.getBody()).length == 0, "empty body full-read");
        // throwing subclass: IOException propagates per declared contract
        CacheResponse failing = new CacheResponse() {
            public Map<String, List<String>> getHeaders() throws IOException { throw new IOException("hdr"); }
            public InputStream getBody() throws IOException { throw new IOException("body"); }
        };
        try { failing.getHeaders(); check(false, "getHeaders IOException should propagate"); }
        catch (IOException e) { check(e.getMessage().equals("hdr"), "getHeaders IOException propagated"); }
        try { failing.getBody(); check(false, "getBody IOException should propagate"); }
        catch (IOException e) { check(e.getMessage().equals("body"), "getBody IOException propagated"); }
    }

    static void testHeaderSemantics() throws Exception {
        // multi-valued headers are representable
        final Map<String, List<String>> multi = new HashMap<String, List<String>>();
        multi.put("Set-Cookie", Arrays.asList("a=1", "b=2"));
        multi.put("X-Empty", Collections.<String>emptyList());
        multi.put("X-Null", null);
        CacheResponse resp = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return multi; }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        Map<String, List<String>> got = resp.getHeaders();
        check(got == multi, "getHeaders returns exact map");
        check(got.get("Set-Cookie").size() == 2, "multi-valued header preserved");
        check(got.get("X-Empty").isEmpty(), "empty list value preserved");
        check(got.get("X-Null") == null, "null value preserved");
        check(got.containsKey("X-Null"), "null-valued key present");
        // immutable headers snapshot also fine
        final Map<String, List<String>> fixed = Collections.unmodifiableMap(
            Collections.<String, List<String>>singletonMap("K", Collections.singletonList("V")));
        CacheResponse imm = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return fixed; }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(imm.getHeaders() == fixed, "unmodifiable headers pass-through");
    }

    static void testObjectContracts() throws Exception {
        CacheResponse r = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(r.equals(r), "identity equals");
        check(r.hashCode() == r.hashCode(), "hashCode stable");
        check(r.toString() != null && !r.toString().isEmpty(), "toString non-empty");
        CacheResponse other = new CacheResponse() {
            public Map<String, List<String>> getHeaders() { return Collections.emptyMap(); }
            public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        };
        check(!r.equals(other), "Object identity equals across instances");
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testBlackBox();
        testBodyStreamSemantics();
        testHeaderSemantics();
        testObjectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
