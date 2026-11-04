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
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;

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

        // ---- checkPermission(null) -> NPE (no SecurityManager installed) ----
        checkThrows(NullPointerException.class,
                () -> AccessController.checkPermission(null), "checkPermission(null)");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: null returns, RTE passthrough, combiner timing, permission matrix ----
    static final int[] COMBINED = { 0 };
    static final DomainCombiner COUNTING_COMBINER = new DomainCombiner() {
        public ProtectionDomain[] combine(ProtectionDomain[] current, ProtectionDomain[] assigned) {
            COMBINED[0]++;
            return current;
        }
    };

    static void testContractDeep() throws Exception {
        // doPrivileged may return null legitimately
        check(AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() { return null; }
        }) == null, "doPrivileged null return passes through");

        // doPrivileged(action, null): null context is legal and behaves like no context
        check(AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() { return 7; }
        }, null) == 7, "doPrivileged(action, null) legal");

        // RuntimeException from the action is NOT wrapped in PrivilegedActionException
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
                public Integer run() { throw new IllegalStateException("rte"); }
            });
            check(false, "RTE must propagate");
        } catch (IllegalStateException e) {
            check("rte".equals(e.getMessage()), "RTE propagates unwrapped");
        }
        // and likewise for the PrivilegedAction form
        try {
            AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                public Integer run() { throw new UnsupportedOperationException("uoe"); }
            });
            check(false, "RTE from action must propagate");
        } catch (UnsupportedOperationException e) {
            check("uoe".equals(e.getMessage()), "RTE from action propagates");
        }
        // Error propagation is likewise direct
        try {
            AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                public Integer run() { throw new AssertionError("err-prop"); }
            });
            check(false, "Error must propagate");
        } catch (AssertionError err) {
            check("err-prop".equals(err.getMessage()), "Error propagates directly");
        }
        // PAE identity: getException() == getCause()
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
                public Integer run() throws Exception { throw new IOException("boom"); }
            });
        } catch (PrivilegedActionException pae) {
            check(pae.getException() == pae.getCause(), "getException == getCause");
            check(pae.getCause() instanceof IOException && "boom".equals(pae.getCause().getMessage()),
                    "cause identity preserved");
        }
        // doPrivileged(PEA, null ctx)
        check(AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
            public Integer run() { return 3; }
        }, null) == 3, "doPrivileged(PEA, null) legal");

        // ---- permission matrix without SecurityManager (JDK-8 recorded behavior) ----
        // JDK-8: RuntimePermission("exitVM") is granted to the current stack (no SM -> static grants)
        AccessController.checkPermission(new RuntimePermission("exitVM"));
        check(true, "RuntimePermission(exitVM) granted without SM (JDK-8)");
        // ... while SecurityPermission("setLog") is not, and checkPermission throws ACE carrying
        // exactly the denied permission object
        final Permission sp = new SecurityPermission("setLog");
        try {
            AccessController.checkPermission(sp);
            check(false, "setLog must be denied");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == sp, "ACE carries the exact denied Permission");
        }
        // inside doPrivileged the same asymmetry holds (privileged frame does not grant more)
        int inPriv = AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() {
                try { AccessController.checkPermission(sp); return 1; } catch (AccessControlException e) { return 2; }
            }
        });
        check(inPriv == 2, "privileged frame does not grant SecurityPermission (JDK-8)");
        // getContext().checkPermission mirrors AccessController.checkPermission
        try {
            AccessController.getContext().checkPermission(sp);
            check(false, "ctx.checkPermission must deny");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == sp, "ctx.checkPermission ACE permission identity");
        }
        // two getContext() snapshots compare equal by value
        check(AccessController.getContext().equals(AccessController.getContext()),
                "getContext snapshots value-equal");

        // ---- DomainCombiner invocation timing (JDK-8) ----
        ProtectionDomain[] pds = { new ProtectionDomain(null, new Permissions()) };
        AccessControlContext accC = new AccessControlContext(new AccessControlContext(pds), COUNTING_COMBINER);
        check(accC.getDomainCombiner() == COUNTING_COMBINER, "ACC keeps combiner reference");
        int before = COMBINED[0];
        AccessController.doPrivileged(new PrivilegedAction<Integer>() {
            public Integer run() { return 5; }
        }, accC);
        check(COMBINED[0] == before, "plain doPrivileged(action, ctx) does not invoke ctx combiner");
        AccessController.doPrivilegedWithCombiner(new PrivilegedAction<Integer>() {
            public Integer run() { return 9; }
        });
        check(COMBINED[0] == before, "doPrivilegedWithCombiner without ctx combiner: not invoked");
        AccessController.doPrivilegedWithCombiner(new PrivilegedAction<Integer>() {
            public Integer run() { return 10; }
        }, accC);
        check(COMBINED[0] == before + 1, "doPrivilegedWithCombiner(action, ctx) invokes ctx combiner exactly once (JDK-8)");
        AccessController.doPrivilegedWithCombiner(new PrivilegedExceptionAction<Integer>() {
            public Integer run() { return 11; }
        });
        check(true, "doPrivilegedWithCombiner(PEA) exists and runs");
    }
}
