/*
 * @test
 * @summary java.io.Reader API conformance: default read/ready/skip semantics.
 * @run main ReaderTest
 */

import java.io.IOException;
import java.io.Reader;

public class ReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class FixedReader extends Reader {
        private int remaining = 2;

        public int read(char[] cbuf, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int n = Math.min(len, remaining);
            for (int i = 0; i < n; i++) cbuf[off + i] = 'R';
            remaining -= n;
            return n;
        }

        public void close() {}
    }

    static void testReader() throws Exception {
        FixedReader r = new FixedReader();
        check(r.read() == 'R', "read() returns a char");
        char[] c = new char[10];
        int n = r.read(c);
        check(n == 1, "read(char[]) reads the remaining char");
        check(c[0] == 'R', "buffer filled with the char");
        check(r.read(c) == -1, "read(char[]) returns -1 at EOF");
        check(!r.ready(), "default ready() is false");
    }

    public static void main(String[] args) throws Exception {
        testReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
