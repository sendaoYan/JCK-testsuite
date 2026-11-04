/*
 * @test
 * @summary javax.management.MBeanServerPermission conformance: white-box structure + black-box behavior.
 * @run main MBeanServerPermissionTest
 */

import java.lang.reflect.*;
import java.security.*;
import javax.management.*;

@SuppressWarnings({"removal","deprecation"})
public class MBeanServerPermissionTest {
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
        Class<?> c = Class.forName("javax.management.MBeanServerPermission");
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
        c.getMethod("newPermissionCollection");
    }

    static void testBlackBox() throws Exception {
        MBeanServerPermission sp = new MBeanServerPermission("findMBeanServer,createMBeanServer");
        check("createMBeanServer,findMBeanServer".equals(sp.getName()), "name canonicalized");
        check("".equals(sp.getActions()), "actions empty (BasicPermission)");
        check(sp.implies(new MBeanServerPermission("createMBeanServer")), "implies subset");
        check(sp.implies(new MBeanServerPermission("findMBeanServer")), "implies other subset");
        check(!sp.implies(new MBeanServerPermission("releaseMBeanServer")), "does not imply disjoint");
        check(!new MBeanServerPermission("createMBeanServer").implies(sp), "subset does not imply superset");
        check(new MBeanServerPermission("*").implies(new MBeanServerPermission("createMBeanServer")), "* implies specific");
        check(!new MBeanServerPermission("createMBeanServer").implies(new MBeanServerPermission("*")), "specific does not imply *");
        check("*".equals(new MBeanServerPermission("*").getName()), "wildcard name");
        check("".equals(new MBeanServerPermission("*").getActions()), "wildcard actions empty");

        MBeanServerPermission same = new MBeanServerPermission("findMBeanServer,createMBeanServer");
        check(sp.equals(same), "equals ignores name order");
        check(sp.hashCode() == same.hashCode(), "hashCode ignores name order");
        check(!sp.equals(new MBeanServerPermission("createMBeanServer")), "equals differs by mask");

        java.security.PermissionCollection pc = sp.newPermissionCollection();
        pc.add(sp);
        check(pc.implies(new MBeanServerPermission("createMBeanServer")), "PermissionCollection implies subset");

        checkIAE("Invalid MBeanServerPermission name: \"bogus\"", () -> new MBeanServerPermission("bogus"));
        checkIAE("MBeanServerPermission actions must be null: foo", () -> new MBeanServerPermission("createMBeanServer", "foo"));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
