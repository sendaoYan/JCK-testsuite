/*
 * @test
 * @summary java.nio.CharBuffer API conformance: put/flip/get and remaining.
 * @run main CharBufferTest
 */

import java.nio.CharBuffer;

public class CharBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharBuffer() {
        CharBuffer cb = CharBuffer.allocate(4);
        cb.put('a');
        cb.put('b');
        cb.flip();
        check(cb.get() == 'a' && cb.get() == 'b', "get chars");
        check(!cb.hasRemaining(), "no remaining");
    }

    public static void main(String[] args) {
        testCharBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
