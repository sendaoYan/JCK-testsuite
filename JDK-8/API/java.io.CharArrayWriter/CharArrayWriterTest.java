/*
 * @test
 * @summary java.io.CharArrayWriter API conformance: write, toString, size, and toCharArray.
 * @run main CharArrayWriterTest
 */

import java.io.CharArrayWriter;

public class CharArrayWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharArrayWriter() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("hello");
        check("hello".equals(w.toString()), "toString");
        check(w.size() == 5, "size");
        char[] chars = w.toCharArray();
        check(chars.length == 5 && chars[0] == 'h', "toCharArray");
    }

    public static void main(String[] args) throws Exception {
        testCharArrayWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
