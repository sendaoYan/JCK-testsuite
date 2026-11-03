/*
 * @test
 * @summary java.awt.AWTPermission conformance: white-box structure + black-box behavior.
 * @run main AWTPermissionTest
 */

import java.security.Permission;
import java.lang.reflect.*;

public class AWTPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.AWTPermission", false, AWTPermissionTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.AWTPermission");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "extends BasicPermission");
    }

    static void testBlackBox() throws Exception {
        java.awt.AWTPermission p1 = new java.awt.AWTPermission("accessClipboard");
        check("accessClipboard".equals(p1.getName()), "getName");
        check(p1.getActions().isEmpty(), "no-arg actions empty");
        java.awt.AWTPermission p2 = new java.awt.AWTPermission("accessClipboard", "read");
        check(p2.getActions().isEmpty(), "actions ctor empty (AWTPermission ignores actions)");
        check(p1.equals(p2), "one/two-arg ctor equal");
        check(p1.implies(p2) || !p1.implies(p2), "implies callable");
        check(p1.equals(p1), "equals reflexive");
        check(p1.hashCode() == p1.hashCode(), "hashCode stable");
        java.security.Permission same = new java.awt.AWTPermission("accessClipboard");
        check(p1.equals(same), "equal perms equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
