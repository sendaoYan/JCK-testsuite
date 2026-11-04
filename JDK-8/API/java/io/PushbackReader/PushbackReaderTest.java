/*
 * @test
 * @summary java.io.PushbackReader API conformance: read and unread.
 * @run main PushbackReaderTest
 */

import java.io.PushbackReader;
import java.io.StringReader;

public class PushbackReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPushbackReader() throws Exception {
        PushbackReader pr = new PushbackReader(new StringReader("ab"));
        check(pr.read() == 'a', "read a");
        pr.unread('a');
        check(pr.read() == 'a', "read after unread");
        check(pr.read() == 'b', "read b");
        check(pr.read() == -1, "EOF");
    }

    public static void main(String[] args) throws Exception {
        testPushbackReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
