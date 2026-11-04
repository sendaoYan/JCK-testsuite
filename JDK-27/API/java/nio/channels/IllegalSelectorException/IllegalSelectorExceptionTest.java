/*
 * @test
 * @summary java.nio.channels.IllegalSelectorException conformance: white-box structure + black-box behavior.
 * @run main IllegalSelectorExceptionTest
 */

import java.nio.channels.IllegalSelectorException;

public class IllegalSelectorExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalSelectorException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new IllegalSelectorException() != null, "no-arg constructor");
        check(new IllegalSelectorException().getMessage() == null, "no-arg message is null");
        check(new IllegalSelectorException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalSelectorException e = new IllegalSelectorException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        try {
            throw e;
        } catch (IllegalSelectorException again) {
            check(again == e, "rethrow preserves identity");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
