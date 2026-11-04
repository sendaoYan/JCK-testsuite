/*
 * @test
 * @summary java.io.PipedReader API conformance: read from a connected character pipe.
 * @run main PipedReaderTest
 */

import java.io.PipedReader;
import java.io.PipedWriter;

public class PipedReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPipedReader() throws Exception {
        PipedWriter out = new PipedWriter();
        PipedReader in = new PipedReader(out);
        out.write('x');
        check(in.read() == 'x', "read back written char");
        in.close();
        out.close();
    }

    public static void main(String[] args) throws Exception {
        testPipedReader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
