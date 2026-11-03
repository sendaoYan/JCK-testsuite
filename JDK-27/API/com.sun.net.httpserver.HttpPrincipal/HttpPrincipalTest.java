/*
 * @test
 * @summary com.sun.net.httpserver.HttpPrincipal conformance: white-box structure + black-box behavior.
 * @run main HttpPrincipalTest
 */

import java.lang.reflect.*;

public class HttpPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpPrincipal");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(java.security.Principal.class.isAssignableFrom(c), "implements java.security.Principal");
        check(c.getConstructor(String.class, String.class) != null, "public (String,String) constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        com.sun.net.httpserver.HttpPrincipal p = new com.sun.net.httpserver.HttpPrincipal("user", "realm");
        check(p.getUsername().equals("user"), "username");
        check(p.getRealm().equals("realm"), "realm");
        check(p.getName().equals("realm:user"), "name == realm:username");
        check(p.toString().equals(p.getName()), "toString == getName");

        // equals / hashCode
        check(p.equals(new com.sun.net.httpserver.HttpPrincipal("user", "realm")), "equals same user/realm");
        check(p.hashCode() == new com.sun.net.httpserver.HttpPrincipal("user", "realm").hashCode(),
              "hashCode consistent with equals");
        check(!p.equals(new com.sun.net.httpserver.HttpPrincipal("user2", "realm")), "different username not equal");
        check(!p.equals(new com.sun.net.httpserver.HttpPrincipal("user", "realm2")), "different realm not equal");
        check(!p.equals(new Object()), "non-HttpPrincipal not equal");
        check(!p.equals(null), "null not equal");

        // empty strings are legal; name is realm + ":" + username
        com.sun.net.httpserver.HttpPrincipal empty = new com.sun.net.httpserver.HttpPrincipal("", "");
        check(empty.getName().equals(":"), "empty user/realm name is ':'");
        check(empty.hashCode() == ("" + "").hashCode(), "hashCode = (username+realm).hashCode()");

        // null rejection (per spec)
        boolean threw = false;
        try { new com.sun.net.httpserver.HttpPrincipal(null, "realm"); } catch (NullPointerException e) { threw = true; }
        check(threw, "null username throws NPE");
        threw = false;
        try { new com.sun.net.httpserver.HttpPrincipal("user", null); } catch (NullPointerException e) { threw = true; }
        check(threw, "null realm throws NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
