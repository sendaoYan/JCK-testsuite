/*
 * @test
 * @summary java.rmi.AlreadyBoundException conformance: hierarchy and constructors.
 * @run main AlreadyBoundExceptionTest
 */

import java.rmi.AlreadyBoundException;

public class AlreadyBoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.lang.Exception.class.isAssignableFrom(AlreadyBoundException.class), "extends Exception");
        AlreadyBoundException e = new AlreadyBoundException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        AlreadyBoundException e2 = new AlreadyBoundException();
        check(e2 != null, "default ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
