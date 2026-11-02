/*
 * @test
 * @summary java.rmi.AccessException conformance: hierarchy and constructors.
 * @run main AccessExceptionTest
 */

import java.rmi.AccessException;

public class AccessExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(AccessException.class), "extends java.rmi.RemoteException");
        AccessException e = new AccessException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        AccessException e2 = new AccessException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
