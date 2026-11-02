/*
 * @test
 * @summary java.rmi.ConnectIOException conformance: hierarchy and constructors.
 * @run main ConnectIOExceptionTest
 */

import java.rmi.ConnectIOException;

public class ConnectIOExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(ConnectIOException.class), "extends java.rmi.RemoteException");
        ConnectIOException e = new ConnectIOException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ConnectIOException e2 = new ConnectIOException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
