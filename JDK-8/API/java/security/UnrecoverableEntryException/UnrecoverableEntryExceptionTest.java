/*
 * @test
 * @summary java.security.UnrecoverableEntryException conformance: constructors, message, cause and superclass.
 * @run main UnrecoverableEntryExceptionTest
 */

public class UnrecoverableEntryExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.UnrecoverableEntryException e = new java.security.UnrecoverableEntryException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        java.security.UnrecoverableEntryException e4 = new java.security.UnrecoverableEntryException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
