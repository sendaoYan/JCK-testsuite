/*
 * @test
 * @summary java.rmi.ServerException conformance: hierarchy and constructors.
 * @run main ServerExceptionTest
 */

import java.rmi.ServerException;

public class ServerExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(ServerException.class), "extends java.rmi.RemoteException");
        ServerException e = new ServerException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ServerException e2 = new ServerException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
