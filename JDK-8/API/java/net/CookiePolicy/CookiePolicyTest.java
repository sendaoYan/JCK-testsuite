/*
 * @test
 * @summary java.net.CookiePolicy conformance: interface hierarchy and members.
 * @run main CookiePolicyTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookiePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===== original assertions preserved (adapted to imported names) =====
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("java.net.CookiePolicy");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("shouldAccept"), "has method shouldAccept");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookiePolicy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getMethod("shouldAccept", URI.class, HttpCookie.class).getModifiers()),
            "shouldAccept abstract");
        for (String f : new String[] {"ACCEPT_ALL", "ACCEPT_NONE", "ACCEPT_ORIGINAL_SERVER"}) {
            Field fld = c.getField(f);
            check(Modifier.isStatic(fld.getModifiers()) && Modifier.isFinal(fld.getModifiers()), f + " static final");
            check(fld.getType() == c, f + " typed CookiePolicy");
            check(fld.get(null) != null, f + " non-null");
        }
        int abstractCount = 0;
        for (Method m : c.getMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 1, "single abstract method");
    }

    static void testAcceptAll() {
        URI u = URI.create("http://example.com/");
        HttpCookie c = new HttpCookie("a", "b");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(u, c), "ALL accept");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(null, c), "ALL null uri");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(u, null), "ALL null cookie");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(null, null), "ALL both null");
        HttpCookie foreign = new HttpCookie("a", "b"); foreign.setDomain("unrelated.org");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(u, foreign), "ALL foreign domain");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(URI.create("mailto:x@y"), c), "ALL non-http uri");
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_NONE, "distinct ALL/NONE");
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_ORIGINAL_SERVER, "distinct ALL/ORIGINAL");
    }

    static void testAcceptNone() {
        URI u = URI.create("http://example.com/");
        HttpCookie c = new HttpCookie("a", "b"); c.setDomain("example.com");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(u, c), "NONE reject");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(null, c), "NONE null uri");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(u, null), "NONE null cookie");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(null, null), "NONE both null");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(URI.create("http://other.org/"), c), "NONE any pair");
    }

    static void testAcceptOriginalServer() {
        CookiePolicy p = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        URI u = URI.create("http://example.com/");
        HttpCookie noDomain = new HttpCookie("a", "b");
        check(!p.shouldAccept(u, noDomain), "OS null domain rejected");
        HttpCookie same = new HttpCookie("a", "b"); same.setDomain("example.com");
        check(p.shouldAccept(u, same), "OS exact domain");
        HttpCookie dot = new HttpCookie("a", "b"); dot.setDomain(".example.com");
        check(p.shouldAccept(URI.create("http://www.example.com/"), dot), "OS dot subdomain");
        check(p.shouldAccept(u, dot), "OS dot apex");
        check(!p.shouldAccept(URI.create("http://a.b.example.com/"), dot), "OS deep subdomain rejected");
        HttpCookie deep = new HttpCookie("a", "b"); deep.setDomain(".b.example.com");
        check(p.shouldAccept(URI.create("http://a.b.example.com/"), deep), "OS matching-level subdomain");
        HttpCookie od = new HttpCookie("a", "b"); od.setDomain("other.com");
        check(!p.shouldAccept(u, od), "OS different domain");
        check(!p.shouldAccept(URI.create("http://example.org/"), same), "OS different host");
        HttpCookie ex = new HttpCookie("a", "b"); ex.setDomain(".example.com");
        check(!p.shouldAccept(URI.create("http://notexample.com/"), ex), "OS suffix-only host");
        HttpCookie up = new HttpCookie("a", "b"); up.setDomain("EXAMPLE.com");
        check(p.shouldAccept(u, up), "OS upper domain (setDomain lowercases)");
        check(p.shouldAccept(URI.create("http://EXAMPLE.com/"), same), "OS upper uri host");
        check(!p.shouldAccept(null, same), "OS null uri");
        check(!p.shouldAccept(u, null), "OS null cookie");
        check(!p.shouldAccept(null, null), "OS both null");
        check(!p.shouldAccept(URI.create("mailto:x@y"), same), "OS mailto uri");
        check(!p.shouldAccept(URI.create("relative/path"), same), "OS relative uri");
        check(p.shouldAccept(URI.create("https://example.com/"), same), "OS https uri");
    }

    static void testCustomImplementation() throws Exception {
        final List<String> calls = new ArrayList<String>();
        CookiePolicy rec = new CookiePolicy() {
            public boolean shouldAccept(URI uri, HttpCookie cookie) {
                calls.add(uri + "|" + (cookie == null ? null : cookie.getName()));
                return true;
            }
        };
        URI u = URI.create("http://example.com/");
        HttpCookie c = new HttpCookie("z", "v"); c.setDomain("example.com");
        check(rec.shouldAccept(u, c), "custom returns its value");
        check(calls.size() == 1, "custom called once");
        check(calls.get(0).equals("http://example.com/|z"), "custom sees exact args");
        check(rec.shouldAccept(null, null), "custom null pair");
        check(calls.get(1).equals("null|null"), "custom null pair recorded");
        CookieManager cm = new CookieManager(null, rec);
        Map<String, List<String>> h = new HashMap<String, List<String>>();
        h.put("Set-Cookie", Collections.singletonList("q=1; Domain=example.com"));
        cm.put(u, h);
        check(cm.getCookieStore().getCookies().size() == 1, "custom accept policy used by manager");
        check(calls.size() == 3, "policy consulted exactly once per put cookie");
        CookieManager cm2 = new CookieManager(null, new CookiePolicy() {
            public boolean shouldAccept(URI uri, HttpCookie cookie) { return false; }
        });
        cm2.put(u, h);
        check(cm2.getCookieStore().getCookies().isEmpty(), "custom veto policy blocks storage");
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testAcceptAll();
        testAcceptNone();
        testAcceptOriginalServer();
        testCustomImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
