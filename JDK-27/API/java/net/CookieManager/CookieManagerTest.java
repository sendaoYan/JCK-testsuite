/*
 * @test
 * @summary java.net.CookieManager conformance: white-box structure + black-box behavior.
 * @run main CookieManagerTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

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
        Map<String, List<String>> m = new HashMap<>();
        m.put(key, Arrays.asList(values));
        return m;
    }

    static HttpCookie find(CookieManager cm, String name) {
        for (HttpCookie c : cm.getCookieStore().getCookies())
            if (c.getName().equals(name)) return c;
        return null;
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
        // inherited static default-handler plumbing comes from CookieHandler
        check(c.getMethod("getDefault") != null, "getDefault inherited");
        check(c.getMethod("setDefault", CookieHandler.class) != null, "setDefault inherited");
    }

    static void testBlackBox() throws Exception {
        CookieManager cm = new CookieManager();
        URI u = URI.create("http://example.com/path");

        Map<String, List<String>> header = new HashMap<>();
        header.put("Set-Cookie", Arrays.asList("name=value; Path=/"));
        cm.put(u, header);

        Map<String, List<String>> out = cm.get(u, new HashMap<>());
        check(out.containsKey("Cookie"), "get returns Cookie key");
        check(out.get("Cookie").contains("name=value"), "cookie value roundtrip");

        List<HttpCookie> stored = cm.getCookieStore().get(u);
        check(stored.size() == 1, "store has 1 cookie");
        check(stored.get(0).getName().equals("name"), "stored name");
        check(stored.get(0).getValue().equals("value"), "stored value");

        // default store is non-null and empty before any put
        CookieStore cs = new CookieManager().getCookieStore();
        check(cs != null, "store non-null");
        check(cs.getCookies().isEmpty(), "fresh store empty");

        // policy control: ACCEPT_NONE rejects everything
        CookieManager cm2 = new CookieManager();
        cm2.setCookiePolicy(CookiePolicy.ACCEPT_NONE);
        cm2.put(u, header);
        check(cm2.getCookieStore().getCookies().isEmpty(), "ACCEPT_NONE rejects");

        // custom store + policy constructor
        CookieManager cm3 = new CookieManager(cm.getCookieStore(), CookiePolicy.ACCEPT_ALL);
        check(cm3.getCookieStore() == cm.getCookieStore(), "custom store retained");
        check(cm3.getCookieStore().getCookies().size() == 1, "custom store shares cookies");
    }

    // ===== deepened contract tests (batch D, probe-verified) =====

    static void testDefaultsAndCtors() throws Exception {
        // default constructor: InMemoryCookieStore + ACCEPT_ORIGINAL_SERVER policy
        CookieManager cm = new CookieManager();
        check(cm.getCookieStore().getClass().getName().equals("java.net.InMemoryCookieStore"),
            "default store is InMemoryCookieStore");
        // policy is not exposed by a getter; verify behaviorally (default == ACCEPT_ORIGINAL_SERVER)
        URI u = URI.create("http://example.com/");
        cm.put(u, headers("Set-Cookie", "cross=1; Domain=other.com"));
        check(cm.getCookieStore().getCookies().isEmpty(), "default policy rejects cross-domain");
        cm.put(u, headers("Set-Cookie", "same=1; Domain=example.com"));
        check(cm.getCookieStore().getCookies().size() == 1, "default policy accepts same-domain");
        // ctor(null, null) -> fresh default store + default policy
        CookieManager cmn = new CookieManager(null, null);
        check(cmn.getCookieStore() != null, "null store arg -> fresh store");
        cmn.put(u, headers("Set-Cookie", "x=1; Domain=other.com"));
        check(cmn.getCookieStore().getCookies().isEmpty(), "null policy arg -> default policy rejects cross");
        cmn.put(u, headers("Set-Cookie", "y=1; Domain=example.com"));
        check(cmn.getCookieStore().getCookies().size() == 1, "null policy arg -> default accepts same");
        // custom store + policy are retained exactly
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
        // setCookiePolicy(null) is silently ignored, policy unchanged
        cm.setCookiePolicy(null);
        cm.put(u, headers("Set-Cookie", "c=1; Domain=third.io"));
        check(cm.getCookieStore().getCookies().size() == 3, "null policy arg ignored, ACCEPT_ALL retained");
    }

    static void testPutParsing() throws Exception {
        URI u = URI.create("http://example.com/");
        // Set-Cookie2 header is honoured too
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie2", "s2=v; Domain=example.com; Path=/"));
        check(cm.getCookieStore().getCookies().size() == 1, "Set-Cookie2 stored");
        check(cm.getCookieStore().getCookies().get(0).getName().equals("s2"), "Set-Cookie2 name");
        // header map key is case-insensitive
        CookieManager cmL = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmL.put(u, headers("set-cookie", "lc=1; Domain=example.com"));
        check(cmL.getCookieStore().getCookies().size() == 1, "lowercase header key stored");
        // multiple values under one key
        CookieManager cmM = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmM.put(u, headers("Set-Cookie", "c1=v1; Domain=example.com; Path=/", "c2=v2; Domain=example.com; Path=/"));
        check(cmM.getCookieStore().getCookies().size() == 2, "two values under one key both stored");
        // both header keys in the same map
        CookieManager cmB = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        Map<String, List<String>> both = new HashMap<>();
        both.put("Set-Cookie", Collections.singletonList("a=1; Domain=example.com; Path=/"));
        both.put("Set-Cookie2", Collections.singletonList("b=2; Domain=example.com; Path=/"));
        cmB.put(u, both);
        check(cmB.getCookieStore().getCookies().size() == 2, "Set-Cookie and Set-Cookie2 both honoured");
        // no Domain attribute -> uri host becomes the cookie domain
        CookieManager cmH = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmH.put(URI.create("http://host.example.com/x"), headers("Set-Cookie", "nod=v; Path=/"));
        List<HttpCookie> st = cmH.getCookieStore().getCookies();
        check(st.size() == 1, "no-domain cookie stored");
        check(st.get(0).getDomain().equals("host.example.com"), "uri host becomes domain");
        check(st.get(0).getVersion() == 0, "plain Set-Cookie stored as version 0");
        // re-putting the same identity replaces the value
        CookieManager cmR = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cmR.put(u, headers("Set-Cookie", "r=1; Domain=example.com; Path=/"));
        cmR.put(u, headers("Set-Cookie", "r=2; Domain=example.com; Path=/"));
        check(cmR.getCookieStore().getCookies().size() == 1, "same identity replaced not duplicated");
        check(cmR.getCookieStore().getCookies().get(0).getValue().equals("2"), "replacement keeps newest value");
        // unparsable header value is skipped without failing the whole put
        CookieManager cmU = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        Map<String, List<String>> mixed = new HashMap<>();
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
        // empty manager still yields a map with a (possibly empty) Cookie list
        CookieManager empty = new CookieManager();
        Map<String, List<String>> g0 = empty.get(URI.create("http://nowhere.com/"), new HashMap<>());
        check(g0 != null, "get returns non-null map");
        check(g0.containsKey("Cookie"), "get always contains Cookie key");
        check(g0.get("Cookie").isEmpty(), "no cookies -> empty Cookie list");
        // put then get round-trip, exact header form
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        URI u = URI.create("http://example.com/");
        cm.put(u, headers("Set-Cookie", "name=value; Domain=example.com; Path=/"));
        Map<String, List<String>> g1 = cm.get(u, new HashMap<String, List<String>>());
        check(g1.get("Cookie").contains("name=value"), "Cookie header form name=value");
        check(g1.get("Cookie").size() == 1, "one cookie one element");
        // multiple cookies -> one Cookie list with one element per cookie
        cm.put(u, headers("Set-Cookie", "second=2; Domain=example.com; Path=/"));
        Map<String, List<String>> g2 = cm.get(u, new HashMap<String, List<String>>());
        check(g2.get("Cookie").size() == 2, "two cookies two elements");
        check(g2.get("Cookie").contains("name=value") && g2.get("Cookie").contains("second=2"), "both present");
        // the requestHeaders argument is ignored, and never mutated
        Map<String, List<String>> req = new HashMap<>();
        req.put("Cookie", new ArrayList<>(Collections.singletonList("pre=1")));
        Map<String, List<String>> g3 = cm.get(u, req);
        check(g3.get("Cookie").contains("name=value"), "request Cookie ignored");
        check(!g3.get("Cookie").contains("pre=1"), "pre-existing request Cookie not echoed");
        check(req.get("Cookie").equals(Collections.singletonList("pre=1")), "request map untouched");
        // returned map is read-only
        ex(() -> g3.put("X-New", Collections.singletonList("v")), "java.lang.UnsupportedOperationException",
            null, "returned map unmodifiable");
    }

    static void testGetFiltering() throws Exception {
        // path filtering happens at the manager layer (not in the store)
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(URI.create("http://example.com/deep/path/x"),
            headers("Set-Cookie", "p=1; Domain=example.com; Path=/deep/path"));
        check(cm.get(URI.create("http://example.com/deep/path/y"), new HashMap<>())
            .get("Cookie").contains("p=1"), "path prefix visible");
        check(cm.get(URI.create("http://example.com/other"), new HashMap<>())
            .get("Cookie").isEmpty(), "path mismatch filtered by manager");
        // secure cookies only travel over https
        CookieManager s = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        s.put(URI.create("https://example.com/"), headers("Set-Cookie", "sec=1; Domain=example.com; Path=/; Secure"));
        check(s.get(URI.create("https://example.com/"), new HashMap<>()).get("Cookie").contains("sec=1"),
            "secure over https");
        check(s.get(URI.create("http://example.com/"), new HashMap<>()).get("Cookie").isEmpty(),
            "secure withheld over http");
        // version-1 Port attribute restricts ports
        CookieManager pp = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        pp.put(URI.create("http://example.com:8080/"),
            headers("Set-Cookie2", "pp=1; Domain=example.com; Path=/; Port=8080"));
        check(pp.get(URI.create("http://example.com:8080/"), new HashMap<>()).get("Cookie").contains("pp=1"),
            "portlisted cookie on listed port");
        check(pp.get(URI.create("http://example.com:9090/"), new HashMap<>()).get("Cookie").isEmpty(),
            "portlisted cookie hidden on other port");
        check(pp.get(URI.create("http://example.com/"), new HashMap<>()).get("Cookie").isEmpty(),
            "portlisted cookie hidden without explicit port");
        // a cookie expired in place is not served
        CookieManager exp = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        exp.put(URI.create("http://example.com/"), headers("Set-Cookie", "e=1; Domain=example.com; Path=/"));
        exp.getCookieStore().getCookies().get(0).setMaxAge(0);
        check(exp.get(URI.create("http://example.com/"), new HashMap<>()).get("Cookie").isEmpty(),
            "expired cookie withheld");
    }

    static void testCustomStoreAndPolicy() throws Exception {
        final List<String> log = new ArrayList<>();
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
        // read paths consult the store for matching cookies
        Map<String, List<String>> out = cm.get(URI.create("http://example.com/"), new HashMap<>());
        check(out.containsKey("Cookie"), "get builds header from custom store");
        // recording policy: arguments as parsed
        final List<String> seen = new ArrayList<>();
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
        // same name, different domains coexist as distinct identities
        CookieManager cm = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm.put(u, headers("Set-Cookie", "n=1; Domain=example.com; Path=/"));
        cm.put(URI.create("http://other.org/"), headers("Set-Cookie", "n=2; Domain=other.org; Path=/"));
        check(cm.getCookieStore().getCookies().size() == 2, "same name distinct domains coexist");
        check(cm.get(u, new HashMap<>()).get("Cookie").equals(Collections.singletonList("n=1")),
            "get serves only the matching domain copy");
        // same name different paths coexist; both match on the deeper uri
        CookieManager cm2 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm2.put(u, headers("Set-Cookie", "wide=1; Domain=example.com; Path=/"));
        cm2.put(URI.create("http://example.com/sub/x"), headers("Set-Cookie", "narrow=1; Domain=example.com; Path=/sub"));
        List<String> hdr = cm2.get(URI.create("http://example.com/sub/y"), new HashMap<>()).get("Cookie");
        check(hdr.size() == 2, "both path-level cookies served");
        check(hdr.contains("wide=1") && hdr.contains("narrow=1"), "contains both (order not specified)");
        // only the wider path matches the root uri
        List<String> hdrRoot = cm2.get(URI.create("http://example.com/"), new HashMap<>()).get("Cookie");
        check(hdrRoot.equals(Collections.singletonList("wide=1")), "root uri sees only root-path cookie");
        // domain attribute is case-insensitive and normalized to lowercase
        CookieManager cm3 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm3.put(u, headers("Set-Cookie", "u=1; Domain=EXAMPLE.com; Path=/"));
        check(cm3.getCookieStore().getCookies().get(0).getDomain().equals("example.com"), "domain attr lowercased");
        check(cm3.get(URI.create("http://WWW.EXAMPLE.COM/"), new HashMap<>()).get("Cookie").contains("u=1"),
            "uri host case-insensitive in get");
        // Set-Cookie2 value containing commas is treated as ONE cookie by the manager
        // (manager strips the header prefix before parsing, so the v1 comma split does not kick in)
        CookieManager cm4 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm4.put(u, headers("Set-Cookie2", "a=b; Domain=example.com; Path=/, c=d; Domain=example.com; Path=/"));
        check(cm4.getCookieStore().getCookies().size() == 1, "comma inside manager value stays one cookie");
        // vetoed cookie is never observable via get even on the same uri
        CookieManager cm5 = new CookieManager(null, CookiePolicy.ACCEPT_NONE);
        cm5.put(u, headers("Set-Cookie", "never=1; Domain=example.com"));
        check(cm5.get(u, new HashMap<>()).get("Cookie").isEmpty(), "vetoed cookie never served");
        // put map without any known cookie header key is a no-op
        CookieManager cm6 = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        cm6.put(u, headers("Content-Type", "text/plain"));
        check(cm6.getCookieStore().getCookies().isEmpty(), "non-cookie header ignored");
        // get/put round-trip stability across repeated calls
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
        // the map returned by get has exactly one key: "Cookie"
        Map<String, List<String>> rk = cm7.get(u, new HashMap<String, List<String>>());
        check(rk.keySet().size() == 1 && rk.keySet().contains("Cookie"), "get map has only Cookie key");
        // CookieManager is usable wherever a CookieHandler is expected
        CookieHandler asHandler = cm7;
        check(asHandler.get(u, new HashMap<String, List<String>>()).get("Cookie").contains("stable=2"),
            "polymorphic get via CookieHandler reference");
        // store reflectively reachable handle matches getCookieStore
        check(cm7.getCookieStore() == cm7.getCookieStore(), "getCookieStore stable identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
