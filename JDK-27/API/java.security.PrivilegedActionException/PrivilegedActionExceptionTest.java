/*
 * @test
 * @summary java.security.PrivilegedActionException conformance: constructor and getException.
 * @run main PrivilegedActionExceptionTest
 */

import java.security.PrivilegedActionException;

public class PrivilegedActionExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Exception cause = new Exception("root");
        PrivilegedActionException e = new PrivilegedActionException(cause);
        check(cause == e.getException(), "getException");
        check(e.getCause() == cause, "getCause");
        check(e instanceof Exception, "extends Exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
