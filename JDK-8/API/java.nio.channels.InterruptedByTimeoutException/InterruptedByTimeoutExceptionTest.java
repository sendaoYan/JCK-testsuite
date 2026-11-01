/*
 * @test
 * @summary java.nio.channels.InterruptedByTimeoutException conformance: hierarchy and constructor.
 * @run main InterruptedByTimeoutExceptionTest
 */

import java.nio.channels.InterruptedByTimeoutException;
import java.io.IOException;

public class InterruptedByTimeoutExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInterruptedByTimeoutException() {
        check(IOException.class.isAssignableFrom(InterruptedByTimeoutException.class), "extends IOException");
        InterruptedByTimeoutException e = new InterruptedByTimeoutException();
        check(e.getMessage() == null, "no-arg message null");
        check(e instanceof IOException, "instanceof IOException");
    }

    public static void main(String[] args) {
        testInterruptedByTimeoutException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
