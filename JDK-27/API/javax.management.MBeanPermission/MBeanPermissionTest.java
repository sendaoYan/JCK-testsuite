/*
 * @test
 * @summary javax.management.MBeanPermission conformance: white-box structure + black-box behavior.
 * @run main MBeanPermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import javax.management.*;

@SuppressWarnings({"removal","deprecation"})
public class MBeanPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkIAE(String expected, Runnable r) {
        try {
            r.run();
            check(false, "expected IllegalArgumentException: " + expected);
        } catch (IllegalArgumentException e) {
            check(expected.equals(e.getMessage()), "IAE message got: [" + e.getMessage() + "]");
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanPermission");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check("java.security.Permission".equals(c.getSuperclass().getName()), "direct superclass Permission");
        check(c.getConstructor(String.class, String.class) != null, "2-arg constructor");
        check(c.getConstructor(String.class, String.class, javax.management.ObjectName.class, String.class) != null, "4-arg constructor");
        c.getMethod("getActions");
        c.getMethod("implies", java.security.Permission.class);
        c.getMethod("equals", Object.class);
        c.getMethod("hashCode");
    }

    static void testBlackBox() throws Exception {
        MBeanPermission p = new MBeanPermission("*", "getAttribute,setAttribute,invoke");
        check("getAttribute,setAttribute,invoke".equals(p.getActions()), "actions preserved");
        check(p.implies(new MBeanPermission("*", "getAttribute")), "implies subset action");
        check(p.implies(new MBeanPermission("*", "setAttribute")), "implies another subset action");
        check(!p.implies(new MBeanPermission("*", "instantiate")), "does not imply disjoint action");
        check("*".equals(new MBeanPermission("*", "*").getActions()), "wildcard actions normalize to *");
        check(new MBeanPermission("*", "*").implies(p), "wildcard actions imply all");
        check(!p.implies(new MBeanPermission("*", "*")), "subset does not imply wildcard");

        check(new MBeanPermission("*", "invoke").implies(new MBeanPermission("a.b.C", "invoke")), "* class implies specific class");
        check(!new MBeanPermission("a.b.C", "invoke").implies(new MBeanPermission("*", "invoke")), "specific class does not imply *");

        MBeanPermission p4 = new MBeanPermission("a.b.C", "invoke", new javax.management.ObjectName("d:type=x"), "invoke");
        check("a.b.C#invoke[d:type=x]".equals(p4.getName()), "4-arg name");
        check("invoke".equals(p4.getActions()), "4-arg actions");
        check(p4.implies(new MBeanPermission("a.b.C", "invoke", new javax.management.ObjectName("d:type=x"), "invoke")), "4-arg implies equal");

        check(p.equals(new MBeanPermission("*", "getAttribute,setAttribute,invoke")), "equals same name/actions");
        check(p.hashCode() == new MBeanPermission("*", "getAttribute,setAttribute,invoke").hashCode(), "hashCode stable");
        check(!p.equals(new MBeanPermission("*", "getAttribute")), "equals differs by actions");

        checkIAE("MBeanPermission name cannot be null", () -> new MBeanPermission(null, "invoke"));
        checkIAE("MBeanPermission name cannot be empty", () -> new MBeanPermission("", "invoke"));
        checkIAE("MBeanPermission: actions can't be null", () -> new MBeanPermission("*", null));
        checkIAE("MBeanPermission: actions can't be empty", () -> new MBeanPermission("*", ""));
        checkIAE("Invalid permission: bogus", () -> new MBeanPermission("*", "bogus"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
