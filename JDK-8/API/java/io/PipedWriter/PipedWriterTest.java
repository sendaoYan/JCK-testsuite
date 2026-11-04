/*
 * @test
 * @summary java.io.PipedWriter API conformance: write to a connected character pipe.
 * @run main PipedWriterTest
 */

import java.io.PipedReader;
import java.io.PipedWriter;

public class PipedWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPipedWriter() throws Exception {
        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        out.write('y');
        check(in.read() == 'y', "read back written char");
        out.close();
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testPipedWriter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
