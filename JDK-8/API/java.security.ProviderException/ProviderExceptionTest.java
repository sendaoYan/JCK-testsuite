/*
 * @test
 * @summary java.security.ProviderException conformance: constructors, message, cause and superclass.
 * @run main ProviderExceptionTest
 */

public class ProviderExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.ProviderException e = new java.security.ProviderException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.RuntimeException, "extends java.lang.RuntimeException");
        Throwable cau = new Exception("cause");
        java.security.ProviderException e2 = new java.security.ProviderException("m", cau);
        check(e2.getCause() == cau, "(String,Throwable) ctor cause");
        Throwable cau2 = new Exception("cause2");
        java.security.ProviderException e3 = new java.security.ProviderException(cau2);
        check(e3.getCause() == cau2, "(Throwable) ctor cause");
        java.security.ProviderException e4 = new java.security.ProviderException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
