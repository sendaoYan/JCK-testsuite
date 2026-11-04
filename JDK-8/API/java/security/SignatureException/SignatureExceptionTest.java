/*
 * @test
 * @summary java.security.SignatureException conformance: constructors, message, cause and superclass.
 * @run main SignatureExceptionTest
 */

public class SignatureExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.SignatureException e = new java.security.SignatureException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.SignatureException e2 = new java.security.SignatureException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.SignatureException e3 = new java.security.SignatureException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.SignatureException e4 = new java.security.SignatureException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
