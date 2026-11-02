/*
 * @test
 * @summary java.rmi.ConnectException conformance: hierarchy and constructors.
 * @run main ConnectExceptionTest
 */

import java.rmi.ConnectException;

public class ConnectExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(ConnectException.class), "extends java.rmi.RemoteException");
        ConnectException e = new ConnectException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ConnectException e2 = new ConnectException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
