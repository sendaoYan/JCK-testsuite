/*
 * @test
 * @summary java.io.StringBufferInputStream API conformance (deprecated): read/available.
 * @run main StringBufferInputStreamTest
 */

import java.io.StringBufferInputStream;

public class StringBufferInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStringBufferInputStream() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("abc");
        check(in.read() == 'a', "read first");
        check(in.read() == 'b', "read second");
        check(in.available() == 1, "available after two reads");
        check(in.read() == 'c', "read third");
        check(in.read() == -1, "read EOF");
    }

    public static void main(String[] args) throws Exception {
        testStringBufferInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
