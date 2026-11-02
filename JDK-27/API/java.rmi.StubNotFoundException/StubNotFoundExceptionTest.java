/*
 * @test
 * @summary java.rmi.StubNotFoundException conformance: hierarchy and constructors.
 * @run main StubNotFoundExceptionTest
 */

import java.rmi.StubNotFoundException;

public class StubNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(StubNotFoundException.class), "extends java.rmi.RemoteException");
        StubNotFoundException e = new StubNotFoundException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        StubNotFoundException e2 = new StubNotFoundException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
