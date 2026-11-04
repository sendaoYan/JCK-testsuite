/*
 * @test
 * @summary java.io.FilterOutputStream API conformance: delegates write/flush to the
 *          underlying stream.
 * @run main FilterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;

public class FilterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFilterOutputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FilterOutputStream fos = new FilterOutputStream(bos);
        fos.write(65);
        fos.write(66);
        fos.flush();
        byte[] out = bos.toByteArray();
        check(out[0] == 65 && out[1] == 66, "delegates write");
    }

    public static void main(String[] args) throws Exception {
        testFilterOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
