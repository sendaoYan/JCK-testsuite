/*
 * @test
 * @summary java.nio.channels.ShutdownChannelGroupException conformance: hierarchy and constructor.
 * @run main ShutdownChannelGroupExceptionTest
 */

import java.nio.channels.ShutdownChannelGroupException;

public class ShutdownChannelGroupExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testShutdownChannelGroupException() {
        check(IllegalStateException.class.isAssignableFrom(ShutdownChannelGroupException.class), "extends IllegalStateException");
        ShutdownChannelGroupException e = new ShutdownChannelGroupException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IllegalStateException, "instanceof IllegalStateException");
    }

    public static void main(String[] args) {
        testShutdownChannelGroupException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
