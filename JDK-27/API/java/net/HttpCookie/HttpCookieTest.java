/*
 * @test
 * @summary java.net.HttpCookie conformance: white-box structure + black-box behavior.
 * @run main HttpCookieTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class HttpCookieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpCookie");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Cloneable.class.isAssignableFrom(c), "Cloneable");
        check(c.getConstructor(String.class, String.class) != null, "ctor(name,value)");
        check(c.getMethod("parse", String.class) != null, "parse");
        check(c.getMethod("domainMatches", String.class, String.class) != null, "domainMatches");
        check(c.getMethod("getMaxAge") != null, "getMaxAge");
        check(c.getMethod("setMaxAge", long.class) != null, "setMaxAge");
    }

    static void testBlackBox() throws Exception {
        HttpCookie c = new HttpCookie("k", "v");
        check(c.getName().equals("k"), "name");
        check(c.getValue().equals("v"), "value");
        check(c.getMaxAge() == -1, "default maxAge -1");
        check(c.getVersion() == 1, "default version 1");
        check(c.getPath() == null, "default path null");
        check(c.getDomain() == null, "default domain null");
        check(!c.hasExpired(), "default not expired");

        // max-age lifecycle
        c.setMaxAge(3600);
        check(c.getMaxAge() == 3600, "setMaxAge");
        check(!c.hasExpired(), "positive maxAge not expired");
        c.setMaxAge(0);
        check(c.getMaxAge() == 0, "setMaxAge 0");
        check(c.hasExpired(), "maxAge 0 expired");
        c.setMaxAge(-1);
        check(c.getMaxAge() == -1 && !c.hasExpired(), "maxAge -1 reset");

        // attribute setters/getters
        HttpCookie d = new HttpCookie("a", "b");
        d.setPath("/p"); d.setDomain("example.com"); d.setSecure(true);
        d.setHttpOnly(true); d.setComment("cmt"); d.setCommentURL("http://c/");
        d.setVersion(0); d.setPortlist("80,8080");
        check(d.getPath().equals("/p"), "path");
        check(d.getDomain().equals("example.com"), "domain");
        check(d.getSecure(), "secure");
        check(d.isHttpOnly(), "httpOnly");
        check(d.getComment().equals("cmt"), "comment");
        check(d.getCommentURL().equals("http://c/"), "commentURL");
        check(d.getVersion() == 0, "version");
        check(d.getPortlist().equals("80,8080"), "portlist");

        // toString / clone
        check(new HttpCookie("k", "v").toString().equals("k=\"v\""), "toString v1");
        HttpCookie orig = new HttpCookie("k", "v");
        HttpCookie cl = (HttpCookie) orig.clone();
        check(cl.equals(orig), "clone equal");
        cl.setValue("v2");
        check(orig.getValue().equals("v"), "clone independent value");

        // equals: name (ci) + domain (ci) + path (cs); ignores value
        HttpCookie e1 = new HttpCookie("N", "1"); e1.setDomain("EXAMPLE.com"); e1.setPath("/x");
        HttpCookie e2 = new HttpCookie("n", "2"); e2.setDomain("example.com"); e2.setPath("/x");
        check(e1.equals(e2), "equals ignores value, ci name/domain");
        e2.setPath("/y");
        check(!e1.equals(e2), "not equals different path");

        // parse
        List<HttpCookie> l = HttpCookie.parse("a=b; Path=/; Max-Age=3600");
        check(l.size() == 1, "parse single cookie");
        check(l.get(0).getName().equals("a") && l.get(0).getValue().equals("b"), "parsed name/value");
        check(l.get(0).getPath().equals("/"), "parsed path");
        check(l.get(0).getMaxAge() == 3600, "parsed maxAge");
        check(HttpCookie.parse("a=b; c=d").size() == 1, "semicolon is attribute separator");

        // domainMatches
        check(HttpCookie.domainMatches(".example.com", "www.example.com"), "domainMatches subdomain");
        check(HttpCookie.domainMatches("example.com", "example.com"), "domainMatches exact");
        check(!HttpCookie.domainMatches("foo.com", "bar.com"), "domainMatches mismatch");
        check(!HttpCookie.domainMatches(null, "x"), "domainMatches null domain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
