/*
 * @test
 * @summary java.rmi.server.SkeletonMismatchException conformance: hierarchy and constructor.
 * @run main SkeletonMismatchExceptionTest
 */

import java.rmi.server.SkeletonMismatchException;

public class SkeletonMismatchExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(SkeletonMismatchException.class), "extends RemoteException");
        SkeletonMismatchException e = new SkeletonMismatchException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
