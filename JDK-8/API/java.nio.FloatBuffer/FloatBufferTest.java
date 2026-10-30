/*
 * @test
 * @summary java.nio.FloatBuffer API conformance: put/flip/get.
 * @run main FloatBufferTest
 */

import java.nio.FloatBuffer;

public class FloatBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFloatBuffer() {
        FloatBuffer b = FloatBuffer.allocate(2);
        b.put(1.5f);
        b.put(2.5f);
        b.flip();
        check(b.get() == 1.5f && b.get() == 2.5f, "get floats");
        check(!b.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testFloatBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
