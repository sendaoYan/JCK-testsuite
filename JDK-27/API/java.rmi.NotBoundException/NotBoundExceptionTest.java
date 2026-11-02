/*
 * @test
 * @summary java.rmi.NotBoundException conformance: hierarchy and constructors.
 * @run main NotBoundExceptionTest
 */

import java.rmi.NotBoundException;

public class NotBoundExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.lang.Exception.class.isAssignableFrom(NotBoundException.class), "extends Exception");
        NotBoundException e = new NotBoundException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        NotBoundException e2 = new NotBoundException();
        check(e2 != null, "default ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
