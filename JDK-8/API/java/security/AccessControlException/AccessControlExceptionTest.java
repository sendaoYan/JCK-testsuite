/*
 * @test
 * @summary java.security.AccessControlException conformance: constructors, message, cause and superclass.
 * @run main AccessControlExceptionTest
 */

public class AccessControlExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AccessControlException e = new java.security.AccessControlException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.SecurityException, "extends java.lang.SecurityException");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: (String,Permission) ctor, identity, serialization, engine wiring ----
    static void testContractDeep() throws Exception {
        java.security.Permission sp = new java.security.SecurityPermission("jck.perm");

        // (String) ctor: permission field null, cause null
        check(e0().getPermission() == null, "single-arg ctor permission null");
        check(e0().getCause() == null, "single-arg ctor cause null");

        // (String, Permission) ctor: exact message and permission identity
        java.security.AccessControlException e2 = new java.security.AccessControlException("denied", sp);
        check("denied".equals(e2.getMessage()), "two-arg message");
        check(e2.getPermission() == sp, "two-arg permission identity");
        check(e2.getCause() == null, "two-arg cause null");

        // (String, null) permission is legal
        java.security.AccessControlException e3 = new java.security.AccessControlException("m", null);
        check(e3.getPermission() == null, "null permission legal");

        // type hierarchy: RuntimeException via SecurityException; no cause-chaining constructors
        check(e2 instanceof RuntimeException, "RuntimeException subtype");
        check(java.security.AccessControlException.class.getConstructors().length == 2,
                "exactly two public constructors");

        // toString / printStackTrace
        check(e0().toString().startsWith("java.security.AccessControlException: boom"), "toString form");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        e2.printStackTrace(new java.io.PrintStream(bos));
        check(bos.toString().contains("java.security.AccessControlException"), "printStackTrace shows class");

        // Throwable machinery: cause slot starts empty, initCause fills it once
        java.security.AccessControlException e4 = new java.security.AccessControlException("m", sp);
        RuntimeException cause = new RuntimeException("root");
        e4.initCause(cause);
        check(e4.getCause() == cause, "initCause once succeeds");
        try {
            e4.initCause(new RuntimeException("second"));
            check(false, "initCause twice must fail");
        } catch (IllegalStateException ise) {
            check(true, "initCause twice -> IllegalStateException");
        }

        // serialization round-trip preserves message and permission
        java.security.AccessControlException rt =
                (java.security.AccessControlException) roundTrip(e2);
        check("denied".equals(rt.getMessage()), "round-trip message");
        check(rt.getPermission() != null && "jck.perm".equals(rt.getPermission().getName()),
                "round-trip permission preserved by value");
        check(rt instanceof SecurityException, "round-trip type");

        // engine wiring: AccessController.checkPermission denial carries the exact Permission (JDK-8)
        try {
            java.security.AccessController.checkPermission(sp);
            check(false, "ungranted permission must be denied");
        } catch (java.security.AccessControlException ace) {
            check(ace.getPermission() == sp, "engine ACE permission identity (JDK-8)");
            check(ace.getMessage() != null && ace.getMessage().contains("jck.perm"),
                    "engine ACE message contains permission name");
        }
        // and the same denial through AccessControlContext.checkPermission
        try {
            java.security.AccessController.getContext().checkPermission(sp);
            check(false, "context denial expected");
        } catch (java.security.AccessControlException ace) {
            check(ace.getPermission() == sp, "context ACE permission identity (JDK-8)");
        }

        // single-arg serialization: permission stays null across the round-trip
        java.security.AccessControlException rt0 =
                (java.security.AccessControlException) roundTrip(e0());
        check(rt0.getPermission() == null, "round-trip single-arg permission null");
        check("boom".equals(rt0.getMessage()), "round-trip single-arg message");
        // permission survives initCause and rethrow
        java.security.AccessControlException e5 = new java.security.AccessControlException("m", sp);
        e5.initCause(new RuntimeException("root"));
        check(e5.getPermission() == sp, "permission retained after initCause");
        check(e5.getCause() instanceof RuntimeException, "cause retained alongside permission");
        // exact runtime class survives catch-by-supertype unwinding
        try {
            throw new java.security.AccessControlException("thrown", sp);
        } catch (SecurityException se) {
            check(se.getClass() == java.security.AccessControlException.class,
                    "exact class when caught as SecurityException");
            check(((java.security.AccessControlException) se).getPermission() == sp,
                    "permission intact after unwinding");
        }
    }

    static java.security.AccessControlException e0() {
        return new java.security.AccessControlException("boom");
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
