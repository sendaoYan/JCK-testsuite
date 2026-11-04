/*
 * @test
 * @summary java.security.GuardedObject conformance: white-box structure + black-box behavior.
 * @run main GuardedObjectTest
 */

import java.lang.reflect.Modifier;
import java.security.Guard;
import java.security.GuardedObject;

public class GuardedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A Guard that always permits.
    static class PermissiveGuard implements Guard {
        public void checkGuard(Object o) { /* allow */ }
    }
    // A Guard that always denies.
    static class DenyGuard implements Guard {
        public void checkGuard(Object o) { throw new SecurityException("denied"); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.GuardedObject");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"getObject"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String secret = "guarded value";

        // Permissive guard returns the object
        GuardedObject allowed = new GuardedObject(secret, new PermissiveGuard());
        check(secret.equals(allowed.getObject()), "getObject returns object when guard permits");

        // Denying guard throws SecurityException
        GuardedObject denied = new GuardedObject(secret, new DenyGuard());
        boolean threw = false;
        try {
            denied.getObject();
        } catch (SecurityException e) {
            threw = true;
            check("denied".equals(e.getMessage()), "SecurityException message");
        }
        check(threw, "getObject throws SecurityException when guard denies");

        // getObject may be called repeatedly
        check(secret.equals(allowed.getObject()), "getObject repeatable");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
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
