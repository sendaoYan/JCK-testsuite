/*
 * @test
 * @summary java.lang.Readable API conformance: read(CharBuffer) copies characters and
 *          returns the count, returns -1 at end of input, and throws NPE for a null buffer.
 * @run main ReadableTest
 */

import java.io.StringReader;
import java.nio.CharBuffer;

public class ReadableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRead() throws Exception {
        Readable r = new StringReader("hello");
        CharBuffer buf = CharBuffer.allocate(3);
        int n = r.read(buf);
        check(n == 3, "read returns chars copied");
        check(buf.flip().toString().equals("hel"), "buffer contents");
        CharBuffer rest = CharBuffer.allocate(10);
        check(r.read(rest) == 2, "read remaining 2");
        check(rest.flip().toString().equals("lo"), "remaining contents");
        check(r.read(CharBuffer.allocate(10)) == -1, "read at EOF returns -1");
    }

    static void testNull() throws Exception {
        Readable r = new StringReader("x");
        try {
            r.read(null);
            throw new AssertionError("read(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testRead();
        testNull();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
