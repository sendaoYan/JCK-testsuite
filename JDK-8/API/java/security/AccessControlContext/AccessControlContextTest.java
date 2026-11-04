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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
