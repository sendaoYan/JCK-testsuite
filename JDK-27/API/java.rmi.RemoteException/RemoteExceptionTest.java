/*
 * @test
 * @summary java.rmi.RemoteException conformance: hierarchy, detail and cause.
 * @run main RemoteExceptionTest
 */

import java.rmi.RemoteException;

public class RemoteExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.io.IOException.class.isAssignableFrom(RemoteException.class), "extends IOException");
        RemoteException e0 = new RemoteException();
        check(e0 != null && e0.getMessage() == null, "default ctor message null");
        RemoteException e1 = new RemoteException("detail-msg");
        check("detail-msg".equals(e1.getMessage()), "message");
        RemoteException e2 = new RemoteException("detail-msg", new Exception("inner"));
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        check(e2.getCause() != null && "inner".equals(e2.getCause().getMessage()), "cause");
        check(e2.detail != null, "detail field");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
