/*
 * @test
 * @summary java.io.StringBufferInputStream conformance: full contract coverage.
 *          Deprecated byte view of a String (low 8 bits per char): reads, skip,
 *          available, reset to start, no-op close, non-ASCII truncation to low byte.
 * @run main StringBufferInputStreamTest
 */

import java.io.StringBufferInputStream;

public class StringBufferInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        StringBufferInputStream in = new StringBufferInputStream("abc");
        check(in.read() == 'a' && in.read() == 'b' && in.read() == 'c', "sequential reads");
        check(in.read() == -1, "EOF");

        in = new StringBufferInputStream("abcdef");
        byte[] buf = new byte[3];
        check(in.read(buf, 0, 3) == 3 && buf[0] == 'a', "region read");
        check(in.read(buf) == 3 && new String(buf, 0, 3).equals("def"), "remaining read");
        check(in.read(buf) == -1, "EOF bulk");

        in = new StringBufferInputStream("é");  // "é"
        int v = in.read();
        check(v == 0xE9, "low 8 bits of 'é' delivered");

        in = new StringBufferInputStream("12345");
        check(in.available() == 5, "available is the full length");
        check(in.skip(2) == 2 && in.available() == 3, "skip(2)");
        check(in.read() == '3', "position after skip");
        check(in.skip(99) == 2, "skip clamps to remaining");

        in = new StringBufferInputStream("xy");
        in.skip(2);
        in.reset();
        check(in.read() == 'x', "reset rewinds to position 0");

        StringBufferInputStream lv = new StringBufferInputStream("12");
        check(lv.read() == '1' && lv.read() == '2' && lv.read() == -1, "exact length read");

        in = new StringBufferInputStream("q");
        in.close();
        check(in.read() == 'q', "close is a no-op here");

        StringBufferInputStream e = new StringBufferInputStream("");
        check(e.read() == -1 && e.available() == 0, "empty source");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
