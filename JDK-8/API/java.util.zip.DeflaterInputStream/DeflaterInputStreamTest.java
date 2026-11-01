/*
 * @test
 * @summary java.util.zip.DeflaterInputStream conformance: compressing read.
 * @run main DeflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.util.zip.DeflaterInputStream;

public class DeflaterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeflaterInputStream() throws Exception {
        byte[] data = "stream data stream data stream data".getBytes("UTF-8");
        DeflaterInputStream in = new DeflaterInputStream(new ByteArrayInputStream(data));
        byte[] buf = new byte[256];
        int n = in.read(buf);
        check(n > 0, "read returns compressed data");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testDeflaterInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
