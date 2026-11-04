/*
 * @test
 * @summary java.security.AccessControlContext conformance: white-box structure + black-box behavior.
 * @run main AccessControlContextTest
 */

import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.security.*;
import java.util.*;

public class AccessControlContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        check(acc.getDomainCombiner() == null, "getDomainCombiner null");
        AccessControlContext acc2 = new AccessControlContext(acc, null);
        check(acc2.getDomainCombiner() == null, "2-arg getDomainCombiner null");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // getContext-based instances: equality, hashCode, checkPermission
        AccessControlContext a = AccessController.getContext();
        AccessControlContext b = AccessController.getContext();
        check(a.equals(b), "equals");
        check(a.hashCode() == b.hashCode(), "hashCode");
        // direct construction with a domain array
        CodeSource cs = new CodeSource(new java.net.URL("file:/tmp/jck.jar"), (CodeSigner[]) null);
        ProtectionDomain pd = new ProtectionDomain(cs, null);
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[]{pd});
        check(acc != null, "constructed");
        // checkPermission always fails in the SecurityManager-less JDK
        try {
            acc.checkPermission(new SecurityPermission("x"));
            check(false, "checkPermission throws");
        } catch (AccessControlException ace) {
            check("checking permissions is not supported".equals(ace.getMessage()), "ace message");
        }
        // DomainCombiner-constructing overload retains the combiner
        DomainCombiner combiner = (current, assigned) -> current;
        AccessControlContext combined = new AccessControlContext(acc, combiner);
        check(combined.getDomainCombiner() == combiner, "combiner retained");
        check(a.getDomainCombiner() == null, "no combiner");
        // doPrivileged honors the combined context
        check("ok".equals(AccessController.doPrivileged((PrivilegedAction<String>) () -> "ok", combined)), "doPrivileged combined");
        // null array rejected
        try {
            new AccessControlContext(null);
            check(false, "null context NPE");
        } catch (NullPointerException npe) { check(true, "null context NPE"); }
        // two empty contexts are equal (empty domain arrays compare equal)
        check(new AccessControlContext(new ProtectionDomain[0]).equals(new AccessControlContext(new ProtectionDomain[0])),
            "empty equal");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
