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

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening (JDK-27): JEP 486 semantics - no permission is ever granted ----
    static final int[] COMBINED = { 0 };
    static final java.security.DomainCombiner COUNTING_COMBINER = new java.security.DomainCombiner() {
        public ProtectionDomain[] combine(ProtectionDomain[] current, ProtectionDomain[] assigned) {
            COMBINED[0]++;
            return current;
        }
    };

    static void testContractDeep() throws Exception {
        // doPrivileged may return null legitimately
        check(AccessController.doPrivileged((PrivilegedAction<Object>) () -> null) == null,
                "doPrivileged null return passes through");
        // doPrivileged(action, null): null context is legal
        check(AccessController.doPrivileged((PrivilegedAction<Integer>) () -> 7, null) == 7,
                "doPrivileged(action, null) legal");
        // RuntimeException is NOT wrapped in PrivilegedActionException
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Integer>) () -> {
                throw new IllegalStateException("rte");
            });
            check(false, "RTE must propagate");
        } catch (IllegalStateException e) {
            check("rte".equals(e.getMessage()), "RTE propagates unwrapped");
        }
        try {
            AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
                throw new UnsupportedOperationException("uoe");
            });
            check(false, "RTE from action must propagate");
        } catch (UnsupportedOperationException e) {
            check("uoe".equals(e.getMessage()), "RTE from action propagates");
        }
        // Error likewise direct
        try {
            AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
                throw new AssertionError("err-prop");
            });
            check(false, "Error must propagate");
        } catch (AssertionError err) {
            check("err-prop".equals(err.getMessage()), "Error propagates directly");
        }
        // PAE identity: getException() == getCause()
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Integer>) () -> {
                throw new IOException("boom");
            });
        } catch (PrivilegedActionException pae) {
            check(pae.getException() == pae.getCause(), "getException == getCause");
            check(pae.getCause() instanceof IOException && "boom".equals(pae.getCause().getMessage()),
                    "cause identity preserved");
        }
        check(AccessController.doPrivileged((PrivilegedExceptionAction<Integer>) () -> 3, null) == 3,
                "doPrivileged(PEA, null) legal");

        // ---- permission matrix (JDK-27, JEP 486): every check is denied, permission field is null ----
        final java.security.SecurityPermission sp = new java.security.SecurityPermission("setLog");
        try {
            AccessController.checkPermission(sp);
            check(false, "check must be denied");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == null, "JDK-27 ACE carries null permission (never granted)");
        }
        // even RuntimePermission("exitVM") that JDK-8 grants statically is denied here
        int inPriv = AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
            try { AccessController.checkPermission(new RuntimePermission("exitVM")); return 1; }
            catch (AccessControlException e) { return 2; }
        });
        check(inPriv == 2, "RuntimePermission(exitVM) denied even in privileged frame (JDK-27)");
        try {
            AccessController.getContext().checkPermission(sp);
            check(false, "ctx.checkPermission must deny");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == null, "ctx.checkPermission ACE null permission (JDK-27)");
        }
        // two getContext() snapshots still compare equal by value
        check(AccessController.getContext().equals(AccessController.getContext()),
                "getContext snapshots value-equal");

        // ---- DomainCombiner timing (JDK-27): combiners are retained but never invoked ----
        ProtectionDomain[] pds = { new ProtectionDomain(null, new java.security.Permissions()) };
        AccessControlContext accC = new AccessControlContext(new AccessControlContext(pds), COUNTING_COMBINER);
        check(accC.getDomainCombiner() == COUNTING_COMBINER, "ACC keeps combiner reference");
        int before = COMBINED[0];
        AccessController.doPrivileged((PrivilegedAction<Integer>) () -> 5, accC);
        AccessController.doPrivilegedWithCombiner((PrivilegedAction<Integer>) () -> 9);
        AccessController.doPrivilegedWithCombiner((PrivilegedAction<Integer>) () -> 10, accC);
        AccessController.doPrivilegedWithCombiner((PrivilegedExceptionAction<Integer>) () -> 11);
        check(COMBINED[0] == before, "JDK-27 never invokes DomainCombiner (SM machinery removed)");
        check(AccessController.doPrivilegedWithCombiner((PrivilegedAction<Integer>) () -> 10, accC) == 10,
                "doPrivilegedWithCombiner(action, ctx) still returns the action result");

        // ---- further JDK-27 facts: AllPermission static domain still denied; deprecation shape ----
        java.security.Permissions all = new java.security.Permissions();
        all.add(new java.security.AllPermission());
        ProtectionDomain allDomain = new ProtectionDomain(null, all);
        AccessControlContext allCtx = new AccessControlContext(new ProtectionDomain[] { allDomain });
        try {
            allCtx.checkPermission(sp);
            check(false, "AllPermission ACC must still be denied (JEP 486)");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == null, "AllPermission denial ACE null permission");
        }
        // AccessController itself is terminally deprecated for removal
        check(AccessController.class.isAnnotationPresent(Deprecated.class),
                "AccessController is @Deprecated (for removal)");
        check(AccessController.class.getAnnotation(Deprecated.class).forRemoval(),
                "deprecation is forRemoval=true");
        // nested privileged frames nest correctly and results pass through
        Integer nested = AccessController.doPrivileged((PrivilegedAction<Integer>) () ->
                AccessController.doPrivileged((PrivilegedAction<Integer>) () -> 77));
        check(nested == 77, "nested doPrivileged frames compose");
    }
}
