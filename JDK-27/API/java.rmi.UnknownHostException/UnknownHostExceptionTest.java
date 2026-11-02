/*
 * @test
 * @summary java.rmi.UnknownHostException conformance: hierarchy and constructors.
 * @run main UnknownHostExceptionTest
 */

import java.rmi.UnknownHostException;

public class UnknownHostExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(UnknownHostException.class), "extends java.rmi.RemoteException");
        UnknownHostException e = new UnknownHostException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        UnknownHostException e2 = new UnknownHostException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
