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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookieManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(CookieHandler.class.isAssignableFrom(c), "extends CookieHandler");
        check(c.getConstructor() != null, "ctor()");
        check(c.getConstructor(CookieStore.class, CookiePolicy.class) != null, "ctor(store,policy)");
        check(c.getMethod("getCookieStore") != null, "getCookieStore");
        check(c.getMethod("setCookiePolicy", CookiePolicy.class) != null, "setCookiePolicy");
        check(c.getMethod("get", URI.class, Map.class) != null, "get");
        check(c.getMethod("put", URI.class, Map.class) != null, "put");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
