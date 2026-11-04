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
        try {
            System.setSecurityManager(null);
            throw new AssertionError("setSecurityManager: expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSecurityManager();
        testChecks();
        testSystemIntegration();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
