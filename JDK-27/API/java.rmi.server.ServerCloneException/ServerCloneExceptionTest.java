/*
 * @test
 * @summary java.rmi.server.ServerCloneException conformance: hierarchy and constructors.
 * @run main ServerCloneExceptionTest
 */

import java.rmi.server.ServerCloneException;

public class ServerCloneExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(ServerCloneException.class.getSuperclass() == java.lang.CloneNotSupportedException.class, "extends CloneNotSupportedException");
        ServerCloneException e = new ServerCloneException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        ServerCloneException e2 = new ServerCloneException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
