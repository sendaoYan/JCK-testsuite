/*
 * @test
 * @summary java.io.PipedOutputStream API conformance: write to a connected pipe.
 * @run main PipedOutputStreamTest
 */

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPipedOutputStream() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        out.write('x');
        check(in.read() == 'x', "read back written byte");
        out.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testPipedOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
