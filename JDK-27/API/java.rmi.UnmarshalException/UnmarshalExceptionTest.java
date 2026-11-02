/*
 * @test
 * @summary java.rmi.UnmarshalException conformance: hierarchy and constructors.
 * @run main UnmarshalExceptionTest
 */

import java.rmi.UnmarshalException;

public class UnmarshalExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(UnmarshalException.class), "extends java.rmi.RemoteException");
        UnmarshalException e = new UnmarshalException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        UnmarshalException e2 = new UnmarshalException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
