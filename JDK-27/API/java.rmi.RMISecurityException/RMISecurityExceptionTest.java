/*
 * @test
 * @summary java.rmi.RMISecurityException conformance: hierarchy and constructors.
 * @run main RMISecurityExceptionTest
 */

import java.rmi.RMISecurityException;

public class RMISecurityExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(RMISecurityException.class.getSuperclass() == java.lang.SecurityException.class, "extends SecurityException");
        RMISecurityException e = new RMISecurityException("detail-msg");
        check("detail-msg".equals(e.getMessage()), "message");
        RMISecurityException e2 = new RMISecurityException("detail-msg", "arg");
        check("detail-msg".equals(e2.getMessage()), "message with arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
