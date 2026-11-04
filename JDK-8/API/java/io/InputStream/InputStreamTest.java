/*
 * @test
 * @summary java.io.InputStream API conformance: default read/skip/available/mark semantics.
 * @run main InputStreamTest
 */

import java.io.IOException;
import java.io.InputStream;

public class InputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Fixed extends InputStream {
        private int remaining = 3;

        public int read() throws IOException {
            if (remaining <= 0) return -1;
            remaining--;
            return 'A';
        }
    }

    static void testInputStream() throws Exception {
        Fixed in = new Fixed();
        check(in.read() == 'A', "read() returns a byte");
        byte[] buf = new byte[4];
        int n = in.read(buf);
        check(n == 2, "read(byte[]) reads the remaining bytes");
        check(buf[0] == 'A' && buf[1] == 'A', "buffer filled with the bytes");
        check(in.read(buf) == -1, "read(byte[]) returns -1 at EOF");
        check(in.available() == 0, "default available() returns 0");
        check(!in.markSupported(), "default markSupported() is false");
    }

    public static void main(String[] args) throws Exception {
        testInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
