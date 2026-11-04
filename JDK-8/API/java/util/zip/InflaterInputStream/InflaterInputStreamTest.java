/*
 * @test
 * @summary java.util.zip.InflaterInputStream conformance: round-trip decompress.
 * @run main InflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class InflaterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInflaterInputStream() throws Exception {
        byte[] data = "stream data stream data".getBytes("UTF-8");
        ByteArrayOutputStream comp = new ByteArrayOutputStream();
        DeflaterOutputStream dout = new DeflaterOutputStream(comp);
        dout.write(data);
        dout.finish();
        dout.close();

        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        byte[] result = new byte[256];
        int n = in.read(result);
        check(n == data.length, "read returns decompressed length");
        check(Arrays.equals(data, Arrays.copyOf(result, n)), "content restored");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testInflaterInputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
