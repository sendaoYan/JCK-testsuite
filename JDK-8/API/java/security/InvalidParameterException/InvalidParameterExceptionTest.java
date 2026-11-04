/*
 * @test
 * @summary java.security.InvalidParameterException conformance: constructors, message, cause and superclass.
 * @run main InvalidParameterExceptionTest
 */

public class InvalidParameterExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.InvalidParameterException e = new java.security.InvalidParameterException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.IllegalArgumentException, "extends java.lang.IllegalArgumentException");
        java.security.InvalidParameterException e4 = new java.security.InvalidParameterException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
