/*
 * @test
 * @summary java.nio.LongBuffer API conformance: put/flip/get.
 * @run main LongBufferTest
 */

import java.nio.LongBuffer;

public class LongBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLongBuffer() {
        LongBuffer b = LongBuffer.allocate(2);
        b.put(10L);
        b.put(20L);
        b.flip();
        check(b.get() == 10L && b.get() == 20L, "get longs");
        check(!b.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testLongBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
