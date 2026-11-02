/*
 * @test
 * @summary javax.activity.InvalidActivityException conformance: exception hierarchy and constructors.
 * @run main InvalidActivityExceptionTest
 */

public class InvalidActivityExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(javax.activity.InvalidActivityException.class), "extends RemoteException");
        javax.activity.InvalidActivityException e = new javax.activity.InvalidActivityException("m");
        check("m".equals(e.getMessage()), "getMessage");
        javax.activity.InvalidActivityException e2 = new javax.activity.InvalidActivityException(new RuntimeException("c"));
        check(e2.getCause() != null, "cause");
        javax.activity.InvalidActivityException e3 = new javax.activity.InvalidActivityException("m2", new RuntimeException("c2"));
        check(e3.getMessage().startsWith("m2"), "message with cause");
        check(e3.getCause() != null, "cause 2");
        check(new javax.activity.InvalidActivityException() != null, "no-arg ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
