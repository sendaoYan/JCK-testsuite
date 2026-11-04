/*
 * @test
 * @summary java.security.KeyStoreException conformance: constructors, message, cause and superclass.
 * @run main KeyStoreExceptionTest
 */

public class KeyStoreExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyStoreException e = new java.security.KeyStoreException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        Throwable cau = new Exception("cause");
        java.security.KeyStoreException e2 = new java.security.KeyStoreException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.KeyStoreException e3 = new java.security.KeyStoreException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.KeyStoreException e4 = new java.security.KeyStoreException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
