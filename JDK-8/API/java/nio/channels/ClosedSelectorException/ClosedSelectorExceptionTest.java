/*
 * @test
 * @summary java.nio.channels.ClosedSelectorException conformance: hierarchy and constructor.
 * @run main ClosedSelectorExceptionTest
 */

import java.nio.channels.ClosedSelectorException;

public class ClosedSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedSelectorException() {
        check(IllegalStateException.class.isAssignableFrom(ClosedSelectorException.class), "extends IllegalStateException");
        ClosedSelectorException e = new ClosedSelectorException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testClosedSelectorException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
