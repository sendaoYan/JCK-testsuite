/*
 * @test
 * @summary java.util.zip.GZIPInputStream conformance: round-trip gzip decompress.
 * @run main GZIPInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGZIPInputStream() throws Exception {
        byte[] data = "gzip data gzip data".getBytes("UTF-8");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        gzout.write(data);
        gzout.close();

        GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] result = new byte[256];
        int n = gzin.read(result);
        check(n == data.length, "read returns decompressed length");
        check(Arrays.equals(data, Arrays.copyOf(result, n)), "content restored");
        gzin.close();
    }

    public static void main(String[] args) throws Exception {
        testGZIPInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
