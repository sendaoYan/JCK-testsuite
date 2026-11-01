/*
 * @test
 * @summary java.util.zip.DeflaterOutputStream conformance: compressing write.
 * @run main DeflaterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.util.zip.DeflaterOutputStream;

public class DeflaterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeflaterOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DeflaterOutputStream out = new DeflaterOutputStream(bout);
        out.write("stream data stream data".getBytes("UTF-8"));
        out.finish();
        out.close();
        check(bout.size() > 0, "compressed output produced");
    }

    public static void main(String[] args) throws Exception {
        testDeflaterOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
