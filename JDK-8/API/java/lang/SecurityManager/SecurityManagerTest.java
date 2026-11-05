/*
 * @test
 * @summary java.lang.SecurityManager conformance: thread group and security context.
 * @run main SecurityManagerTest
 */

public class SecurityManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSecurityManager() {
        SecurityManager sm = new SecurityManager();
        check(sm.getThreadGroup() == Thread.currentThread().getThreadGroup(),
              "getThreadGroup returns current thread's group");
        Object ctx = sm.getSecurityContext();
        check(true, "getSecurityContext callable");
    }

    // ================= 契约深化（2026-11）追加 =================
    // Exposes the protected getClassContext().
    static class CtxSM extends SecurityManager {
        Class<?>[] ctx() { return getClassContext(); }
    }

    interface CheckAction { void run() throws Exception; }

    static void expectAccessControlException(CheckAction r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected AccessControlException");
        } catch (java.security.AccessControlException expected) {
            passed++;
        } catch (Exception other) {
            throw new AssertionError(msg + ": unexpected " + other);
        }
    }

    static void expectSecurityException(CheckAction r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected SecurityException");
        } catch (SecurityException expected) {
            passed++;
        } catch (Exception other) {
            throw new AssertionError(msg + ": unexpected " + other);
        }
    }

    static void expectNpe(CheckAction r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        } catch (Exception other) {
            throw new AssertionError(msg + ": unexpected " + other);
        }
    }

    static void testWhiteBox() throws Exception {
        check(Class.forName("java.lang.SecurityManager") != null, "class loadable");
        check(!SecurityManager.class.isInterface(), "not an interface");
        check(SecurityManager.class.getSuperclass() == Object.class, "extends Object");
        check(!java.lang.reflect.Modifier.isFinal(SecurityManager.class.getModifiers()),
              "not final (subclassable)");
        check(SecurityManager.class.getConstructors().length == 1, "exactly 1 public ctor");
        check(SecurityManager.class.getConstructor() != null, "no-arg ctor present");
        // JDK-8: SecurityManager is NOT deprecated (JDK-27 deprecates for removal).
        check(SecurityManager.class.getAnnotation(Deprecated.class) == null,
              "not @Deprecated on JDK-8");
        check(System.getSecurityManager() == null, "no SecurityManager installed");
    }

    // With NO SecurityManager installed (always the case in this suite), the
    // check* methods still evaluate permissions through AccessController
    // against the caller's protection domain: the application domain holds
    // "exitVM", "stopThread" and the default policy grants, so those checks
    // pass silently, while ungranted permissions are denied with
    // AccessControlException. All locked empirically on temurin 8u372.
    static void testNoThrowChecks() {
        SecurityManager sm = new SecurityManager();
        sm.checkExit(1);
        passed++;
        sm.checkExit(-1);
        passed++;
        sm.checkExit(0);
        passed++;
        sm.checkPermission(new java.lang.RuntimePermission("exitVM"));
        passed++;
        sm.checkPermission(new java.lang.RuntimePermission("stopThread"));
        passed++;
        sm.checkPackageAccess("java.lang");
        passed++;
        sm.checkPropertyAccess("java.version");
        passed++;
        sm.checkAccess(Thread.currentThread());
        passed++;
        sm.checkAccess(Thread.currentThread().getThreadGroup());
        passed++;
    }

    // Absolute paths deliberately OUTSIDE the test codebase so the app-domain
    // FilePermission("<codebase>/-", "read") grant can never satisfy them.
    static final String ABS = "/jck/lcF/abs/somefile";

    static void testDeniedChecks() throws Exception {
        SecurityManager sm = new SecurityManager();
        expectAccessControlException(() -> sm.checkRead(ABS), "checkRead(String)");
        expectAccessControlException(() -> sm.checkWrite(ABS), "checkWrite(String)");
        expectAccessControlException(() -> sm.checkDelete(ABS), "checkDelete(String)");
        expectAccessControlException(() -> sm.checkRead(new java.io.FileDescriptor()),
                                      "checkRead(FileDescriptor)");
        expectAccessControlException(() -> sm.checkConnect("lcF-invalid.example", 80),
                                      "checkConnect(host,port)");
        expectAccessControlException(() -> sm.checkLink("libfoo.so"), "checkLink");
        expectAccessControlException(() -> sm.checkExec("/bin/ls"), "checkExec");
        expectAccessControlException(() -> sm.checkListen(8080), "checkListen");
        expectAccessControlException(() -> sm.checkPropertiesAccess(), "checkPropertiesAccess");
        expectAccessControlException(() -> sm.checkCreateClassLoader(), "checkCreateClassLoader");
        expectAccessControlException(() -> sm.checkSetFactory(), "checkSetFactory");
        expectAccessControlException(() -> sm.checkPrintJobAccess(), "checkPrintJobAccess");
        expectAccessControlException(() -> sm.checkSecurityAccess("getPolicy"), "checkSecurityAccess");

        // The denied exception carries the exact permission that failed.
        try {
            sm.checkRead(ABS);
            throw new AssertionError("expected AccessControlException");
        } catch (java.security.AccessControlException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("access denied"),
                  "ACE message starts with 'access denied'");
            check(e.getPermission() instanceof java.io.FilePermission, "ACE permission is FilePermission");
            check(ABS.equals(e.getPermission().getName()), "ACE permission names the path");
            check("read".equals(e.getPermission().getActions()), "ACE permission action read");
        }
        try {
            sm.checkCreateClassLoader();
            throw new AssertionError("expected AccessControlException");
        } catch (java.security.AccessControlException e) {
            check("createClassLoader".equals(e.getPermission().getName()),
                  "ACE permission createClassLoader");
        }
        try {
            sm.checkLink("libfoo.so");
            throw new AssertionError("expected AccessControlException");
        } catch (java.security.AccessControlException e) {
            check("loadLibrary.libfoo.so".equals(e.getPermission().getName()),
                  "ACE permission loadLibrary.<name>");
        }
    }

    static void testContextChecks() throws Exception {
        SecurityManager sm = new SecurityManager();
        Object acc = sm.getSecurityContext();
        check(acc instanceof java.security.AccessControlContext, "getSecurityContext is an ACC");
        // A context argument that is not an AccessControlContext is rejected
        // outright with SecurityException (both overloads).
        expectSecurityException(
                () -> sm.checkPermission(new java.lang.RuntimePermission("createClassLoader"), "not-an-acc"),
                "checkPermission with non-ACC context");
        expectSecurityException(() -> sm.checkRead(ABS, "not-an-acc"),
                                "checkRead with non-ACC context");
        // A genuine ACC context is evaluated: ungranted permission denied.
        expectAccessControlException(
                () -> sm.checkPermission(new java.lang.RuntimePermission("createClassLoader"), acc),
                "checkPermission with ACC context");
        expectAccessControlException(() -> sm.checkRead(ABS, acc), "checkRead with ACC context");
        // The captured context includes this code, so a granted permission passes.
        sm.checkPermission(new java.lang.RuntimePermission("exitVM"), acc);
        passed++;
    }

    static void testNullArguments() throws Exception {
        SecurityManager sm = new SecurityManager();
        try {
            sm.checkPermission(null);
            throw new AssertionError("checkPermission(null): expected NPE");
        } catch (NullPointerException e) {
            check("permission can't be null".equals(e.getMessage()),
                  "checkPermission(null) exact message [JDK8]");
        }
        try {
            sm.checkPropertyAccess(null);
            throw new AssertionError("checkPropertyAccess(null): expected NPE");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()),
                  "checkPropertyAccess(null) exact message [JDK8]");
        }
        try {
            sm.checkRead((String) null);
            throw new AssertionError("checkRead(null): expected NPE");
        } catch (NullPointerException e) {
            check("name can't be null".equals(e.getMessage()), "checkRead(null) exact message [JDK8]");
        }
        expectNpe(() -> sm.checkAccess((Thread) null), "checkAccess(null Thread)");
        try {
            sm.checkAccess((Thread) null);
            throw new AssertionError("unreachable");
        } catch (NullPointerException e) {
            check("thread can't be null".equals(e.getMessage()),
                  "checkAccess(null Thread) exact message [JDK8]");
        }
        try {
            sm.checkAccess((ThreadGroup) null);
            throw new AssertionError("checkAccess(null ThreadGroup): expected NPE");
        } catch (NullPointerException e) {
            check("thread group can't be null".equals(e.getMessage()),
                  "checkAccess(null ThreadGroup) exact message [JDK8]");
        }
    }

    static void testClassContext() {
        CtxSM sm = new CtxSM();
        Class<?>[] ctx = sm.ctx();
        check(ctx != null && ctx.length >= 2, "getClassContext non-empty");
        check(ctx[0].getName().equals("SecurityManagerTest$CtxSM"),
              "getClassContext[0] is the SM subclass");
        check(ctx[1].getName().equals("SecurityManagerTest"),
              "getClassContext[1] is the caller");
        CtxSM sm2 = new CtxSM();
        check(sm2.ctx().length >= 2, "getClassContext repeatable");
    }

    public static void main(String[] args) throws Exception {
        testSecurityManager();
        testWhiteBox();
        testNoThrowChecks();
        testDeniedChecks();
        testContextChecks();
        testNullArguments();
        testClassContext();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
