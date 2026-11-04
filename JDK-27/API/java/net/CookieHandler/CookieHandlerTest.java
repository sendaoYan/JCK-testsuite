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
        check(c.getMethod("getDefault") != null, "getDefault");
        check(c.getMethod("setDefault", CookieHandler.class) != null, "setDefault");
        check(c.getMethod("get", URI.class, Map.class) != null, "get");
        check(c.getMethod("put", URI.class, Map.class) != null, "put");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
