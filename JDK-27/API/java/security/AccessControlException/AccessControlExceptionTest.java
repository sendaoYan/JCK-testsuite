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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening (JDK-27): ctor surface, serialization, engine ACE null permission ----
    static void testContractDeep() throws Exception {
        Permission p = new SecurityPermission("jck.perm2");
        AccessControlException e2 = new AccessControlException("denied", p);
        check(AccessControlException.class.getConstructors().length == 2, "exactly two public constructors");
        check(new AccessControlException("m", null).getPermission() == null, "null permission legal");
        check(e2.toString().startsWith("java.security.AccessControlException: denied"), "toString form");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e2.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString().contains("java.security.AccessControlException"), "printStackTrace shows class");

        // cause slot starts empty; initCause once then IllegalStateException
        RuntimeException cause = new RuntimeException("root");
        e2.initCause(cause);
        check(e2.getCause() == cause, "initCause once succeeds");
        try {
            e2.initCause(new RuntimeException("second"));
            check(false, "initCause twice must fail");
        } catch (IllegalStateException ise) {
            check(true, "initCause twice -> IllegalStateException");
        }

        // serialization round-trip preserves message and permission
        AccessControlException rt = (AccessControlException) roundTrip(new AccessControlException("ser", p));
        check("ser".equals(rt.getMessage()), "round-trip message");
        check(rt.getPermission() != null && "jck.perm2".equals(rt.getPermission().getName()),
                "round-trip permission preserved by value");

        // engine wiring (JDK-27, JEP 486): denial ACE carries a NULL permission even though
        // a concrete Permission object was passed to checkPermission
        try {
            AccessController.checkPermission(new SecurityPermission("anything.else"));
            check(false, "must be denied");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == null, "engine ACE permission is null (JDK-27)");
        }
        try {
            AccessController.getContext().checkPermission(p);
            check(false, "context denial expected");
        } catch (AccessControlException ace) {
            check(ace.getPermission() == null, "context ACE permission is null (JDK-27)");
        }

        // single-arg serialization: permission stays null across the round-trip
        AccessControlException rt0 = (AccessControlException) roundTrip(new AccessControlException("boom"));
        check(rt0.getPermission() == null, "round-trip single-arg permission null");
        check("boom".equals(rt0.getMessage()), "round-trip single-arg message");
        // permission survives initCause
        check(e2.getPermission() == p && e2.getCause() != null, "permission retained after initCause");
    }

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return oin.readObject();
    }
}
