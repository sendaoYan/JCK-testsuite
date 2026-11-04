/*
 * @test
 * @summary java.util.Base64.Decoder conformance: decode(String/byte[]/ByteBuffer)/wrap.
 * @run main DecoderTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class DecoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecoder() throws Exception {
        Base64.Decoder d = Base64.getDecoder();
        byte[] decoded = d.decode("SGVsbG8=");
        check("Hello".equals(new String(decoded, "US-ASCII")), "decode(String)");
        byte[] decoded2 = d.decode("SGVsbG8=".getBytes("US-ASCII"));
        check("Hello".equals(new String(decoded2, "US-ASCII")), "decode(byte[])");
        ByteBuffer buf = d.decode(ByteBuffer.wrap("SGVsbG8=".getBytes("US-ASCII")));
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        check("Hello".equals(new String(arr, "US-ASCII")), "decode(ByteBuffer)");
        InputStream in = d.wrap(new ByteArrayInputStream("SGVsbG8=".getBytes("US-ASCII")));
        byte[] read = new byte[16];
        int n = in.read(read);
        check(n == 5 && "Hello".equals(new String(read, 0, n, "US-ASCII")), "wrap(InputStream)");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testDecoder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
