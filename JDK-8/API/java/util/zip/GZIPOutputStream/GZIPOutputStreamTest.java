/*
 * @test
 * @summary java.util.zip.GZIPOutputStream conformance: gzip write + magic header.
 * @run main GZIPOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGZIPOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        gzout.write("gzip".getBytes("UTF-8"));
        gzout.close();
        byte[] gz = bout.toByteArray();
        check(gz.length > 0, "gzip output produced");
        check(gz[0] == (byte) 0x1f && gz[1] == (byte) 0x8b, "gzip magic header 1f 8b");
    }

    public static void main(String[] args) throws Exception {
        testGZIPOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
