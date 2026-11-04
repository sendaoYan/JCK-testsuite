/*
 * @test
 * @summary java.security.AccessController conformance: full contract coverage.
 *          getContext, doPrivileged(PrivilegedAction), doPrivileged(PrivilegedAction, ctx),
 *          doPrivileged(PrivilegedExceptionAction) success and wrapped exception, and
 *          checkPermission(null).
 * @run main AccessControllerTest
 */

import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;

public class AccessControllerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        // ---- getContext ----
        AccessControlContext ctx = AccessController.getContext();
        check(ctx != null, "getContext non-null");

        // ---- doPrivileged(PrivilegedAction) ----
        String r = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() { return "result"; }
        });
        check("result".equals(r), "doPrivileged(action) returns result");

        // ---- doPrivileged(PrivilegedAction, AccessControlContext) ----
        String r2 = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() { return "result2"; }
        }, ctx);
        check("result2".equals(r2), "doPrivileged(action, ctx) returns result");

        // ---- doPrivileged(PrivilegedExceptionAction) success ----
        String r3 = AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
            public String run() { return "result3"; }
        });
        check("result3".equals(r3), "doPrivileged(exceptionAction) returns result");

        // ---- doPrivileged(PrivilegedExceptionAction) wrapped exception ----
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                public String run() throws Exception { throw new IOException("boom"); }
            });
            throw new AssertionError("expected PrivilegedActionException");
        } catch (PrivilegedActionException e) {
            check(e.getException() instanceof IOException, "PAE wraps IOException");
            passed++;
        }

        // ---- checkPermission(null) -> AccessControlException (JDK-27 default policy denies) ----
        checkThrows(AccessControlException.class,
                () -> AccessController.checkPermission(null), "checkPermission(null)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
