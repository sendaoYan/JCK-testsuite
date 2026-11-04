/*
 * @test
 * @summary java.security.NoSuchProviderException conformance: constructors, message, cause and superclass.
 * @run main NoSuchProviderExceptionTest
 */

public class NoSuchProviderExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.NoSuchProviderException e = new java.security.NoSuchProviderException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        java.security.NoSuchProviderException e4 = new java.security.NoSuchProviderException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
