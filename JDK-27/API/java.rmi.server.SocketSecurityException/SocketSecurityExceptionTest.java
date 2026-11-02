/*
 * @test
 * @summary java.rmi.server.SocketSecurityException conformance: hierarchy and constructors.
 * @run main SocketSecurityExceptionTest
 */

import java.rmi.server.SocketSecurityException;

public class SocketSecurityExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.server.ExportException.class.isAssignableFrom(SocketSecurityException.class), "extends java.rmi.server.ExportException");
        SocketSecurityException e = new SocketSecurityException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        SocketSecurityException e2 = new SocketSecurityException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
