/*
 * @test
 * @summary javax.management.MBeanTrustPermission conformance: white-box structure + black-box behavior.
 * @run main MBeanTrustPermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import javax.management.*;

@SuppressWarnings({"removal","deprecation"})
public class MBeanTrustPermissionTest {
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
        Class<?> c = Class.forName("javax.management.MBeanTrustPermission");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.security.BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check("java.security.BasicPermission".equals(c.getSuperclass().getName()), "direct superclass BasicPermission");
        check(c.getConstructor(String.class) != null, "1-arg constructor");
        check(c.getConstructor(String.class, String.class) != null, "2-arg constructor");
        c.getMethod("implies", java.security.Permission.class);
        c.getMethod("equals", Object.class);
        c.getMethod("hashCode");
    }

    static void testBlackBox() throws Exception {
        MBeanTrustPermission register = new MBeanTrustPermission("register");
        check("register".equals(register.getName()), "name register");
        check("".equals(register.getActions()), "actions empty (BasicPermission)");
        check(register.implies(new MBeanTrustPermission("register")), "register implies register");
        check(!register.implies(new MBeanTrustPermission("*")), "register does not imply *");

        MBeanTrustPermission star = new MBeanTrustPermission("*");
        check("*".equals(star.getName()), "name wildcard");
        check(star.implies(new MBeanTrustPermission("register")), "* implies register");

        check(register.equals(new MBeanTrustPermission("register")), "equals same name");
        check(register.hashCode() == new MBeanTrustPermission("register").hashCode(), "hashCode stable");
        check(!register.equals(star), "equals differs by name");

        checkIAE("MBeanTrustPermission: Unknown target name [bogus]", () -> new MBeanTrustPermission("bogus"));
        checkIAE("MBeanTrustPermission actions must be null: foo", () -> new MBeanTrustPermission("register", "foo"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
