/*
 * @test
 * @summary java.security.AccessControlException conformance: white-box structure + black-box behavior.
 * @run main AccessControlExceptionTest
 */

import java.security.AccessControlException;
import java.security.AllPermission;
import java.security.*;
import java.util.*;

public class AccessControlExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        AccessControlException e1 = new AccessControlException("msg");
        check("msg".equals(e1.getMessage()), "message from (String)");
        check(e1.getPermission() == null, "getPermission null");
        AllPermission perm = new AllPermission();
        AccessControlException e2 = new AccessControlException("msg2", perm);
        check(perm.equals(e2.getPermission()), "getPermission");
        check(e1 instanceof SecurityException, "extends SecurityException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Constructor and getters
        Permission p = new SecurityPermission("jck.perm");
        AccessControlException e1 = new AccessControlException("denied jck");
        check("denied jck".equals(e1.getMessage()), "message");
        check(e1.getPermission() == null, "null permission");
        AccessControlException e2 = new AccessControlException("denied with perm", p);
        check(e2.getPermission() == p, "permission");
        check("denied with perm".equals(e2.getMessage()), "message2");
        // it is a SecurityException
        check(e2 instanceof SecurityException, "SecurityException subtype");
        check(e2 instanceof RuntimeException, "RuntimeException subtype");
        // cause defaults to null
        check(e2.getCause() == null, "null cause");
        // real throw from AccessController
        try {
            AccessController.checkPermission(p);
            check(false, "expected ACE");
        } catch (AccessControlException ace) {
            check("checking permissions is not supported".equals(ace.getMessage()), "real message");
            check(ace instanceof SecurityException, "real is SecurityException");
        }
        // equality is identity-based like most exceptions
        check(!e1.equals(e2), "not equal");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
