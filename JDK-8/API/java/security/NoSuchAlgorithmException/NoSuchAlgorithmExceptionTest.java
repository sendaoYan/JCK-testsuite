/*
 * @test
 * @summary java.security.NoSuchAlgorithmException conformance: constructors, message, cause and superclass.
 * @run main NoSuchAlgorithmExceptionTest
 */

public class NoSuchAlgorithmExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.NoSuchAlgorithmException e = new java.security.NoSuchAlgorithmException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.NoSuchAlgorithmException e2 = new java.security.NoSuchAlgorithmException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.NoSuchAlgorithmException e3 = new java.security.NoSuchAlgorithmException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.NoSuchAlgorithmException e4 = new java.security.NoSuchAlgorithmException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
