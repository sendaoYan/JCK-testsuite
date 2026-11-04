/*
 * @test
 * @summary java.net.CookiePolicy conformance: white-box structure + black-box behavior.
 * @run main CookiePolicyTest
 */

import java.lang.reflect.*;
import java.net.*;

public class CookiePolicyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.CookiePolicy");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(c.getMethod("shouldAccept", URI.class, HttpCookie.class) != null, "shouldAccept");
        check(c.getField("ACCEPT_ALL") != null, "ACCEPT_ALL");
        check(c.getField("ACCEPT_NONE") != null, "ACCEPT_NONE");
        check(c.getField("ACCEPT_ORIGINAL_SERVER") != null, "ACCEPT_ORIGINAL_SERVER");
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
        check(!CookiePolicy.ACCEPT_ORIGINAL_SERVER.shouldAccept(URI.create("http://other.org/"), other), "ACCEPT_ORIGINAL_SERVER different host");

        // constants are distinct singletons
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_NONE, "distinct ALL/NONE");
        check(CookiePolicy.ACCEPT_ALL != CookiePolicy.ACCEPT_ORIGINAL_SERVER, "distinct ALL/ORIGINAL");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
