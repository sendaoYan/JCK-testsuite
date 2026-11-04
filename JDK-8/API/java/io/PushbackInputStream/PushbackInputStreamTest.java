/*
 * @test
 * @summary java.io.PushbackInputStream API conformance: read and unread.
 * @run main PushbackInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.PushbackInputStream;

public class PushbackInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPushbackInputStream() throws Exception {
        PushbackInputStream p =
            new PushbackInputStream(new ByteArrayInputStream(new byte[]{10, 20}));
        check(p.read() == 10, "read 10");
        p.unread(10);
        check(p.read() == 10, "read after unread");
        check(p.read() == 20, "read 20");
        check(p.read() == -1, "EOF");
    }

    public static void main(String[] args) throws Exception {
        testPushbackInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
