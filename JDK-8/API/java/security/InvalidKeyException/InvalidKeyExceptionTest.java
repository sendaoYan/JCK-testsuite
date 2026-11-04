/*
 * @test
 * @summary java.security.InvalidKeyException conformance: constructors, message, cause and superclass.
 * @run main InvalidKeyExceptionTest
 */

public class InvalidKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.InvalidKeyException e = new java.security.InvalidKeyException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.KeyException, "extends java.security.KeyException");
        Throwable cau = new Exception("cause");
        java.security.InvalidKeyException e2 = new java.security.InvalidKeyException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.InvalidKeyException e3 = new java.security.InvalidKeyException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.InvalidKeyException e4 = new java.security.InvalidKeyException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
