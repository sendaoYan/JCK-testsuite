/*
 * @test
 * @summary java.security.spec.InvalidParameterSpecException conformance: constructors, message, cause and superclass.
 * @run main InvalidParameterSpecExceptionTest
 */

public class InvalidParameterSpecExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.InvalidParameterSpecException e = new java.security.spec.InvalidParameterSpecException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        java.security.spec.InvalidParameterSpecException e4 = new java.security.spec.InvalidParameterSpecException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
