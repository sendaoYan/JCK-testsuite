/*
 * @test
 * @summary jdk.net.UnixDomainPrincipal conformance: white-box structure + black-box behavior.
 * @run main UnixDomainPrincipalTest
 */

import jdk.net.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class UnixDomainPrincipalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.net.UnixDomainPrincipal");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.lang.Record.class.isAssignableFrom(c), "is a record");
        check(hasMethod(c, "user"), "user");
        check(hasMethod(c, "group"), "group");

    }

    static void testBlackBox() throws Exception {
        java.nio.file.attribute.UserPrincipalLookupService ls = java.nio.file.FileSystems.getDefault().getUserPrincipalLookupService();
        java.nio.file.attribute.UserPrincipal up = ls.lookupPrincipalByName(System.getProperty("user.name"));
        java.nio.file.attribute.GroupPrincipal gp = null;
        try { gp = ls.lookupPrincipalByGroupName("root"); } catch (java.io.IOException e) {}
        UnixDomainPrincipal p = new UnixDomainPrincipal(up, gp);
        check(p.user() == up, "user");
        check(p.group() == gp, "group");
        check(p.toString() != null, "toString");
        check(p.equals(new UnixDomainPrincipal(up, gp)), "equals");
        check(p.hashCode() == new UnixDomainPrincipal(up, gp).hashCode(), "hashCode");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
