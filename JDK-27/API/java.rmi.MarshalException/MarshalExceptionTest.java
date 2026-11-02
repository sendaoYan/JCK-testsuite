/*
 * @test
 * @summary java.rmi.MarshalException conformance: hierarchy and constructors.
 * @run main MarshalExceptionTest
 */

import java.rmi.MarshalException;

public class MarshalExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(MarshalException.class), "extends java.rmi.RemoteException");
        MarshalException e = new MarshalException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        MarshalException e2 = new MarshalException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
