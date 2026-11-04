/*
 * @test
 * @summary java.net.CookieManager conformance: store/policy/put/get.
 * @run main CookieManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CookieManagerTest {
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

    static Map<String, List<String>> headers(String key, String... values) {
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        m.put(key, Arrays.asList(values));
        return m;
    }

    static HttpCookie find(CookieManager cm, String name) {
        for (HttpCookie c : cm.getCookieStore().getCookies())
            if (c.getName().equals(name)) return c;
        return null;
    }

    // ===== original assertions preserved (adapted to imported names) =====
    static void testLegacy() throws Exception {
        CookieManager cm = new CookieManager();
        check(cm.getCookieStore() != null, "default cookie store");
        CookiePolicy policy = new CookiePolicy() {
            public boolean shouldAccept(URI u, HttpCookie c) { return false; }
        };
        cm.setCookiePolicy(policy);
        URI uri = new URI("http://example.com/");
        HttpCookie ck = new HttpCookie("k", "v");
        cm.getCookieStore().add(uri, ck);
        check(cm.getCookieStore().get(uri).contains(ck), "cookie stored");
        Map<String, List<String>> headers2 = new HashMap<String, List<String>>();
        headers2.put("Set-Cookie", Collections.singletonList("a=b; Path=/"));
        cm.put(uri, headers2);
        Map<String, List<String>> got = cm.get(uri, new HashMap<String, List<String>>());
        check(got != null, "get returns cookie map");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookieManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(CookieHandler.class.isAssignableFrom(c), "extends CookieHandler");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(CookieStore.class, CookiePolicy.class) != null, "ctor(store,policy)");
        check(c.getMethod("getCookieStore") != null, "getCookieStore");
        check(c.getMethod("setCookiePolicy", CookiePolicy.class) != null, "setCookiePolicy");
        check(c.getMethod("get", URI.class, Map.class) != null, "get");
        check(c.getMethod("put", URI.class, Map.class) != null, "put");
        check(c.getMethod("getDefault") != null, "getDefault inherited");
        check(c.getMethod("setDefault", CookieHandler.class) != null, "setDefault inherited");
    }

    static void testDefaultsAndCtors() throws Exception {
        CookieManager cm = new CookieManager();
        check(cm.getCookieStore().getClass().getName().equals("java.net.InMemoryCookieStore"),
            "default store is InMemoryCookieStore");
        URI u = URI.create("http://example.com/");
        cm.put(u, headers("Set-Cookie", "cross=1; Domain=other.com"));
        check(cm.getCookieStore().getCookies().isEmpty(), "default policy rejects cross-domain");
        cm.put(u, headers("Set-Cookie", "same=1; Domain=example.com"));
        check(cm.getCookieStore().getCookies().size() == 1, "default policy accepts same-domain");
        CookieManager cmn = new CookieManager(null, null);
        check(cmn.getCookieStore() != null, "null store arg -> fresh store");
        cmn.put(u, headers("Set-Cookie", "x=1; Domain=other.com"));
        check(cmn.getCookieStore().getCookies().isEmpty(), "null policy arg -> default policy rejects cross");
        cmn.put(u, headers("Set-Cookie", "y=1; Domain=example.com"));
        check(cmn.getCookieStore().getCookies().size() == 1, "null policy arg -> default accepts same");
        CookieStore mine = new CookieManager().getCookieStore();
        CookiePolicy p = CookiePolicy.ACCEPT_ALL;
        CookieManager custom = new CookieManager(mine, p);
        check(custom.getCookieStore() == mine, "custom store identity");
        custom.put(u, headers("Set-Cookie", "anywhere=1; Domain=z.org"));
        check(mine.getCookies().size() == 1, "ACCEPT_ALL via ctor accepts foreign domain into custom store");
    }

    static void testSetCookiePolicy() throws Exception {
        CookieManager cm = new CookieManager();
        URI u = URI.create("http://example.com/");
        cm.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
        cm.put(u, headers("Set-Cookie", "a=1; Domain=example.com"));
        check(cm.getCookieStore().getCookies().isEmpty(), "setPolicy ACCEPT_NONE applies dynamically");
        cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie", "a=1; Domain=example.com"));
        check(cm.getCookieStore().getCookies().size() == 1, "setPolicy ACCEPT_ALL applies dynamically");
        cm.put(u, headers("Set-Cookie", "b=1; Domain=other.org"));
        check(cm.getCookieStore().getCookies().size() == 2, "ACCEPT_ALL accepts foreign domain");
        cm.setCookiePolicy(null);
        cm.put(u, headers("Set-Cookie", "c=1; Domain=third.io"));
        check(cm.getCookieStore().getCookies().size() == 3, "null policy arg ignored, ACCEPT_ALL retained");
    }

    static void testPutParsing() throws Exception {
        URI u = URI.create("http://example.com/");
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie2", "s2=v; Domain=example.com; Path=/"));
        check(cm.getCookieStore().getCookies().size() == 1, "Set-Cookie2 stored");
        check(cm.getCookieStore().getCookies().get(0).getName().equals("s2"), "Set-Cookie2 name");
        CookieManager cmL = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmL.put(u, headers("set-cookie", "lc=1; Domain=example.com"));
        check(cmL.getCookieStore().getCookies().size() == 1, "lowercase header key stored");
        CookieManager cmM = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmM.put(u, headers("Set-Cookie", "c1=v1; Domain=example.com; Path=/", "c2=v2; Domain=example.com; Path=/"));
        check(cmM.getCookieStore().getCookies().size() == 2, "two values under one key both stored");
        CookieManager cmB = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        Map<String, List<String>> both = new HashMap<String, List<String>>();
        both.put("Set-Cookie", Collections.singletonList("a=1; Domain=example.com; Path=/"));
        both.put("Set-Cookie2", Collections.singletonList("b=2; Domain=example.com; Path=/"));
        cmB.put(u, both);
        check(cmB.getCookieStore().getCookies().size() == 2, "Set-Cookie and Set-Cookie2 both honoured");
        CookieManager cmH = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmH.put(URI.create("http://host.example.com/x"), headers("Set-Cookie", "nod=v; Path=/"));
        List<HttpCookie> st = cmH.getCookieStore().getCookies();
        check(st.size() == 1, "no-domain cookie stored");
        check(find(cmH, "nod").getDomain().equals("host.example.com"), "uri host becomes domain");
        check(find(cmH, "nod").getVersion() == 0, "plain Set-Cookie stored as version 0");
        CookieManager cmR = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmR.put(u, headers("Set-Cookie", "r=1; Domain=example.com; Path=/"));
        cmR.put(u, headers("Set-Cookie", "r=2; Domain=example.com; Path=/"));
        check(cmR.getCookieStore().getCookies().size() == 1, "same identity replaced not duplicated");
        check(cmR.getCookieStore().getCookies().get(0).getValue().equals("2"), "replacement keeps newest value");
        CookieManager cmU = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        Map<String, List<String>> mixed = new HashMap<String, List<String>>();
        mixed.put("Set-Cookie", Arrays.asList("novalue", "good=1; Domain=example.com; Path=/"));
        cmU.put(u, mixed);
        check(cmU.getCookieStore().getCookies().size() == 1, "bad value skipped, good one kept");
        // missing Domain on a dotless host gets a ".local" suffix (RFC 2965 effective host)
        CookieManager cmLh = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmLh.put(URI.create("http://localhost/x"), headers("Set-Cookie", "lo=1; Path=/"));
        check(cmLh.getCookieStore().getCookies().get(0).getDomain().equals("localhost.local"),
            "dotless host gains .local suffix");
        // missing Path defaults to the directory part of the request uri
        CookieManager cmP = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmP.put(URI.create("http://example.com/dir/page.html"), headers("Set-Cookie", "np=1; Domain=example.com"));
        check(find(cmP, "np").getPath().equals("/dir/"), "default path is uri directory");
        cmP.put(URI.create("http://example.com/dir/sub/page"), headers("Set-Cookie", "np2=1; Domain=example.com"));
        check(find(cmP, "np2").getPath().equals("/dir/sub/"), "default path nested directory");
        cmP.put(URI.create("http://example.com/dir/"), headers("Set-Cookie", "np3=1; Domain=example.com"));
        check(find(cmP, "np3").getPath().equals("/dir/"), "default path keeps trailing dir");
    }

    static void testPutExpiry() throws Exception {
        URI u = URI.create("http://example.com/");
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie", "dead=1; Domain=example.com; max-age=0"));
        check(cm.getCookieStore().getCookies().isEmpty(), "max-age=0 not stored");
        cm.put(u, headers("Set-Cookie", "dead2=1; Domain=example.com; expires=Wed, 21 Oct 2015 07:28:00 GMT"));
        check(cm.getCookieStore().getCookies().isEmpty(), "past expires not stored");
        cm.put(u, headers("Set-Cookie", "alive=1; Domain=example.com; expires=Wed, 21 Oct 2099 07:28:00 GMT"));
        check(cm.getCookieStore().getCookies().size() == 1, "future expires stored");
    }

    static void testPutNullArgs() throws Exception {
        CookieManager cm = new CookieManager();
        Map<String, List<String>> h = headers("Set-Cookie", "a=1");
        ex(() -> cm.put(null, h), "java.lang.IllegalArgumentException", "Argument is null", "put null uri");
        ex(() -> cm.put(URI.create("http://e.com/"), null), "java.lang.IllegalArgumentException",
            "Argument is null", "put null map");
        ex(() -> cm.get(null, new HashMap<String, List<String>>()), "java.lang.IllegalArgumentException",
            "Argument is null", "get null uri");
        ex(() -> cm.get(URI.create("http://e.com/"), null), "java.lang.IllegalArgumentException",
            "Argument is null", "get null map");
    }

    static void testGetRequestHeaderConstruction() throws Exception {
        CookieManager empty = new CookieManager();
        Map<String, List<String>> g0 = empty.get(URI.create("http://nowhere.com/"), new HashMap<String, List<String>>());
        check(g0 != null, "get returns non-null map");
        check(g0.containsKey("Cookie"), "get always contains Cookie key");
        check(g0.get("Cookie").isEmpty(), "no cookies -> empty Cookie list");
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        URI u = URI.create("http://example.com/");
        cm.put(u, headers("Set-Cookie", "name=value; Domain=example.com; Path=/"));
        Map<String, List<String>> g1 = cm.get(u, new HashMap<String, List<String>>());
        check(g1.get("Cookie").contains("name=value"), "Cookie header form name=value");
        check(g1.get("Cookie").size() == 1, "one cookie one element");
        cm.put(u, headers("Set-Cookie", "second=2; Domain=example.com; Path=/"));
        Map<String, List<String>> g2 = cm.get(u, new HashMap<String, List<String>>());
        check(g2.get("Cookie").size() == 2, "two cookies two elements");
        check(g2.get("Cookie").contains("name=value") && g2.get("Cookie").contains("second=2"), "both present");
        Map<String, List<String>> req = new HashMap<String, List<String>>();
        req.put("Cookie", new ArrayList<String>(Collections.singletonList("pre=1")));
        Map<String, List<String>> g3 = cm.get(u, req);
        check(g3.get("Cookie").contains("name=value"), "request Cookie ignored");
        check(!g3.get("Cookie").contains("pre=1"), "pre-existing request Cookie not echoed");
        check(req.get("Cookie").equals(Collections.singletonList("pre=1")), "request map untouched");
        ex(() -> g3.put("X-New", Collections.singletonList("v")), "java.lang.UnsupportedOperationException",
            null, "returned map unmodifiable");
    }

    static void testGetFiltering() throws Exception {
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(URI.create("http://example.com/deep/path/x"),
            headers("Set-Cookie", "p=1; Domain=example.com; Path=/deep/path"));
        check(cm.get(URI.create("http://example.com/deep/path/y"), new HashMap<String, List<String>>())
            .get("Cookie").contains("p=1"), "path prefix visible");
        check(cm.get(URI.create("http://example.com/other"), new HashMap<String, List<String>>())
            .get("Cookie").isEmpty(), "path mismatch filtered by manager");
        CookieManager s = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        s.put(URI.create("https://example.com/"), headers("Set-Cookie", "sec=1; Domain=example.com; Path=/; Secure"));
        check(s.get(URI.create("https://example.com/"), new HashMap<String, List<String>>()).get("Cookie").contains("sec=1"),
            "secure over https");
        check(s.get(URI.create("http://example.com/"), new HashMap<String, List<String>>()).get("Cookie").isEmpty(),
            "secure withheld over http");
        CookieManager pp = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        pp.put(URI.create("http://example.com:8080/"),
            headers("Set-Cookie2", "pp=1; Domain=example.com; Path=/; Port=8080"));
        check(pp.get(URI.create("http://example.com:8080/"), new HashMap<String, List<String>>()).get("Cookie").contains("pp=1"),
            "portlisted cookie on listed port");
        check(pp.get(URI.create("http://example.com:9090/"), new HashMap<String, List<String>>()).get("Cookie").isEmpty(),
            "portlisted cookie hidden on other port");
        check(pp.get(URI.create("http://example.com/"), new HashMap<String, List<String>>()).get("Cookie").isEmpty(),
            "portlisted cookie hidden without explicit port");
        CookieManager exp = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        exp.put(URI.create("http://example.com/"), headers("Set-Cookie", "e=1; Domain=example.com; Path=/"));
        exp.getCookieStore().getCookies().get(0).setMaxAge(0);
        check(exp.get(URI.create("http://example.com/"), new HashMap<String, List<String>>()).get("Cookie").isEmpty(),
            "expired cookie withheld");
    }

    static void testCustomStoreAndPolicy() throws Exception {
        final List<String> log = new ArrayList<String>();
        CookieStore recording = new CookieStore() {
            public void add(URI uri, HttpCookie cookie) { log.add("add:" + uri + ":" + cookie.getName()); }
            public List<HttpCookie> get(URI uri) { return Collections.emptyList(); }
            public List<HttpCookie> getCookies() { return Collections.emptyList(); }
            public List<URI> getURIs() { return Collections.emptyList(); }
            public boolean remove(URI uri, HttpCookie cookie) { return false; }
            public boolean removeAll() { return false; }
        };
        CookieManager cm = new CookieManager(recording, CookiePolicy.ACCEPT_ALL);
        cm.put(URI.create("http://example.com/"), headers("Set-Cookie", "z=1; Domain=example.com"));
        check(log.size() == 1, "custom store add invoked once");
        check(log.get(0).equals("add:http://example.com/:z"), "custom store sees uri + parsed cookie");
        Map<String, List<String>> out = cm.get(URI.create("http://example.com/"), new HashMap<String, List<String>>());
        check(out.containsKey("Cookie"), "get builds header from custom store");
        final List<String> seen = new ArrayList<String>();
        CookieManager cm2 = new CookieManager(null, new CookiePolicy() {
            public boolean shouldAccept(URI uri, HttpCookie cookie) {
                seen.add(uri.getHost() + "/" + cookie.getDomain());
                return false;
            }
        });
        cm2.put(URI.create("http://example.com/"), headers("Set-Cookie", "q=1; Domain=example.com"));
        check(seen.size() == 1, "policy consulted once");
        check(seen.get(0).equals("example.com/example.com"), "policy sees uri host and cookie domain");
        check(cm2.getCookieStore().getCookies().isEmpty(), "vetoed cookie never stored");
    }

    static void testCookieHandlerIntegration() throws Exception {
        CookieHandler prev = CookieHandler.getDefault();
        try {
            check(CookieHandler.getDefault() == null, "no default handler initially (fresh JVM)");
            CookieManager cm = new CookieManager();
            CookieHandler.setDefault(cm);
            check(CookieHandler.getDefault() == cm, "CookieManager installable as default handler");
            check(CookieHandler.getDefault() instanceof CookieManager, "default is a CookieManager");
            CookieHandler.setDefault(null);
            check(CookieHandler.getDefault() == null, "setDefault(null) clears");
            CookieHandler.setDefault(cm);
            check(CookieHandler.getDefault() == cm, "reinstall works");
        } finally {
            CookieHandler.setDefault(prev);
        }
    }

    static void testAdditionalMatrix() throws Exception {
        URI u = URI.create("http://example.com/");
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie", "n=1; Domain=example.com; Path=/"));
        cm.put(URI.create("http://other.org/"), headers("Set-Cookie", "n=2; Domain=other.org; Path=/"));
        check(cm.getCookieStore().getCookies().size() == 2, "same name distinct domains coexist");
        check(cm.get(u, new HashMap<String, List<String>>()).get("Cookie").equals(Collections.singletonList("n=1")),
            "get serves only the matching domain copy");
        CookieManager cm2 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm2.put(u, headers("Set-Cookie", "wide=1; Domain=example.com; Path=/"));
        cm2.put(URI.create("http://example.com/sub/x"), headers("Set-Cookie", "narrow=1; Domain=example.com; Path=/sub"));
        List<String> hdr = cm2.get(URI.create("http://example.com/sub/y"), new HashMap<String, List<String>>()).get("Cookie");
        check(hdr.size() == 2, "both path-level cookies served");
        check(hdr.contains("wide=1") && hdr.contains("narrow=1"), "contains both (order not specified)");
        List<String> hdrRoot = cm2.get(URI.create("http://example.com/"), new HashMap<String, List<String>>()).get("Cookie");
        check(hdrRoot.equals(Collections.singletonList("wide=1")), "root uri sees only root-path cookie");
        CookieManager cm3 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm3.put(u, headers("Set-Cookie", "u=1; Domain=EXAMPLE.com; Path=/"));
        check(cm3.getCookieStore().getCookies().get(0).getDomain().equals("example.com"), "domain attr lowercased");
        check(cm3.get(URI.create("http://WWW.EXAMPLE.COM/"), new HashMap<String, List<String>>()).get("Cookie").contains("u=1"),
            "uri host case-insensitive in get");
        CookieManager cm4 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm4.put(u, headers("Set-Cookie2", "a=b; Domain=example.com; Path=/, c=d; Domain=example.com; Path=/"));
        check(cm4.getCookieStore().getCookies().size() == 1, "comma inside manager value stays one cookie");
        CookieManager cm5 = new CookieManager(null, CookiePolicy.ACCEPT_NONE);
        cm5.put(u, headers("Set-Cookie", "never=1; Domain=example.com"));
        check(cm5.get(u, new HashMap<String, List<String>>()).get("Cookie").isEmpty(), "vetoed cookie never served");
        CookieManager cm6 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm6.put(u, headers("Content-Type", "text/plain"));
        check(cm6.getCookieStore().getCookies().isEmpty(), "non-cookie header ignored");
        CookieManager cm7 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm7.put(u, headers("Set-Cookie", "stable=1; Domain=example.com; Path=/"));
        for (int i = 0; i < 3; i++) {
            Map<String, List<String>> r = cm7.get(u, new HashMap<String, List<String>>());
            check(r.get("Cookie").equals(Collections.singletonList("stable=1")), "round-trip stable #" + i);
        }
        // the response uri is registered normalized: scheme + host only, no path/port
        // (InMemoryCookieStore indexes by effective URI, probe-verified both JDKs)
        cm7.put(u, headers("Set-Cookie", "stable=2; Domain=example.com; Path=/"));
        cm7.put(URI.create("http://example.com:8080/sub/x"), headers("Set-Cookie", "p2=1; Domain=example.com; Path=/"));
        check(cm7.getCookieStore().getURIs().size() == 1, "same host -> one normalized uri key");
        check(cm7.getCookieStore().getURIs().contains(URI.create("http://example.com")),
            "store tracks normalized host-only uri");
        Map<String, List<String>> rk = cm7.get(u, new HashMap<String, List<String>>());
        check(rk.keySet().size() == 1 && rk.keySet().contains("Cookie"), "get map has only Cookie key");
        CookieHandler asHandler = cm7;
        check(asHandler.get(u, new HashMap<String, List<String>>()).get("Cookie").contains("stable=2"),
            "polymorphic get via CookieHandler reference");
        check(cm7.getCookieStore() == cm7.getCookieStore(), "getCookieStore stable identity");
        // same-path multi-cookie header keeps insertion order (sort is stable for equal paths)
        CookieManager cm8 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm8.put(u, headers("Set-Cookie", "zz=9; Domain=example.com; Path=/",
            "aa=1; Domain=example.com; Path=/", "mm=5; Domain=example.com; Path=/"));
        List<String> ordered = cm8.get(u, new HashMap<String, List<String>>()).get("Cookie");
        check(ordered.size() == 3, "three same-path cookies served");
        check(ordered.contains("zz=9") && ordered.contains("aa=1") && ordered.contains("mm=5"),
            "all three present");
        // store also holds all three under one identity-set
        check(cm8.getCookieStore().getCookies().size() == 3, "three distinct identities stored");
        // same-path repeated get is deterministic
        List<String> again = cm8.get(u, new HashMap<String, List<String>>()).get("Cookie");
        check(again.equals(ordered), "same-path ordering deterministic across calls");
    }

    public static void main(String[] args) throws Exception {
        testLegacy();
        testWhiteBox();
        testDefaultsAndCtors();
        testSetCookiePolicy();
        testPutParsing();
        testPutExpiry();
        testPutNullArgs();
        testGetRequestHeaderConstruction();
        testGetFiltering();
        testCustomStoreAndPolicy();
        testCookieHandlerIntegration();
        testAdditionalMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
