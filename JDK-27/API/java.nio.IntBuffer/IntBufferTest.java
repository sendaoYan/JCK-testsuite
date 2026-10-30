/*
 * @test
 * @summary java.nio.IntBuffer API conformance: put/flip/get and remaining.
 * @run main IntBufferTest
 */

import java.nio.IntBuffer;

public class IntBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIntBuffer() {
        IntBuffer ib = IntBuffer.allocate(3);
        ib.put(10);
        ib.put(20);
        ib.put(30);
        ib.flip();
        check(ib.get() == 10 && ib.get() == 20 && ib.get() == 30, "get ints");
        check(!ib.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testIntBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
