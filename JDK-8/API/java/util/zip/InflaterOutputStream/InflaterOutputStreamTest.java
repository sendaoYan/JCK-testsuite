/*
 * @test
 * @summary java.util.zip.InflaterOutputStream conformance: round-trip decompress.
 * @run main InflaterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class InflaterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInflaterOutputStream() throws Exception {
        byte[] data = "stream data stream data".getBytes("UTF-8");
        ByteArrayOutputStream comp = new ByteArrayOutputStream();
        DeflaterOutputStream dout = new DeflaterOutputStream(comp);
        dout.write(data);
        dout.finish();
        dout.close();

        ByteArrayOutputStream decomp = new ByteArrayOutputStream();
        InflaterOutputStream iout = new InflaterOutputStream(decomp);
        iout.write(comp.toByteArray());
        iout.finish();
        iout.close();
        check(Arrays.equals(data, decomp.toByteArray()), "round-trip restores content");
    }

    public static void main(String[] args) throws Exception {
        testInflaterOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
