/*
 * @test
 * @summary java.net.CookieHandler conformance: white-box structure + black-box behavior.
 * @run main CookieHandlerTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class CookieHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookieHandler");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getConstructor().getModifiers()), "public no-arg ctor");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object directly");
        // exact public method set (Object methods excluded)
        Set<String> names = new HashSet<>();
        for (Method m : c.getMethods())
            if (m.getDeclaringClass() != Object.class) names.add(m.getName());
        check(names.contains("get"), "has method get");
        check(names.contains("getDefault"), "has method getDefault");
        check(names.contains("put"), "has method put");
        check(names.contains("setDefault"), "has method setDefault");
        check(names.size() == 4, "exactly 4 public methods (getDefault/setDefault/get/put)");
        // abstract members
        check(Modifier.isAbstract(c.getMethod("get", URI.class, Map.class).getModifiers()), "get is abstract");
        check(Modifier.isAbstract(c.getMethod("put", URI.class, Map.class).getModifiers()), "put is abstract");
        // get/put declare IOException
        boolean getThrows = false, putThrows = false;
        for (Class<?> t : c.getMethod("get", URI.class, Map.class).getExceptionTypes())
            if (t == java.io.IOException.class) getThrows = true;
        for (Class<?> t : c.getMethod("put", URI.class, Map.class).getExceptionTypes())
            if (t == java.io.IOException.class) putThrows = true;
        check(getThrows, "get declares IOException");
        check(putThrows, "put declares IOException");
        // static factory/installer pair
        check(Modifier.isStatic(c.getMethod("getDefault").getModifiers()), "getDefault static");
        check(Modifier.isStatic(c.getMethod("setDefault", CookieHandler.class).getModifiers()), "setDefault static");
        check(c.getMethod("setDefault", CookieHandler.class).getReturnType() == void.class, "setDefault void");
        // cannot instantiate the abstract class reflectively
        try {
            c.getDeclaredConstructor().newInstance();
            check(false, "newInstance should fail");
        } catch (InvocationTargetException e) {
            check(true, "newInstance wrapped failure");
        } catch (InstantiationException e) {
            check(true, "InstantiationException");
        }
    }

    static void testBlackBox() throws Exception {
        // default handler set/get roundtrip (restore previous state)
        CookieHandler prev = CookieHandler.getDefault();
        try {
            CookieManager cm = new CookieManager();
            CookieHandler.setDefault(cm);
            check(CookieHandler.getDefault() == cm, "setDefault/getDefault roundtrip");

            // custom subclass fulfilling the abstract contract
            final Map<URI, Map<String, List<String>>> seen = new HashMap<>();
            CookieHandler custom = new CookieHandler() {
                public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) {
                    return requestHeaders;
                }
                public void put(URI uri, Map<String, List<String>> responseHeaders) {
                    seen.put(uri, responseHeaders);
                }
            };
            CookieHandler.setDefault(custom);
            URI u = URI.create("http://example.com/");
            Map<String, List<String>> h = new HashMap<>();
            h.put("Set-Cookie", Collections.singletonList("a=b"));
            custom.put(u, h);
            check(seen.containsKey(u), "custom put recorded");
            check(custom.get(u, h) == h, "custom get passthrough");
        } finally {
            CookieHandler.setDefault(prev);
        }
    }

    // ===== deepened contract tests (batch D, probe-verified) =====

    static void testSubclassSemantics() throws Exception {
        final List<String> log = new ArrayList<>();
        final Map<String, List<String>> canned = new HashMap<>();
        canned.put("Cookie", Collections.singletonList("canned=v"));
        CookieHandler h = new CookieHandler() {
            public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) {
                log.add("get:" + uri + ":" + (requestHeaders == null));
                return canned;
            }
            public void put(URI uri, Map<String, List<String>> responseHeaders) {
                log.add("put:" + uri + ":" + (responseHeaders == null));
            }
        };
        URI u = URI.create("http://example.com/");
        Map<String, List<String>> req = new HashMap<>();
        check(h.get(u, req) == canned, "get returns exactly what subclass provides");
        check(h.get(u, req).get("Cookie").contains("canned=v"), "canned content readable");
        h.put(u, req);
        check(log.size() == 3, "both calls recorded");
        check(log.get(0).equals("get:http://example.com/:false"), "get receives exact uri + map");
        check(log.get(1).equals("get:http://example.com/:false"), "second get recorded");
        check(log.get(2).equals("put:http://example.com/:false"), "put receives exact uri + map");
        // subclass may throw IOException per the declared contract
        CookieHandler throwing = new CookieHandler() {
            public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders)
                throws java.io.IOException { throw new java.io.IOException("custom-get"); }
            public void put(URI uri, Map<String, List<String>> responseHeaders)
                throws java.io.IOException { throw new java.io.IOException("custom-put"); }
        };
        try { throwing.get(u, req); check(false, "get should propagate"); }
        catch (java.io.IOException e) { check(e.getMessage().equals("custom-get"), "get IOException propagated"); }
        try { throwing.put(u, req); check(false, "put should propagate"); }
        catch (java.io.IOException e) { check(e.getMessage().equals("custom-put"), "put IOException propagated"); }
    }

    static void testDefaultHandlerLifecycle() throws Exception {
        CookieHandler prev = CookieHandler.getDefault();
        try {
            check(CookieHandler.getDefault() == null, "fresh JVM has no default handler");
            CookieManager first = new CookieManager();
            CookieHandler.setDefault(first);
            check(CookieHandler.getDefault() == first, "installed handler retrievable");
            CookieManager second = new CookieManager();
            CookieHandler.setDefault(second);
            check(CookieHandler.getDefault() == second, "setDefault replaces");
            check(CookieHandler.getDefault() != first, "previous handler released");
            CookieHandler.setDefault(null);
            check(CookieHandler.getDefault() == null, "setDefault(null) clears default");
            // default is a JVM-global slot: two references see the same value
            CookieHandler a = CookieHandler.getDefault();
            CookieHandler b = CookieHandler.getDefault();
            check(a == b, "getDefault stable");
        } finally {
            CookieHandler.setDefault(prev);
        }
    }

    static void testHierarchy() {
        check(CookieManager.class.getSuperclass() == CookieHandler.class, "CookieManager direct subclass");
        CookieHandler cm = new CookieManager();
        check(cm instanceof CookieHandler, "instanceof CookieHandler");
        check(CookieHandler.class.isAssignableFrom(cm.getClass()), "isAssignableFrom");
        check(!CookieHandler.class.isInterface(), "class not interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSubclassSemantics();
        testDefaultHandlerLifecycle();
        testHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
