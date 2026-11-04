/*
 * @test
 * @summary java.nio.Buffer API conformance: position/limit/remaining/capacity via a
 *          ByteBuffer.
 * @run main BufferTest
 */

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class BufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuffer() {
        Buffer buf = ByteBuffer.allocate(4);
        check(buf.capacity() == 4, "capacity");
        check(buf.position() == 0, "position");
        check(buf.limit() == 4, "limit");
        check(buf.remaining() == 4, "remaining");
        buf.limit(2);
        check(buf.limit() == 2 && buf.remaining() == 2, "limit adjusts remaining");
        check(buf.hasRemaining(), "hasRemaining");
    }

    public static void main(String[] args) {
        testBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
