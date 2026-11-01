/*
 * @test
 * @summary java.nio.channels.IllegalSelectorException conformance: hierarchy and constructor.
 * @run main IllegalSelectorExceptionTest
 */

import java.nio.channels.IllegalSelectorException;

public class IllegalSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalSelectorException() {
        check(IllegalArgumentException.class.isAssignableFrom(IllegalSelectorException.class), "extends IllegalArgumentException");
        IllegalSelectorException e = new IllegalSelectorException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
    }

    public static void main(String[] args) {
        testIllegalSelectorException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
