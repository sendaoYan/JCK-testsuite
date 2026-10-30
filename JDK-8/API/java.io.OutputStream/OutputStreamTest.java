/*
 * @test
 * @summary java.io.OutputStream API conformance: default write/flush/close semantics.
 * @run main OutputStreamTest
 */

import java.io.OutputStream;

public class OutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sink extends OutputStream {
        int last;
        int count;

        public void write(int b) {
            last = b;
            count++;
        }
    }

    static void testOutputStream() throws Exception {
        Sink s = new Sink();
        s.write('A');
        check(s.last == 'A', "write(int) delegates to the subclass");
        s.write(new byte[] {'B', 'C', 'D'});
        check(s.count == 4, "write(byte[]) invokes write(int) per byte");
        s.write(new byte[] {'X', 'Y', 'Z'}, 1, 2);
        check(s.count == 6, "write(byte[],off,len) writes the requested range");
        check(s.last == 'Z', "last written byte is Z");
        s.flush();
        s.close();
    }

    public static void main(String[] args) throws Exception {
        testOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
