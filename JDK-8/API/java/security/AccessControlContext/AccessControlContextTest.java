/*
 * @test
 * @summary java.security.AccessControlContext conformance: full contract coverage.
 *          constructors (ProtectionDomain[] and (AccessControlContext, DomainCombiner)),
 *          getDomainCombiner, equals/hashCode value semantics, and checkPermission.
 * @run main AccessControlContextTest
 */

import java.security.AccessControlContext;
import java.security.ProtectionDomain;

public class AccessControlContextTest {
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
        // ---- ProtectionDomain[] constructor + getDomainCombiner ----
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        check(acc.getDomainCombiner() == null, "getDomainCombiner null");

        // ---- (AccessControlContext, DomainCombiner) constructor ----
        AccessControlContext acc2 = new AccessControlContext(acc, null);
        check(acc2.getDomainCombiner() == null, "2-arg ctor getDomainCombiner null");

        // ---- equals/hashCode value semantics ----
        AccessControlContext same = new AccessControlContext(new ProtectionDomain[0]);
        check(acc.equals(same), "equals same empty domains");
        check(acc.hashCode() == same.hashCode(), "hashCode same empty domains");
        check(acc.equals(acc), "equals self");
        check(!acc.equals(null), "equals null false");
        check(!acc.equals("not a context"), "equals unrelated type false");

        // ---- checkPermission(null) -> NPE (no SecurityManager installed) ----
        final AccessControlContext accN = acc;
        checkThrows(NullPointerException.class, () -> accN.checkPermission(null),
                "checkPermission(null)");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: combiner round-trip, denied permission identity, equality over domains ----
    static void testContractDeep() throws Exception {
        // null domain array is rejected by the public constructor
        checkThrows(NullPointerException.class, () -> new AccessControlContext((ProtectionDomain[]) null),
                "ctor null domain array");

        // combiner round-trip via the (ctx, combiner) constructor
        java.security.DomainCombiner dc = new java.security.DomainCombiner() {
            public ProtectionDomain[] combine(ProtectionDomain[] current, ProtectionDomain[] assigned) {
                return current;
            }
        };
        AccessControlContext base = new AccessControlContext(new ProtectionDomain[0]);
        AccessControlContext withCombiner = new AccessControlContext(base, dc);
        check(withCombiner.getDomainCombiner() == dc, "combiner round-trip identity");
        check(new AccessControlContext(base, null).getDomainCombiner() == null, "null combiner legal");
        // combiner DOES participate in equals: same domains + different combiners are unequal
        check(!withCombiner.equals(new AccessControlContext(base, null)),
                "combiner participates in equals");
        check(withCombiner.equals(new AccessControlContext(base, dc)),
                "same combiner stays equal");

        // checkPermission denial carries the exact Permission (JDK-8, no SM)
        java.security.Permissions none = new java.security.Permissions();
        ProtectionDomain locked = new ProtectionDomain(null, none);
        AccessControlContext denied = new AccessControlContext(new ProtectionDomain[] { locked });
        final java.security.SecurityPermission sp = new java.security.SecurityPermission("setLog");
        try {
            denied.checkPermission(sp);
            check(false, "empty-domain ACC must deny");
        } catch (java.security.AccessControlException ace) {
            check(ace.getPermission() == sp, "ACE permission identity (JDK-8)");
        }
        // an AllPermission domain in the same array grants everything (union semantics)
        java.security.Permissions all = new java.security.Permissions();
        all.add(new java.security.AllPermission());
        ProtectionDomain open = new ProtectionDomain(null, all);
        AccessControlContext granted = new AccessControlContext(new ProtectionDomain[] { open });
        granted.checkPermission(sp);
        check(true, "AllPermission domain grants within its ACC");
        // ACC.checkPermission is INTERSECTION semantics: every domain in the context must imply
        AccessControlContext mixed = new AccessControlContext(new ProtectionDomain[] { locked, open });
        try {
            mixed.checkPermission(sp);
            check(false, "one non-implying domain must deny the whole context");
        } catch (java.security.AccessControlException ace) {
            check(ace.getPermission() == sp, "intersection denial carries permission");
        }

        // equals over ProtectionDomain contents: same array -> equal, different -> not equal
        AccessControlContext a1 = new AccessControlContext(new ProtectionDomain[] { locked, open });
        AccessControlContext a2 = new AccessControlContext(new ProtectionDomain[] { locked, open });
        check(a1.equals(a2) && a1.hashCode() == a2.hashCode(), "same domain sequence value-equal");
        check(a1.equals(new AccessControlContext(new ProtectionDomain[] { open, locked })),
                "domain order does NOT matter for equality (set semantics)");

        // doPrivileged with a captured context runs under that context's domains
        AccessControlContext snap = java.security.AccessController.getContext();
        Integer r = java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Integer>() {
            public Integer run() { return 42; }
        }, snap);
        check(r == 42, "doPrivileged with captured context runs");
        // a snapshot of the (AllPermission) context does not grant beyond its own domains
        try {
            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Integer>() {
                public Integer run() {
                    denied.checkPermission(sp);
                    return 1;
                }
            });
            check(false, "privileged action inherits denied context");
        } catch (java.security.AccessControlException ace) {
            check(ace.getPermission() == sp, "denied context propagated into action");
        }

        // AccessControlContext is NOT Serializable
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(base);
            check(false, "ACC must not be serializable");
        } catch (java.io.NotSerializableException nse) {
            check(true, "ACC not serializable");
        }
        check(base.toString() != null, "toString non-null");

        // a null element inside the domain array is accepted by the constructor
        check(new AccessControlContext(new ProtectionDomain[] { null }).getDomainCombiner() == null,
                "null domain element tolerated at construction");
        // two snapshots taken in the same frame are value-equal
        check(java.security.AccessController.getContext().equals(java.security.AccessController.getContext()),
                "same-frame getContext snapshots equal");
        // a snapshot's combiner slot is empty in a plain frame
        check(snap.getDomainCombiner() == null, "plain-frame snapshot has no combiner");
        // JDK-8: a ZERO-domain context grants everything (vacuously true check)
        new AccessControlContext(new ProtectionDomain[0]).checkPermission(sp);
        check(true, "zero-domain ACC grants (vacuous, JDK-8)");
        check(base.toString().startsWith("java.security.AccessControlContext@"),
                "toString class@identityHash form");
    }
}
