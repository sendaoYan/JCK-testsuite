/*
 * @test
 * @summary java.net.URLPermission conformance: white-box structure + black-box behavior.
 * @run main URLPermissionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class URLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
    }

    static void testBlackBox() throws Exception {
        URLPermission exact = new URLPermission("http://example.com/path", "GET:Accept");
        check(exact.getActions().equals("GET:Accept"), "actions normalized");
        check(exact.implies(new URLPermission("http://example.com/path", "GET:Accept")), "implies self");
        check(!exact.implies(new URLPermission("http://example.com/path/deeper", "GET:Accept")), "exact path not imply deeper");
        check(!exact.implies(new URLPermission("http://example.com/other", "GET:Accept")), "not imply other path");

        // single-arg constructor => wildcard methods and headers
        URLPermission any = new URLPermission("http://example.com/path");
        check(any.getActions().equals("*:*"), "single-arg wildcard actions");

        // "/-" suffix: recursive prefix match
        URLPermission prefix = new URLPermission("http://example.com/path/-", "GET:Accept");
        check(prefix.implies(new URLPermission("http://example.com/path/deeper", "GET:Accept")), "/- implies deeper");
        check(!prefix.implies(new URLPermission("http://example.com/path", "GET:Accept")), "/- not imply exact (no slash)");

        // "/*" suffix: immediate children only
        URLPermission star = new URLPermission("http://example.com/path/*", "GET:Accept");
        check(star.implies(new URLPermission("http://example.com/path/child", "GET:Accept")), "/* implies child");
        check(!star.implies(new URLPermission("http://example.com/path/deeper/x", "GET:Accept")), "/* not imply nested");

        check(exact.equals(new URLPermission("http://example.com/path", "GET:Accept")), "equals");
        check(exact.hashCode() == new URLPermission("http://example.com/path", "GET:Accept").hashCode(), "hashCode");
        check(!exact.equals(prefix), "not equals different path");

        fails(IllegalArgumentException.class, "no scheme", () -> new URLPermission("example.com/path"));
        fails(IllegalArgumentException.class, "bad url", () -> new URLPermission("not a url"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
