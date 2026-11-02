/*
 * @test
 * @summary java.security.KeyManagementException conformance: constructors, message, cause and superclass.
 * @run main KeyManagementExceptionTest
 */

public class KeyManagementExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.KeyManagementException e = new java.security.KeyManagementException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.KeyException, "extends java.security.KeyException");
        Throwable cau = new Exception("cause");
        java.security.KeyManagementException e2 = new java.security.KeyManagementException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.KeyManagementException e3 = new java.security.KeyManagementException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.KeyManagementException e4 = new java.security.KeyManagementException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
