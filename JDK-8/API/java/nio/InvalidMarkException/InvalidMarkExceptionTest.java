/*
 * @test
 * @summary java.nio.InvalidMarkException API conformance: reset without a mark.
 * @run main InvalidMarkExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class InvalidMarkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvalidMarkException() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        try {
            buf.reset();
            check(false, "reset without a mark should throw");
        } catch (InvalidMarkException e) {
            check(true, "reset without a mark throws InvalidMarkException");
        }
    }

    public static void main(String[] args) {
        testInvalidMarkException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
