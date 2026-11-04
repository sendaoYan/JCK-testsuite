/*
 * @test
 * @summary java.io.StringWriter API conformance: write, toString, and append.
 * @run main StringWriterTest
 */

import java.io.StringWriter;

public class StringWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWriter() {
        StringWriter w = new StringWriter();
        w.write("abc");
        w.write('d');
        check("abcd".equals(w.toString()), "write/toString");
        StringWriter w2 = new StringWriter();
        w2.append("xy").append('z');
        check("xyz".equals(w2.toString()), "append");
    }

    public static void main(String[] args) {
        testWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
