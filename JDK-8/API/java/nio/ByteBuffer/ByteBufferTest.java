/*
 * @test
 * @summary java.nio.ByteBuffer API conformance: allocate/put/flip/get state machine.
 * @run main ByteBufferTest
 */

import java.nio.ByteBuffer;

public class ByteBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testByteBuffer() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        check(bb.capacity() == 8, "capacity");
        check(bb.position() == 0, "initial position");
        check(bb.limit() == 8, "initial limit");
        bb.put((byte) 1);
        bb.put((byte) 2);
        check(bb.position() == 2, "position after put");
        bb.flip();
        check(bb.position() == 0 && bb.limit() == 2, "flip");
        check(bb.remaining() == 2, "remaining");
        check(bb.get() == 1 && bb.get() == 2, "get in order");
        check(!bb.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testByteBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
