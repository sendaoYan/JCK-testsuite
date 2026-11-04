/*
 * @test
 * @summary java.net.CookiePolicy conformance: white-box structure + black-box behavior.
 * @run main CookiePolicyTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class CookiePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookiePolicy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("shouldAccept", URI.class, HttpCookie.class) != null, "shouldAccept");
        check(Modifier.isAbstract(c.getMethod("shouldAccept", URI.class, HttpCookie.class).getModifiers()),
            "shouldAccept abstract");
        check(c.getField("ACCEPT_ALL") != null, "ACCEPT_ALL");
        check(c.getField("ACCEPT_NONE") != null, "ACCEPT_NONE");
        check(c.getField("ACCEPT_ORIGINAL_SERVER") != null, "ACCEPT_ORIGINAL_SERVER");
        for (String f : new String[] {"ACCEPT_ALL", "ACCEPT_NONE", "ACCEPT_ORIGINAL_SERVER"}) {
            Field fld = c.getField(f);
            check(Modifier.isStatic(fld.getModifiers()) && Modifier.isFinal(fld.getModifiers()), f + " static final");
            check(fld.getType() == c, f + " typed CookiePolicy");
            check(fld.get(null) != null, f + " non-null");
        }
        // interface has exactly one abstract method (functional interface shape)
        int abstractCount = 0;
        for (Method m : c.getMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == 1, "single abstract method");
    }

    static void testBlackBox() throws Exception {
        URI u = URI.create("http://example.com/");
        HttpCookie c = new HttpCookie("a", "b");
        c.setDomain("example.com");

        check(CookiePolicy.ACCEPT_ALL.shouldAccept(u, c), "ACCEPT_ALL true");
        check(CookiePolicy.ACCEPT_ALL.shouldAccept(URI.create("http://other.org/"), c), "ACCEPT_ALL always true");
        check(!CookiePolicy.ACCEPT_NONE.shouldAccept(u, c), "ACCEPT_NONE false");

        check(CookiePolicy.ACCEPT_ORIGINAL_SERVER.shouldAccept(u, c), "ACCEPT_ORIGINAL_SERVER same host");
        HttpCookie other = new HttpCookie("a", "b");
        other.setDomain("example.com");
        check(!CookiePolicy.ACCEPT_ORIGINAL_SERVER.shouldAccept(URI.create("http://other.org/"), other),
            "ACCEPT_ORIGINAL_SERVER different host");

        // constants are distinct singletons
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_NONE, "distinct ALL/NONE");
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_ORIGINAL_SERVER, "distinct ALL/ORIGINAL");
    }

    // ===== deepened contract tests (batch D, probe-verified) =====

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
        // null-domain cookie is rejected (probe-verified)
        HttpCookie noDomain = new HttpCookie("a", "b");
        check(!p.shouldAccept(u, noDomain), "OS null domain rejected");
        // exact host match
        HttpCookie same = new HttpCookie("a", "b"); same.setDomain("example.com");
        check(p.shouldAccept(u, same), "OS exact domain");
        // dotted domain matches subdomain and apex
        HttpCookie dot = new HttpCookie("a", "b"); dot.setDomain(".example.com");
        check(p.shouldAccept(URI.create("http://www.example.com/"), dot), "OS dot subdomain");
        check(p.shouldAccept(u, dot), "OS dot apex");
        // deep subdomain is NOT matched by a 2-level dotted domain (probe-verified:
        // domainMatches only matches the apex and one level of subdomain)
        check(!p.shouldAccept(URI.create("http://a.b.example.com/"), dot), "OS deep subdomain rejected");
        HttpCookie deep = new HttpCookie("a", "b"); deep.setDomain(".b.example.com");
        check(p.shouldAccept(URI.create("http://a.b.example.com/"), deep), "OS matching-level subdomain");
        // different domain rejected
        HttpCookie od = new HttpCookie("a", "b"); od.setDomain("other.com");
        check(!p.shouldAccept(u, od), "OS different domain");
        check(!p.shouldAccept(URI.create("http://example.org/"), same), "OS different host");
        // suffix-only host rejected
        HttpCookie ex = new HttpCookie("a", "b"); ex.setDomain(".example.com");
        check(!p.shouldAccept(URI.create("http://notexample.com/"), ex), "OS suffix-only host");
        // case-insensitive domain/host comparison
        HttpCookie up = new HttpCookie("a", "b"); up.setDomain("EXAMPLE.com");
        check(p.shouldAccept(u, up), "OS upper domain (setDomain lowercases)");
        check(p.shouldAccept(URI.create("http://EXAMPLE.com/"), same), "OS upper uri host");
        // null args
        check(!p.shouldAccept(null, same), "OS null uri");
        check(!p.shouldAccept(u, null), "OS null cookie");
        check(!p.shouldAccept(null, null), "OS both null");
        // uri without host
        check(!p.shouldAccept(URI.create("mailto:x@y"), same), "OS mailto uri");
        check(!p.shouldAccept(URI.create("relative/path"), same), "OS relative uri");
        // https vs http makes no difference
        check(p.shouldAccept(URI.create("https://example.com/"), same), "OS https uri");
    }

    static void testCustomImplementation() throws java.io.IOException {
        final List<String> calls = new ArrayList<>();
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
        // custom false-deciding policy wired into a CookieManager
        CookieManager cm = new CookieManager(null, rec);
        Map<String, List<String>> h = new HashMap<>();
        h.put("Set-Cookie", Collections.singletonList("q=1; Domain=example.com"));
        cm.put(u, h);
        check(cm.getCookieStore().getCookies().size() == 1, "custom accept policy used by manager");
        check(calls.size() == 3, "policy consulted exactly once per put cookie");
        // vetoing policy
        CookieManager cm2 = new CookieManager(null, new CookiePolicy() {
            public boolean shouldAccept(URI uri, HttpCookie cookie) { return false; }
        });
        cm2.put(u, h);
        check(cm2.getCookieStore().getCookies().isEmpty(), "custom veto policy blocks storage");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAcceptAll();
        testAcceptNone();
        testAcceptOriginalServer();
        testCustomImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
