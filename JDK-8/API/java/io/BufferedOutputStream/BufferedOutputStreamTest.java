/*
 * @test
 * @summary java.io.BufferedOutputStream API conformance: write byte/array and flush.
 * @run main BufferedOutputStreamTest
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

public class BufferedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBufferedOutputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BufferedOutputStream b = new BufferedOutputStream(bos);
        b.write(65);
        b.write(new byte[]{66, 67});
        b.flush();
        byte[] out = bos.toByteArray();
        check(out.length == 3 && out[0] == 65 && out[1] == 66 && out[2] == 67,
              "write + flush");
    }

    public static void main(String[] args) throws Exception {
        testBufferedOutputStream();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
