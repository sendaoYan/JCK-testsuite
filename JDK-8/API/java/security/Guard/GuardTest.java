/*
 * @test
 * @summary java.security.Guard conformance: interface hierarchy and key methods.
 * @run main GuardTest
 */

public class GuardTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Guard");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkGuard"), "declares checkGuard");

        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static java.lang.Object lastGuarded;

    static void testContract() throws Exception {
        // single-method interface: checkGuard(Object) only, returns void
        java.lang.reflect.Method[] ms = java.security.Guard.class.getDeclaredMethods();
        check(ms.length == 1, "single method interface");
        check(ms[0].getName().equals("checkGuard"), "method name checkGuard");
        check(ms[0].getParameterCount() == 1 && ms[0].getParameterTypes()[0] == Object.class,
                "checkGuard(Object) signature");
        check(ms[0].getReturnType() == void.class, "checkGuard returns void");
        check(ms[0].getExceptionTypes().length == 1
                && ms[0].getExceptionTypes()[0] == SecurityException.class,
                "declares throws SecurityException (unchecked)");
        check(!ms[0].isDefault(), "not a default method");

        // Permission implements Guard: the platform's canonical guard
        check(java.security.Guard.class.isAssignableFrom(java.security.Permission.class),
                "Permission is a Guard");
        check(new java.security.AllPermission() instanceof java.security.Guard,
                "AllPermission instanceof Guard");

        // permissive guard: no exception, invoked with the exact target object
        class Recording implements java.security.Guard {
            int calls;
            public void checkGuard(Object o) { calls++; lastGuarded = o; }
        }
        Recording g = new Recording();
        Object target = new Object();
        g.checkGuard(target);
        check(g.calls == 1, "guard invoked once");
        check(lastGuarded == target, "guard receives exact target object");
        g.checkGuard(null);
        check(g.calls == 2 && lastGuarded == null, "null target permitted by contract");

        // denying guard: SecurityException with caller's message propagated verbatim
        class Denying implements java.security.Guard {
            public void checkGuard(Object o) { throw new SecurityException("denied-by-guard"); }
        }
        try {
            new Denying().checkGuard("x");
            check(false, "deny must throw");
        } catch (SecurityException e) {
            check("denied-by-guard".equals(e.getMessage()), "SecurityException message verbatim");
        }

        // Permission.checkGuard (JDK-8, no SecurityManager installed): a documented no-op --
        // it only delegates to the SM when one is set; AccessController.checkPermission(perm)
        // itself still throws AccessControlException for ungranted permissions (recorded fact)
        boolean denied = false;
        try {
            new java.security.SecurityPermission("setLog").checkGuard(new Object());
        } catch (SecurityException e) {
            denied = true;
        }
        check(!denied, "Permission.checkGuard no-op without SecurityManager (JDK-8 actual behavior)");
        try {
            java.security.AccessController.checkPermission(new java.security.SecurityPermission("setLog"));
            check(false, "AccessController.checkPermission denies ungranted SecurityPermission");
        } catch (java.security.AccessControlException e) {
            check(e.getPermission() instanceof java.security.SecurityPermission,
                    "AccessControlException carries the permission");
        }

        // a guard may throw unchecked subtypes of other kinds; they are not wrapped
        try {
            new java.security.Guard() {
                public void checkGuard(Object o) { throw new IllegalStateException("boom"); }
            }.checkGuard(null);
            check(false, "unchecked exception propagates");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "unchecked exception unwrapped");
        }

        // GuardedObject integration: getObject consults the guard each call
        Recording rg = new Recording();
        java.security.GuardedObject go = new java.security.GuardedObject("payload", rg);
        check("payload".equals(go.getObject()), "permissive guard yields object");
        check("payload".equals(go.getObject()), "second getObject also permitted");
        check(rg.calls == 2, "guard consulted on every getObject");
    }
}
