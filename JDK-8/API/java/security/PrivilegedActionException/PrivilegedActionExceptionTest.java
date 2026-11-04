/*
 * @test
 * @summary java.security.PrivilegedActionException conformance: constructor, getException, getCause.
 * @run main PrivilegedActionExceptionTest
 */

public class PrivilegedActionExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Exception cause = new Exception("cause");
        java.security.PrivilegedActionException e = new java.security.PrivilegedActionException(cause);
        check(cause.equals(e.getException()), "getException");
        check(cause.equals(e.getCause()), "getCause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
