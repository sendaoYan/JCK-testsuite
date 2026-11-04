/*
 * @test
 * @summary java.security.UnrecoverableKeyException conformance: constructors, message, cause and superclass.
 * @run main UnrecoverableKeyExceptionTest
 */

public class UnrecoverableKeyExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.UnrecoverableKeyException e = new java.security.UnrecoverableKeyException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.UnrecoverableEntryException, "extends java.security.UnrecoverableEntryException");
        java.security.UnrecoverableKeyException e4 = new java.security.UnrecoverableKeyException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
