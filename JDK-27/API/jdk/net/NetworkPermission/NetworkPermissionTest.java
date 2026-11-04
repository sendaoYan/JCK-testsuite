/*
 * @test
 * @summary jdk.net.NetworkPermission conformance: white-box structure + black-box behavior.
 * @run main NetworkPermissionTest
 */

import jdk.net.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
public class NetworkPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.net.NetworkPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(hasMethod(c, "getName"), "getName");
        check(hasMethod(c, "getActions"), "getActions");

    }

    static void testBlackBox() throws Exception {
        NetworkPermission p = new NetworkPermission("unixDomainSocket");
        check("unixDomainSocket".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "actions empty");
        NetworkPermission p2 = new NetworkPermission("unixDomainSocket", "accept");
        check("".equals(p2.getActions()), "actions empty with actions arg");
        check(p.equals(p2), "equals same name");
        check(p.hashCode() == p2.hashCode(), "hashCode");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
