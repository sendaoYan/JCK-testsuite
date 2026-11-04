/*
 * @test
 * @summary java.io.PrintWriter API conformance: print, println, and checkError.
 * @run main PrintWriterTest
 */

import java.io.PrintWriter;
import java.io.StringWriter;

public class PrintWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.print("hello");
        pw.println(42);
        pw.flush();
        String out = sw.toString();
        check(out.startsWith("hello"), "print");
        check(out.contains("42"), "println int");
        check(!pw.checkError(), "checkError false");
    }

    public static void main(String[] args) {
        testPrintWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
