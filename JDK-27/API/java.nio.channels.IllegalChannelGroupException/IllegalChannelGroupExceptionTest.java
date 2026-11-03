/*
 * @test
 * @summary java.nio.channels.IllegalChannelGroupException conformance: white-box structure + black-box behavior.
 * @run main IllegalChannelGroupExceptionTest
 */

import java.nio.channels.IllegalChannelGroupException;

public class IllegalChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(java.lang.IllegalArgumentException.class.isAssignableFrom(IllegalChannelGroupException.class), "hierarchy: extends java.lang.IllegalArgumentException");
        check(new IllegalChannelGroupException() != null, "no-arg constructor");
        check(new IllegalChannelGroupException().getMessage() == null, "no-arg message is null");
        check(new IllegalChannelGroupException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IllegalChannelGroupException e = new IllegalChannelGroupException();
        check(e.getMessage() == null, "no message");
        check(e instanceof IllegalArgumentException, "IllegalArgumentException family");
        check(e.toString().contains("IllegalChannelGroupException"), "toString names class");
        // no-arg ctor usable in catch-and-rethrow chains
        try {
            throw e;
        } catch (IllegalChannelGroupException again) {
            check(again == e, "rethrow preserves identity");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
