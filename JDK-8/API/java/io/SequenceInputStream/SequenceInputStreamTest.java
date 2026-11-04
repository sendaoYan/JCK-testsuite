/*
 * @test
 * @summary java.io.SequenceInputStream API conformance: concatenated sequential read.
 * @run main SequenceInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.SequenceInputStream;

public class SequenceInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSequenceInputStream() throws Exception {
        SequenceInputStream sis =
            new SequenceInputStream(new ByteArrayInputStream(new byte[]{1, 2}),
                                    new ByteArrayInputStream(new byte[]{3, 4}));
        check(sis.read() == 1 && sis.read() == 2 && sis.read() == 3 && sis.read() == 4
                  && sis.read() == -1,
              "sequential read");
    }

    public static void main(String[] args) throws Exception {
        testSequenceInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
