/*
 * @test
 * @summary java.lang.RuntimePermission conformance: white-box structure + black-box behavior.
 * @run main RuntimePermissionTest
 */

import java.lang.reflect.Modifier;
import java.security.BasicPermission;

public class RuntimePermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.RuntimePermission");
        check(Modifier.isFinal(c.getModifiers()), "RuntimePermission is final");
        check(c.getSuperclass() == BasicPermission.class, "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "(String) constructor present");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRuntimePermission() {
        RuntimePermission p = new RuntimePermission("getClassLoader");
        check(p instanceof BasicPermission, "extends BasicPermission");
        check("getClassLoader".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions is empty for one-arg ctor");

        RuntimePermission withActions = new RuntimePermission("setSecurityManager", "read,write");
        check("setSecurityManager".equals(withActions.getName()), "two-arg getName");
        check("".equals(withActions.getActions()), "BasicPermission ignores actions: getActions empty");

        check(p.implies(new RuntimePermission("getClassLoader")), "implies same name");
        check(!p.implies(new RuntimePermission("setSecurityManager")), "!implies different name");
        check(p.equals(new RuntimePermission("getClassLoader")), "equals same name");
        check(p.hashCode() == new RuntimePermission("getClassLoader").hashCode(), "hashCode");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testRuntimePermission();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
