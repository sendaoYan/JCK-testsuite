/*
 * @test
 * @summary java.io.LineNumberInputStream API conformance (deprecated): getLineNumber.
 * @run main LineNumberInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.LineNumberInputStream;

public class LineNumberInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLineNumberInputStream() throws Exception {
        LineNumberInputStream in =
            new LineNumberInputStream(new ByteArrayInputStream("a\nb\n".getBytes("US-ASCII")));
        check(in.getLineNumber() == 0, "initial line number 0");
        check(in.read() == 'a', "read 'a'");
        check(in.read() == '\n', "read newline");
        check(in.getLineNumber() == 1, "line number incremented after newline");
        check(in.read() == 'b', "read 'b'");
        check(in.read() == '\n', "read second newline");
        check(in.getLineNumber() == 2, "line number incremented again");
    }

    public static void main(String[] args) throws Exception {
        testLineNumberInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
