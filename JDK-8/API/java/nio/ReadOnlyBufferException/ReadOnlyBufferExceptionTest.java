/*
 * @test
 * @summary java.nio.ReadOnlyBufferException API conformance: put on a read-only buffer.
 * @run main ReadOnlyBufferExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

public class ReadOnlyBufferExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReadOnlyBufferException() {
        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        try {
            ro.put((byte) 1);
            check(false, "put on a read-only buffer should throw");
        } catch (ReadOnlyBufferException e) {
            check(true, "put on a read-only buffer throws ReadOnlyBufferException");
        }
    }

    public static void main(String[] args) {
        testReadOnlyBufferException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
