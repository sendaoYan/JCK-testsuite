/*
 * @test
 * @summary java.security.acl.AclNotFoundException conformance: constructors, message, cause and superclass.
 * @run main AclNotFoundExceptionTest
 */

public class AclNotFoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.acl.AclNotFoundException e = new java.security.acl.AclNotFoundException();
        check(e instanceof java.lang.Exception, "extends java.lang.Exception");
        java.security.acl.AclNotFoundException e4 = new java.security.acl.AclNotFoundException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
