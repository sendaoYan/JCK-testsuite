/*
 * @test
 * @summary java.rmi.server.SkeletonNotFoundException conformance: hierarchy and constructors.
 * @run main SkeletonNotFoundExceptionTest
 */

import java.rmi.server.SkeletonNotFoundException;

public class SkeletonNotFoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(SkeletonNotFoundException.class), "extends java.rmi.RemoteException");
        SkeletonNotFoundException e = new SkeletonNotFoundException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        SkeletonNotFoundException e2 = new SkeletonNotFoundException("detail-msg", new Exception("inner"));
        check(e2.getCause() != null, "cause set");
        check(e2.getMessage().startsWith("detail-msg"), "outer message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
