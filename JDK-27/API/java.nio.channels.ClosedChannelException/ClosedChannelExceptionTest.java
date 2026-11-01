/*
 * @test
 * @summary java.nio.channels.ClosedChannelException conformance: hierarchy and constructor.
 * @run main ClosedChannelExceptionTest
 */

import java.nio.channels.ClosedChannelException;
import java.io.IOException;

public class ClosedChannelExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClosedChannelException() {
        check(IOException.class.isAssignableFrom(ClosedChannelException.class), "extends IOException");
        ClosedChannelException e = new ClosedChannelException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IOException, "instanceof IOException");
    }

    public static void main(String[] args) {
        testClosedChannelException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
