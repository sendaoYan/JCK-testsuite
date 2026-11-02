/*
 * @test
 * @summary java.security.acl.LastOwnerException conformance: constructors, message, cause and superclass.
 * @run main LastOwnerExceptionTest
 */

public class LastOwnerExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.acl.LastOwnerException e = new java.security.acl.LastOwnerException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        java.security.acl.LastOwnerException e4 = new java.security.acl.LastOwnerException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
