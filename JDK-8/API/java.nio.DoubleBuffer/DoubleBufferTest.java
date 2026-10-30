/*
 * @test
 * @summary java.nio.DoubleBuffer API conformance: put/flip/get.
 * @run main DoubleBufferTest
 */

import java.nio.DoubleBuffer;

public class DoubleBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDoubleBuffer() {
        DoubleBuffer b = DoubleBuffer.allocate(2);
        b.put(1.5);
        b.put(2.5);
        b.flip();
        check(b.get() == 1.5 && b.get() == 2.5, "get doubles");
        check(!b.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testDoubleBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
