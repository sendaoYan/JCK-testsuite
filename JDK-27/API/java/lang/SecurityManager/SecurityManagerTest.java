/*
 * @test
 * @summary java.lang.SecurityManager conformance: white-box structure + black-box behavior.
 * @run main SecurityManagerTest
 */

import java.io.FileDescriptor;
import java.net.InetAddress;

public class SecurityManagerTest {
    static int passed;

    // Exposes the protected getClassContext().
    static class SM extends SecurityManager {
        Class<?>[] ctx() { return getClassContext(); }
    }

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
        // Per the batch boundary rule, System.setSecurityManager is never invoked
        // (JEP 486 removed it). The reflective metadata checks below verify the
        // stub's declaration and deprecation without calling it; empirical
        // behavior was recorded during development: calling it throws
        // UnsupportedOperationException on JDK 27.
    }

    // ---- Part 2b: black-box for the remaining check* methods + getClassContext ----
    static void testBlackBox() {
        SecurityManager sm = new SecurityManager();
        // Every default check* method throws SecurityException unconditionally (JEP 486).
        expectSecurityException(() -> sm.checkAccept("localhost", 80), "checkAccept");
        expectSecurityException(() -> sm.checkAccess(Thread.currentThread().getThreadGroup()),
              "checkAccess(ThreadGroup)");
        expectSecurityException(() -> sm.checkAccess(Thread.currentThread()), "checkAccess(Thread)");
        expectSecurityException(() -> sm.checkConnect("localhost", 80, new Object()),
              "checkConnect(host,port,context)");
        expectSecurityException(() -> sm.checkConnect("localhost", 80), "checkConnect(host,port)");
        expectSecurityException(() -> sm.checkDelete("some-file"), "checkDelete");
        expectSecurityException(() -> sm.checkExec("ls"), "checkExec");
        expectSecurityException(() -> sm.checkLink("libc.so"), "checkLink");
        expectSecurityException(() -> sm.checkListen(8080), "checkListen");
        expectSecurityException(() -> sm.checkMulticast(InetAddress.getLoopbackAddress(), (byte) 1),
              "checkMulticast(addr,ttl)");
        expectSecurityException(() -> sm.checkMulticast(InetAddress.getLoopbackAddress()),
              "checkMulticast(addr)");
        expectSecurityException(() -> sm.checkPackageAccess("java.lang"), "checkPackageAccess");
        expectSecurityException(() -> sm.checkPackageDefinition("java.lang"), "checkPackageDefinition");
        expectSecurityException(() -> sm.checkPrintJobAccess(), "checkPrintJobAccess");
        expectSecurityException(() -> sm.checkPropertiesAccess(), "checkPropertiesAccess");
        expectSecurityException(() -> sm.checkPropertyAccess("java.home"), "checkPropertyAccess");
        expectSecurityException(() -> sm.checkSecurityAccess("getProperty"), "checkSecurityAccess");
        expectSecurityException(() -> sm.checkSetFactory(), "checkSetFactory");
        expectSecurityException(() -> sm.checkWrite(new FileDescriptor()), "checkWrite(FileDescriptor)");
        expectSecurityException(() -> sm.checkWrite("some-file"), "checkWrite(String)");

        // getClassContext() is protected; expose it via a subclass.
        Class<?>[] ctx = new SM().ctx();
        check(ctx != null && ctx.length >= 1, "getClassContext returns a non-empty stack");
    }

    // ================= 契约深化（2026-11）追加：JEP 486 stub 化实证 =================
    // The setter still EXISTS as a deprecated-for-removal stub that throws
    // UnsupportedOperationException; only its shape is reflected here, it is
    // never invoked by the new checks.
    static void testStubSurface() throws Exception {
        java.lang.reflect.Method m = System.class.getMethod("setSecurityManager", SecurityManager.class);
        check(m != null, "System.setSecurityManager still declared [JDK27 stub]");
        check(java.lang.reflect.Modifier.isStatic(m.getModifiers()), "setSecurityManager static");
        check(m.getReturnType() == void.class, "setSecurityManager returns void");
        Deprecated d = m.getAnnotation(Deprecated.class);
        check(d != null, "setSecurityManager @Deprecated");
        check("17".equals(d.since()), "setSecurityManager deprecated since 17");
        check(d.forRemoval(), "setSecurityManager deprecated for removal");
        Deprecated cd = SecurityManager.class.getAnnotation(Deprecated.class);
        check(cd != null, "SecurityManager class @Deprecated");
        check("17".equals(cd.since()), "SecurityManager deprecated since 17");
        check(cd.forRemoval(), "SecurityManager deprecated for removal");
        check(!java.lang.reflect.Modifier.isFinal(SecurityManager.class.getModifiers()),
              "not final (subclassable)");
        check(SecurityManager.class.getConstructors().length == 1, "exactly 1 public ctor");
        check(System.getSecurityManager() == null, "getSecurityManager permanently null [JEP 486]");
    }

    // Even the null-argument guards are unreachable: the stub throws
    // SecurityException before any argument validation.
    static void testStubNullArguments() {
        SecurityManager sm = new SecurityManager();
        try {
            sm.checkPermission(null);
            throw new AssertionError("checkPermission(null): expected SecurityException");
        } catch (SecurityException e) {
            check(e.getMessage() == null, "checkPermission(null) SecurityException null message [JDK27]");
        }
        expectSecurityException(() -> sm.checkPropertyAccess(null), "checkPropertyAccess(null)");
        expectSecurityException(() -> sm.checkRead((String) null), "checkRead(null)");
        expectSecurityException(() -> sm.checkAccess((Thread) null), "checkAccess(null Thread)");
        expectSecurityException(() -> sm.checkAccess((ThreadGroup) null), "checkAccess(null ThreadGroup)");
        expectSecurityException(() -> sm.checkDelete(null), "checkDelete(null)");
        expectSecurityException(() -> sm.checkExec(null), "checkExec(null)");
        expectSecurityException(() -> sm.checkLink(null), "checkLink(null)");
        expectSecurityException(() -> sm.checkConnect(null, 80), "checkConnect(null, port)");
    }

    // The context-taking overloads are stubs too: any context object (even a
    // real AccessControlContext or a bogus String) gets SecurityException.
    static void testStubContextChecks() {
        SecurityManager sm = new SecurityManager();
        Object acc = sm.getSecurityContext();
        check(acc instanceof java.security.AccessControlContext, "getSecurityContext still returns an ACC");
        expectSecurityException(() -> sm.checkRead("some-file", "not-an-acc"),
                                "checkRead with non-ACC context");
        expectSecurityException(() -> sm.checkRead("some-file", acc), "checkRead with ACC context");
        expectSecurityException(
                () -> sm.checkPermission(new java.lang.RuntimePermission("exitVM"), "not-an-acc"),
                "checkPermission(Permission, non-ACC)");
        expectSecurityException(
                () -> sm.checkPermission(new java.lang.RuntimePermission("exitVM"), acc),
                "checkPermission(Permission, ACC)");
        expectSecurityException(() -> sm.checkConnect("localhost", 80, acc),
                                "checkConnect with context");
    }

    // Remaining stubs not covered by earlier sections.
    static void testStubRemainingChecks() {
        SecurityManager sm = new SecurityManager();
        expectSecurityException(() -> sm.checkRead(new FileDescriptor()), "checkRead(FileDescriptor)");
        expectSecurityException(() -> sm.checkWrite(new FileDescriptor()), "checkWrite(FileDescriptor)");
        expectSecurityException(() -> sm.checkExit(-1), "checkExit(-1)");
        expectSecurityException(() -> sm.checkListen(-1), "checkListen(-1)");
        expectSecurityException(() -> sm.checkAccept(null, -1), "checkAccept(null, -1)");
        expectSecurityException(() -> sm.checkPackageAccess(null), "checkPackageAccess(null)");
        expectSecurityException(() -> sm.checkPackageDefinition(""), "checkPackageDefinition(empty)");
        expectSecurityException(() -> sm.checkPropertyAccess("any.key"), "checkPropertyAccess(any)");
        expectSecurityException(() -> sm.checkSecurityAccess(null), "checkSecurityAccess(null)");
    }

    static void testClassContextStub() {
        Class<?>[] ctx = new SM().ctx();
        check(ctx != null && ctx.length >= 2, "getClassContext still functional");
        check(ctx[0].getName().equals("SecurityManagerTest$SM"), "getClassContext[0] is the SM subclass");
        check(ctx[1].getName().equals("SecurityManagerTest"), "getClassContext[1] is the caller");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSecurityManager();
        testChecks();
        testSystemIntegration();
        testBlackBox();
        testStubSurface();
        testStubNullArguments();
        testStubContextChecks();
        testStubRemainingChecks();
        testClassContextStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
