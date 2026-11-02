/*
 * @test
 * @summary java.rmi.server.ExportException conformance: hierarchy and constructors.
 * @run main ExportExceptionTest
 */

import java.rmi.server.ExportException;

public class ExportExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(ExportException.class), "extends java.rmi.RemoteException");
        ExportException e = new ExportException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ExportException e2 = new ExportException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
