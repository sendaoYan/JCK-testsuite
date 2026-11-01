/*
 * @test
 * @summary java.nio.channels.IllegalChannelGroupException conformance: hierarchy and constructor.
 * @run main IllegalChannelGroupExceptionTest
 */

import java.nio.channels.IllegalChannelGroupException;

public class IllegalChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllegalChannelGroupException() {
        check(IllegalArgumentException.class.isAssignableFrom(IllegalChannelGroupException.class), "extends IllegalArgumentException");
        IllegalChannelGroupException e = new IllegalChannelGroupException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalArgumentException, "instanceof IllegalArgumentException");
    }

    public static void main(String[] args) {
        testIllegalChannelGroupException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
