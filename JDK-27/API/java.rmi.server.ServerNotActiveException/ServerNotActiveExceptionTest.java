/*
 * @test
 * @summary java.rmi.server.ServerNotActiveException conformance: hierarchy and constructors.
 * @run main ServerNotActiveExceptionTest
 */

import java.rmi.server.ServerNotActiveException;

public class ServerNotActiveExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.lang.Exception.class.isAssignableFrom(ServerNotActiveException.class), "extends Exception");
        ServerNotActiveException e = new ServerNotActiveException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ServerNotActiveException e2 = new ServerNotActiveException();
        check(e2 != null, "default ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
