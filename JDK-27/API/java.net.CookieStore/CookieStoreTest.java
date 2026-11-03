/*
 * @test
 * @summary java.net.CookieStore conformance: white-box structure + black-box behavior.
 * @run main CookieStoreTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class CookieStoreTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookieStore");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("add", URI.class, HttpCookie.class) != null, "add");
        check(c.getMethod("get", URI.class) != null, "get");
        check(c.getMethod("getCookies") != null, "getCookies");
        check(c.getMethod("getURIs") != null, "getURIs");
        check(c.getMethod("remove", URI.class, HttpCookie.class) != null, "remove");
        check(c.getMethod("removeAll") != null, "removeAll");
    }

    static void testBlackBox() throws Exception {
        // Use the concrete store behind CookieManager (InMemoryCookieStore).
        CookieStore cs = new CookieManager().getCookieStore();
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
