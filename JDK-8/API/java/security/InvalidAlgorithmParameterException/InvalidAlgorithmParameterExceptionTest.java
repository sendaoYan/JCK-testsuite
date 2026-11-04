/*
 * @test
 * @summary java.security.InvalidAlgorithmParameterException conformance: constructors, message, cause and superclass.
 * @run main InvalidAlgorithmParameterExceptionTest
 */

public class InvalidAlgorithmParameterExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.InvalidAlgorithmParameterException e = new java.security.InvalidAlgorithmParameterException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.InvalidAlgorithmParameterException e2 = new java.security.InvalidAlgorithmParameterException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.InvalidAlgorithmParameterException e3 = new java.security.InvalidAlgorithmParameterException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.InvalidAlgorithmParameterException e4 = new java.security.InvalidAlgorithmParameterException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
