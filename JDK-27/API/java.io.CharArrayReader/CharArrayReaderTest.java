/*
 * @test
 * @summary java.io.CharArrayReader API conformance: read() to EOF.
 * @run main CharArrayReaderTest
 */

import java.io.CharArrayReader;

public class CharArrayReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharArrayReader() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c'});
        check(r.read() == 'a' && r.read() == 'b' && r.read() == 'c' && r.read() == -1,
              "read() to EOF");
    }

    public static void main(String[] args) throws Exception {
        testCharArrayReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
