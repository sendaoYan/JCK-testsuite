/*
 * @test
 * @summary java.security.AccessController conformance: white-box structure + black-box behavior.
 * @run main AccessControllerTest
 */

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.*;
import java.util.*;

public class AccessControllerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        String r = AccessController.doPrivileged((PrivilegedAction<String>) () -> "result");
        check("result".equals(r), "doPrivileged returns value");
        check(AccessController.getContext() != null, "getContext non-null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // doPrivileged(PrivilegedAction): runs and returns the action result
        String r1 = AccessController.doPrivileged((PrivilegedAction<String>) () -> "jck-value");
        check(r1.equals("jck-value"), "doPrivileged value");
        // PrivilegedAction returning null propagates null
        check(AccessController.doPrivileged((PrivilegedAction<Object>) () -> null) == null, "null result");
        // doPrivileged with AccessControlContext argument behaves identically
        AccessControlContext acc = AccessController.getContext();
        check("doPrivileged(acc)".equals(AccessController.doPrivileged(
            (PrivilegedAction<String>) () -> "doPrivileged(acc)", acc)), "doPrivileged with acc");
        // unchecked exception from the action propagates unwrapped
        try {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> { throw new IllegalStateException("boom"); });
            check(false, "unchecked propagates");
        } catch (IllegalStateException ise) {
            check("boom".equals(ise.getMessage()), "unchecked unwrapped");
        }
        // doPrivileged(PrivilegedExceptionAction): normal return
        int r2 = AccessController.doPrivileged((PrivilegedExceptionAction<Integer>) () -> 41 + 1);
        check(r2 == 42, "PEA result");
        // checked exception from PEA is wrapped in PrivilegedActionException
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new CloneNotSupportedException("nope"); });
            check(false, "should wrap");
        } catch (PrivilegedActionException pae) {
            check(pae.getCause() instanceof CloneNotSupportedException, "PAE wraps checked");
            check(pae.getException() == pae.getCause(), "getException == cause");
        }
        // runtime exception from PEA is NOT wrapped
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new IllegalArgumentException("direct"); });
            check(false, "runtime not wrapped");
        } catch (IllegalArgumentException iae) {
            check("direct".equals(iae.getMessage()), "runtime direct");
        }
        // PEA with acc argument
        try {
            AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> { throw new CloneNotSupportedException(); }, acc);
            check(false, "wrap with acc");
        } catch (PrivilegedActionException pae) {
            check(true, "PAE with acc");
        }
        // getContext returns a snapshot; repeated snapshots are equal
        AccessControlContext c1 = AccessController.getContext();
        AccessControlContext c2 = AccessController.getContext();
        check(c1 != null && c1.equals(c2) && c1.hashCode() == c2.hashCode(), "context snapshot equals");
        // checkPermission: SecurityManager permanently removed, always AccessControlException
        try {
            AccessController.checkPermission(new AllPermission());
            check(false, "checkPermission throws");
        } catch (AccessControlException ace) {
            check("checking permissions is not supported".equals(ace.getMessage()), "checkPermission message");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
