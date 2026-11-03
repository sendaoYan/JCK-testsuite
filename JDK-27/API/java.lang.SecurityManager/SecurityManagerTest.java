/*
 * @test
 * @summary java.lang.SecurityManager conformance: white-box structure + black-box behavior.
 * @run main SecurityManagerTest
 */

public class SecurityManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.SecurityManager");
        check(!c.isInterface(), "SecurityManager is a class");
        check(c.getSuperclass() == Object.class, "SecurityManager extends Object");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSecurityManager() {
        SecurityManager sm = new SecurityManager();
        check(sm.getThreadGroup() == Thread.currentThread().getThreadGroup(),
              "getThreadGroup returns current thread's group");
        check(sm.getSecurityContext() instanceof java.security.AccessControlContext,
              "getSecurityContext returns an AccessControlContext");
    }

    static void testChecks() {
        SecurityManager sm = new SecurityManager();
        // Since the SM was disabled (JEP 486), every default check* method
        // throws SecurityException unconditionally.
        expectSecurityException(() -> sm.checkPermission(new java.lang.RuntimePermission("getClassLoader")),
              "checkPermission throws");
        expectSecurityException(() -> sm.checkExit(0), "checkExit throws");
        expectSecurityException(() -> sm.checkCreateClassLoader(), "checkCreateClassLoader throws");
        expectSecurityException(() -> sm.checkRead("some-file"), "checkRead throws");
    }

    static void expectSecurityException(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected SecurityException");
        } catch (SecurityException expected) {
            passed++;
        }
    }

    static void testSystemIntegration() {
        check(System.getSecurityManager() == null, "no security manager installed");
        try {
            System.setSecurityManager(null);
            throw new AssertionError("setSecurityManager: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSecurityManager();
        testChecks();
        testSystemIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
