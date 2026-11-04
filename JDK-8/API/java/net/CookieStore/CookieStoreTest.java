/*
 * @test
 * @summary java.net.CookieStore conformance: interface hierarchy and members.
 * @run main CookieStoreTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class CookieStoreTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Th { void run() throws Throwable; }
    static void ex(Th r, String type, String msg, String label) {
        try { r.run(); throw new AssertionError(label + ": no exception thrown"); }
        catch (Throwable t) {
            if (!type.equals(t.getClass().getName()))
                throw new AssertionError(label + ": wrong type " + t);
            if (msg != null && !msg.equals(t.getMessage()))
                throw new AssertionError(label + ": wrong msg " + t.getMessage());
            passed++;
        }
    }

    // ===== original assertions preserved (adapted to imported names) =====
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.CookieStore");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("add"), "has method add");
        check(names.contains("get"), "has method get");
        check(names.contains("getCookies"), "has method getCookies");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookieStore");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getMethod("getURIs") != null, "getURIs");
        check(c.getMethod("remove", URI.class, HttpCookie.class) != null, "remove");
        check(c.getMethod("removeAll") != null, "removeAll");
        // exact contract signature table (JDK-8 == JDK-27)
        check(c.getMethod("add", URI.class, HttpCookie.class).getReturnType() == void.class, "add void");
        check(c.getMethod("get", URI.class).getReturnType() == List.class, "get -> List");
        check(c.getMethod("getCookies").getReturnType() == List.class, "getCookies -> List");
        check(c.getMethod("getURIs").getReturnType() == List.class, "getURIs -> List");
        check(c.getMethod("remove", URI.class, HttpCookie.class).getReturnType() == boolean.class, "remove boolean");
        check(c.getMethod("removeAll").getReturnType() == boolean.class, "removeAll boolean");
        for (Method m : c.getMethods()) check(Modifier.isAbstract(m.getModifiers()), "all methods abstract");
    }

    static void testBlackBox() {
        CookieStore cs = new CookieManager().getCookieStore();
        check(cs != null, "default store non-null");
        check(cs.getClass().getName().equals("java.net.InMemoryCookieStore"), "InMemoryCookieStore default");
        URI u = URI.create("http://example.com");
        HttpCookie c = new HttpCookie("a", "b");

        check(cs.getCookies().isEmpty(), "initially empty");
        cs.add(u, c);
        check(cs.get(u).contains(c), "get(uri) returns cookie");
        check(cs.getCookies().contains(c), "getCookies returns cookie");
        check(cs.getURIs().contains(u), "getURIs returns uri");

        check(cs.remove(u, c), "remove returns true");
        check(!cs.getCookies().contains(c), "cookie removed");

        cs.add(u, c);
        cs.add(u, new HttpCookie("x", "y"));
        check(cs.getCookies().size() == 2, "two cookies stored");
        check(cs.removeAll(), "removeAll true");
        check(cs.getCookies().isEmpty(), "removeAll empties store");
        check(cs.getURIs().isEmpty(), "removeAll clears uris");
    }

    // ===== deepened contract tests (batch D, probe-verified on JDK-8) =====

    static void testAddNulls() {
        // add(uri, null) -> NPE "cookie is null"
        ex(() -> new CookieManager().getCookieStore().add(URI.create("http://x.com/"), null),
            "java.lang.NullPointerException", "cookie is null", "add null cookie");
        // add(null, cookie) is legal: cookie with no associated URI
        CookieStore cs = new CookieManager().getCookieStore();
        HttpCookie orphan = new HttpCookie("o", "1"); orphan.setDomain("example.com"); orphan.setPath("/");
        cs.add(null, orphan);
        check(cs.getCookies().size() == 1, "orphan cookie stored");
        check(cs.getURIs().isEmpty(), "orphan has no uri");
        // get(null) -> NPE "uri is null"
        ex(() -> new CookieManager().getCookieStore().get(null),
            "java.lang.NullPointerException", "uri is null", "get null uri");
    }

    static void testDomainMatchingVisibility() {
        CookieStore cs = new CookieManager().getCookieStore();
        HttpCookie k = new HttpCookie("k", "v"); k.setDomain(".example.com"); k.setPath("/");
        cs.add(URI.create("http://www.example.com/"), k);
        check(cs.get(URI.create("http://www.example.com/")).contains(k), "visible to adding host");
        check(cs.get(URI.create("http://example.com/")).contains(k), "visible to apex host");
        check(cs.get(URI.create("http://other.com/")).isEmpty(), "invisible to other host");
        check(cs.get(URI.create("http://www.other.org/")).isEmpty(), "invisible to other subdomain");
        check(cs.get(URI.create("http://WWW.EXAMPLE.COM/")).contains(k), "visible to upper-case host");
        // the store layer does NOT path-filter: path filtering is CookieManager.get's job
        HttpCookie deep = new HttpCookie("d", "v"); deep.setDomain(".example.com"); deep.setPath("/deep");
        cs.add(URI.create("http://www.example.com/deep/x"), deep);
        check(cs.get(URI.create("http://www.example.com/deep/y")).contains(deep), "store: path prefix visible");
        check(cs.get(URI.create("http://www.example.com/shallow")).contains(deep), "store: other path still visible");
        check(cs.get(URI.create("http://www.example.com/")).contains(deep), "store: root path visible");
        check(new CookieManager().getCookieStore().get(URI.create("http://none.com/")).isEmpty(), "empty for unknown uri");
    }

    static void testUnmodifiableViews() {
        CookieStore cs = new CookieManager().getCookieStore();
        HttpCookie c = new HttpCookie("a", "b"); c.setDomain("example.com"); c.setPath("/");
        cs.add(URI.create("http://example.com/"), c);
        ex(() -> cs.getCookies().add(new HttpCookie("z", "z")), "java.lang.UnsupportedOperationException",
            null, "getCookies add");
        ex(() -> cs.getCookies().remove(c), "java.lang.UnsupportedOperationException",
            null, "getCookies remove");
        // JDK-8 divergence: getURIs() and get(uri) return mutable snapshots;
        // mutating them must not affect the store (JDK-27 wraps them unmodifiable)
        cs.getURIs().add(URI.create("http://zz.com/"));
        check(cs.getURIs().size() == 1, "getURIs mutation is a local snapshot");
        cs.get(URI.create("http://example.com/")).add(new HttpCookie("q", "q"));
        check(cs.getCookies().size() == 1, "get(uri) mutation is a local snapshot");
        check(cs.get(URI.create("http://zz.com/")).isEmpty(), "snapshot uri not really stored");
    }

    static void testRemoveSemantics() {
        CookieStore cs = new CookieManager().getCookieStore();
        URI u = URI.create("http://example.com/");
        HttpCookie k = new HttpCookie("k", "v"); k.setDomain("example.com"); k.setPath("/");
        cs.add(u, k);
        check(!cs.remove(u, new HttpCookie("nope", "x")), "remove absent cookie false");
        ex(() -> cs.remove(u, null), "java.lang.NullPointerException", "cookie is null", "remove null cookie");
        HttpCookie sameKey = new HttpCookie("k", "w"); sameKey.setDomain("example.com"); sameKey.setPath("/");
        check(cs.remove(URI.create("http://unrelated.org/"), sameKey), "remove matches cookie regardless of uri arg");
        check(cs.getCookies().isEmpty(), "cookie gone after remove");
        check(!cs.remove(u, sameKey), "remove on emptied store false");
        CookieStore cs2 = new CookieManager().getCookieStore();
        HttpCookie orphan = new HttpCookie("o", "1"); orphan.setDomain("example.com"); orphan.setPath("/");
        cs2.add(null, orphan);
        HttpCookie same2 = new HttpCookie("o", "2"); same2.setDomain("example.com"); same2.setPath("/");
        check(cs2.remove(null, same2), "remove orphan via null uri");
        check(cs2.getCookies().isEmpty(), "orphan removed");
    }

    static void testRemoveAllSemantics() {
        CookieStore cs = new CookieManager().getCookieStore();
        check(!cs.removeAll(), "removeAll on empty store false");
        HttpCookie a = new HttpCookie("a", "1"); a.setDomain("example.com"); a.setPath("/");
        cs.add(URI.create("http://example.com/"), a);
        HttpCookie b = new HttpCookie("b", "2"); b.setDomain("other.org"); b.setPath("/");
        cs.add(URI.create("http://other.org/"), b);
        check(cs.getCookies().size() == 2, "two cookies before removeAll");
        check(cs.removeAll(), "removeAll non-empty true");
        check(cs.getCookies().isEmpty() && cs.getURIs().isEmpty(), "store emptied");
        check(!cs.removeAll(), "second removeAll false");
    }

    static void testReplaceAndDedup() {
        CookieStore cs = new CookieManager().getCookieStore();
        URI u = URI.create("http://example.com/");
        HttpCookie one = new HttpCookie("d", "1"); one.setDomain("example.com"); one.setPath("/");
        HttpCookie two = new HttpCookie("d", "2"); two.setDomain("example.com"); two.setPath("/");
        cs.add(u, one);
        cs.add(u, two);
        check(cs.getCookies().size() == 1, "identity dedup keeps one cookie");
        check(cs.getCookies().get(0).getValue().equals("2"), "last value wins");
        CookieStore cs2 = new CookieManager().getCookieStore();
        HttpCookie s = new HttpCookie("s", "1"); s.setDomain("example.com"); s.setPath("/");
        cs2.add(URI.create("http://example.com/a"), s);
        cs2.add(URI.create("http://example.com/b"), s);
        check(cs2.getCookies().size() == 1, "same cookie two uris stored once");
        check(cs2.getURIs().size() == 1, "one uri tracked for same cookie");
        HttpCookie other = new HttpCookie("s", "1"); other.setDomain("example.com"); other.setPath("/p");
        cs2.add(URI.create("http://example.com/"), other);
        check(cs2.getCookies().size() == 2, "different path distinct identity");
    }

    static void testExpiredCookies() {
        CookieStore cs = new CookieManager().getCookieStore();
        HttpCookie dead = new HttpCookie("e", "1"); dead.setDomain("example.com"); dead.setPath("/");
        dead.setMaxAge(0);
        cs.add(URI.create("http://example.com/"), dead);
        check(cs.getCookies().isEmpty(), "expired cookie not stored");
        CookieStore cs2 = new CookieManager().getCookieStore();
        HttpCookie live = new HttpCookie("l", "1"); live.setDomain("example.com"); live.setPath("/");
        cs2.add(URI.create("http://example.com/"), live);
        live.setMaxAge(0);
        check(cs2.getCookies().isEmpty(), "expired-in-place cookie filtered out");
    }

    static void testEqualsInteroperability() {
        CookieStore cs = new CookieManager().getCookieStore();
        HttpCookie k = new HttpCookie("k", "v"); k.setDomain("example.com"); k.setPath("/");
        cs.add(URI.create("http://example.com/"), k);
        HttpCookie probe = new HttpCookie("k", "other"); probe.setDomain("EXAMPLE.com".toLowerCase());
        probe.setPath("/");
        check(cs.getCookies().contains(probe), "contains via equals identity");
        check(cs.get(URI.create("http://example.com/")).contains(probe), "get(uri) contains via equals");
        check(cs.remove(URI.create("http://example.com/"), probe), "remove via equals identity");
        check(cs.getCookies().isEmpty(), "removed via equals proxy");
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testBlackBox();
        testAddNulls();
        testDomainMatchingVisibility();
        testUnmodifiableViews();
        testRemoveSemantics();
        testRemoveAllSemantics();
        testReplaceAndDedup();
        testExpiredCookies();
        testEqualsInteroperability();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
