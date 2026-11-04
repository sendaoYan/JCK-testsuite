/*
 * @test
 * @summary java.io.PrintStream API conformance: print, println, and checkError.
 * @run main PrintStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PrintStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPrintStream() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        ps.print("hello");
        ps.println(42);
        ps.flush();
        String out = bos.toString();
        check(out.startsWith("hello"), "print");
        check(out.contains("42"), "println int");
        check(!ps.checkError(), "checkError false");
    }

    public static void main(String[] args) {
        testPrintStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
