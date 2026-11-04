/*
 * @test
 * @summary java.security.GeneralSecurityException conformance: constructors, message, cause and superclass.
 * @run main GeneralSecurityExceptionTest
 */

public class GeneralSecurityExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.GeneralSecurityException e = new java.security.GeneralSecurityException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        Throwable cau = new Exception("cause");
        java.security.GeneralSecurityException e2 = new java.security.GeneralSecurityException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.GeneralSecurityException e3 = new java.security.GeneralSecurityException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.GeneralSecurityException e4 = new java.security.GeneralSecurityException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
