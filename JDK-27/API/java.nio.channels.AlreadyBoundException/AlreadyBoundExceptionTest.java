/*
 * @test
 * @summary java.nio.channels.AlreadyBoundException conformance: hierarchy and constructor.
 * @run main AlreadyBoundExceptionTest
 */

import java.nio.channels.AlreadyBoundException;

public class AlreadyBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAlreadyBoundException() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyBoundException.class), "extends IllegalStateException");
        AlreadyBoundException e = new AlreadyBoundException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testAlreadyBoundException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
