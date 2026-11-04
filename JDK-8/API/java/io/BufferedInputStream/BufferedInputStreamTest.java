/*
 * @test
 * @summary java.io.BufferedInputStream API conformance: read, skip, and mark/reset.
 * @run main BufferedInputStreamTest
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class BufferedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferedInputStream() throws Exception {
        BufferedInputStream bis =
            new BufferedInputStream(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
        check(bis.read() == 1, "read first");
        check(bis.skip(2) == 2, "skip two");
        check(bis.read() == 4, "read after skip");
        bis.mark(10);
        int a = bis.read();
        check(a == 5, "read after mark");
        bis.reset();
        check(bis.read() == a, "reset replays");
        check(bis.read() == -1, "EOF");
    }

    public static void main(String[] args) throws Exception {
        testBufferedInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
