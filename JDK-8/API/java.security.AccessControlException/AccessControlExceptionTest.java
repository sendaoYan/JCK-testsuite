/*
 * @test
 * @summary java.security.AccessControlException conformance: constructors, message, cause and superclass.
 * @run main AccessControlExceptionTest
 */

public class AccessControlExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.AccessControlException e = new java.security.AccessControlException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.lang.SecurityException, "extends java.lang.SecurityException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
