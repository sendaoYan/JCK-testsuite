/*
 * @test
 * @summary java.security.Guard conformance: white-box structure + black-box behavior.
 * @run main GuardTest
 */

import java.lang.reflect.Modifier;
import java.security.AllPermission;
import java.security.Guard;

public class GuardTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Guard");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"checkGuard"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Permission implements Guard
        AllPermission all = new AllPermission();
        check(all instanceof Guard, "AllPermission instanceof Guard");

        // A custom Guard can permit without throwing
        Guard allow = new Guard() {
            public void checkGuard(Object o) { /* allow */ }
        };
        boolean noThrow = true;
        try {
            allow.checkGuard(new Object());
        } catch (SecurityException e) {
            noThrow = false;
        }
        check(noThrow, "custom permissive Guard does not throw");

        // A custom Guard can deny
        Guard deny = new Guard() {
            public void checkGuard(Object o) { throw new SecurityException("no"); }
        };
        boolean threw = false;
        try {
            deny.checkGuard("target");
        } catch (SecurityException e) {
            threw = true;
            check("no".equals(e.getMessage()), "deny message");
        }
        check(threw, "custom Guard can throw SecurityException");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
