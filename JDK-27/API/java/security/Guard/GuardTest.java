/*
 * @test
 * @summary java.security.Guard conformance: white-box structure + black-box behavior.
 * @run main GuardTest
 */

import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.Guard;
import java.security.GuardedObject;
import java.security.SecurityPermission;

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

        // ---- appended contract dimensions ----
        // single-method interface shape: checkGuard(Object) -> void, no checked exceptions
        java.lang.reflect.Method[] ms = Guard.class.getDeclaredMethods();
        check(ms.length == 1, "single method interface");
        check(ms[0].getName().equals("checkGuard") && ms[0].getParameterCount() == 1
                && ms[0].getParameterTypes()[0] == Object.class, "checkGuard(Object) signature");
        check(ms[0].getReturnType() == void.class, "returns void");
        check(ms[0].getExceptionTypes().length == 1
                && ms[0].getExceptionTypes()[0] == SecurityException.class,
                "declares throws SecurityException (unchecked)");
        check(!ms[0].isDefault(), "not a default method");
        // guard receives the exact guarded object (identity), null target allowed
        Object seen = new Object();
        final Object[] box = new Object[1];
        Guard recorder = new Guard() { public void checkGuard(Object o) { box[0] = o; } };
        recorder.checkGuard(seen);
        check(box[0] == seen, "guard receives exact target identity");
        recorder.checkGuard(null);
        check(box[0] == null, "null target permitted");
        // Permission.checkGuard under JEP 486 (no SecurityManager permitted): every permission
        // check fails with a PLAIN SecurityException (not AccessControlException) -- recorded fact
        try {
            new SecurityPermission("setLog").checkGuard(new Object());
            check(false, "Permission guard must deny post-SMR");
        } catch (AccessControlException e) {
            check(false, "denial must be plain SecurityException, not AccessControlException");
        } catch (SecurityException e) {
            check(e.getClass() == SecurityException.class, "denial is plain SecurityException");
        }
        // unchecked non-Security exceptions propagate unwrapped
        try {
            new Guard() { public void checkGuard(Object o) { throw new IllegalStateException("boom"); } }
                    .checkGuard(null);
            check(false, "unchecked propagation");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "ISE unwrapped");
        }
        // GuardedObject consults the guard on every getObject
        final int[] calls = {0};
        Guard counting = new Guard() { public void checkGuard(Object o) { calls[0]++; } };
        GuardedObject go = new GuardedObject("payload", counting);
        check("payload".equals(go.getObject()), "first getObject permitted");
        check("payload".equals(go.getObject()), "second getObject permitted");
        check(calls[0] == 2, "guard consulted on every getObject");
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
