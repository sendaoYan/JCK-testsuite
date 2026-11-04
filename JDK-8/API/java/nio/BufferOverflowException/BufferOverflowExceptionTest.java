/*
 * @test
 * @summary java.nio.BufferOverflowException API conformance: thrown when writing past
 *          capacity.
 * @run main BufferOverflowExceptionTest
 */

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class BufferOverflowExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferOverflowException() {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        boolean thrown = false;
        try {
            bb.put((byte) 2);
        } catch (BufferOverflowException e) {
            thrown = true;
            check(e instanceof RuntimeException, "is RuntimeException");
        }
        check(thrown, "put past capacity throws BufferOverflowException");
    }

    public static void main(String[] args) {
        testBufferOverflowException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
