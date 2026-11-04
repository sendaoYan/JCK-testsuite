/*
 * @test
 * @summary java.security.KeyException conformance: constructors, message, cause and superclass.
 * @run main KeyExceptionTest
 */

public class KeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyException e = new java.security.KeyException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.KeyException e2 = new java.security.KeyException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.KeyException e3 = new java.security.KeyException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.KeyException e4 = new java.security.KeyException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
