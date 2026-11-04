/*
 * @test
 * @summary java.io.BufferedWriter API conformance: write, newLine, and flush.
 * @run main BufferedWriterTest
 */

import java.io.BufferedWriter;
import java.io.StringWriter;

public class BufferedWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferedWriter() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter bw = new BufferedWriter(sw);
        bw.write("hello");
        bw.newLine();
        bw.write("world");
        bw.flush();
        String expected = "hello" + System.lineSeparator() + "world";
        check(expected.equals(sw.toString()), "write/newLine/flush");
    }

    public static void main(String[] args) throws Exception {
        testBufferedWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
