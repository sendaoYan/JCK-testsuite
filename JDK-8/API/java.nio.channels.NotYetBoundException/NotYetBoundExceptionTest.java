/*
 * @test
 * @summary java.nio.channels.NotYetBoundException conformance: hierarchy and constructor.
 * @run main NotYetBoundExceptionTest
 */

import java.nio.channels.NotYetBoundException;

public class NotYetBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNotYetBoundException() {
        check(IllegalStateException.class.isAssignableFrom(NotYetBoundException.class), "extends IllegalStateException");
        NotYetBoundException e = new NotYetBoundException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testNotYetBoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
