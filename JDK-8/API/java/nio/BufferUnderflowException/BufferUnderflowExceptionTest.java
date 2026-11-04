/*
 * @test
 * @summary java.nio.BufferUnderflowException API conformance: thrown when reading past the
 *          limit.
 * @run main BufferUnderflowExceptionTest
 */

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class BufferUnderflowExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferUnderflowException() {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        bb.flip();
        check(bb.get() == 1, "read the single byte");
        boolean thrown = false;
        try {
            bb.get();
        } catch (BufferUnderflowException e) {
            thrown = true;
            check(e instanceof RuntimeException, "is RuntimeException");
        }
        check(thrown, "get past limit throws BufferUnderflowException");
    }

    public static void main(String[] args) {
        testBufferUnderflowException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
