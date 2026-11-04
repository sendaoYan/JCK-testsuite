/*
 * @test
 * @summary java.nio.ShortBuffer API conformance: put/flip/get.
 * @run main ShortBufferTest
 */

import java.nio.ShortBuffer;

public class ShortBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testShortBuffer() {
        ShortBuffer b = ShortBuffer.allocate(2);
        b.put((short) 1);
        b.put((short) 2);
        b.flip();
        check(b.get() == 1 && b.get() == 2, "get shorts");
        check(!b.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testShortBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
