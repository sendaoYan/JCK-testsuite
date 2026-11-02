/*
 * @test
 * @summary java.rmi.NoSuchObjectException conformance: hierarchy and constructors.
 * @run main NoSuchObjectExceptionTest
 */

import java.rmi.NoSuchObjectException;

public class NoSuchObjectExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(java.rmi.RemoteException.class.isAssignableFrom(NoSuchObjectException.class), "extends java.rmi.RemoteException");
        NoSuchObjectException e = new NoSuchObjectException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
