/*
 * @test
 * @summary java.security.spec.InvalidKeySpecException conformance: constructors, message, cause and superclass.
 * @run main InvalidKeySpecExceptionTest
 */

public class InvalidKeySpecExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.InvalidKeySpecException e = new java.security.spec.InvalidKeySpecException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.spec.InvalidKeySpecException e2 = new java.security.spec.InvalidKeySpecException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.spec.InvalidKeySpecException e3 = new java.security.spec.InvalidKeySpecException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.spec.InvalidKeySpecException e4 = new java.security.spec.InvalidKeySpecException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
