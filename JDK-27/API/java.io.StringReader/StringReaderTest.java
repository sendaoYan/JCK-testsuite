/*
 * @test
 * @summary java.io.StringReader API conformance: read(), read(char[]), and EOF returning -1.
 * @run main StringReaderTest
 */

import java.io.StringReader;

public class StringReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testReader() throws Exception {
        StringReader r = new StringReader("hello");
        check(r.read() == 'h' && r.read() == 'e', "read()");
        char[] buf = new char[3];
        check(r.read(buf) == 3 && buf[0] == 'l' && buf[1] == 'l' && buf[2] == 'o',
              "read(char[])");
        check(r.read() == -1, "EOF");
    }

    public static void main(String[] args) throws Exception {
        testReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
