/*
 * @test
 * @summary java.nio.channels.AcceptPendingException conformance: hierarchy and constructor.
 * @run main AcceptPendingExceptionTest
 */

import java.nio.channels.AcceptPendingException;

public class AcceptPendingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAcceptPendingException() {
        check(IllegalStateException.class.isAssignableFrom(AcceptPendingException.class), "extends IllegalStateException");
        AcceptPendingException e = new AcceptPendingException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testAcceptPendingException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
