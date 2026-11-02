/*
 * @test
 * @summary java.rmi.UnexpectedException conformance: hierarchy and constructors.
 * @run main UnexpectedExceptionTest
 */

import java.rmi.UnexpectedException;

public class UnexpectedExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(UnexpectedException.class), "extends java.rmi.RemoteException");
        UnexpectedException e = new UnexpectedException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        UnexpectedException e2 = new UnexpectedException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
