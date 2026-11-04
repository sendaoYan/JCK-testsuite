/*
 * @test
 * @summary java.io.LineNumberReader API conformance: line numbering and readLine.
 * @run main LineNumberReaderTest
 */

import java.io.LineNumberReader;
import java.io.StringReader;

public class LineNumberReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLineNumberReader() throws Exception {
        LineNumberReader lnr = new LineNumberReader(new StringReader("one\ntwo\nthree"));
        check(lnr.getLineNumber() == 0, "initial line number");
        check("one".equals(lnr.readLine()), "readLine one");
        check(lnr.getLineNumber() == 1, "line number after one");
        check("two".equals(lnr.readLine()), "readLine two");
        check(lnr.getLineNumber() == 2, "line number after two");
    }

    public static void main(String[] args) throws Exception {
        testLineNumberReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
