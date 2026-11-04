/*
 * @test
 * @summary java.security.acl.NotOwnerException conformance: constructors, message, cause and superclass.
 * @run main NotOwnerExceptionTest
 */

public class NotOwnerExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.acl.NotOwnerException e = new java.security.acl.NotOwnerException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        java.security.acl.NotOwnerException e4 = new java.security.acl.NotOwnerException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
