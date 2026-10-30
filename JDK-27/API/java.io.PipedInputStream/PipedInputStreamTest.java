/*
 * @test
 * @summary java.io.PipedInputStream API conformance: read from a connected pipe.
 * @run main PipedInputStreamTest
 */

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPipedInputStream() throws Exception {
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(out);
        out.write('y');
        check(in.read() == 'y', "read back written byte");
        in.close();
        out.close();
    }

    public static void main(String[] args) throws Exception {
        testPipedInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
