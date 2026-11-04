/*
 * @test
 * @summary java.security.DigestException conformance: constructors, message, cause and superclass.
 * @run main DigestExceptionTest
 */

public class DigestExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.DigestException e = new java.security.DigestException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.DigestException e2 = new java.security.DigestException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.DigestException e3 = new java.security.DigestException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.DigestException e4 = new java.security.DigestException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
