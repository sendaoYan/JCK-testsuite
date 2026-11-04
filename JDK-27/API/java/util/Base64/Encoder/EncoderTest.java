/*
 * @test
 * @summary java.util.Base64.Encoder conformance: encode/encodeToString/withoutPadding/wrap.
 * @run main EncoderTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class EncoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEncoder() throws Exception {
        Base64.Encoder e = Base64.getEncoder();
        byte[] encoded = e.encode("Hello".getBytes("US-ASCII"));
        check("SGVsbG8=".equals(new String(encoded, "US-ASCII")), "encode(byte[])");
        check("SGVsbG8=".equals(e.encodeToString("Hello".getBytes("US-ASCII"))), "encodeToString");
        ByteBuffer buf = e.encode(ByteBuffer.wrap("Hello".getBytes("US-ASCII")));
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        check("SGVsbG8=".equals(new String(arr, "US-ASCII")), "encode(ByteBuffer)");
        check("SGVsbG8".equals(e.withoutPadding().encodeToString("Hello".getBytes("US-ASCII"))),
              "withoutPadding");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream os = e.wrap(bout);
        os.write("Hello".getBytes("US-ASCII"));
        os.close();
        check("SGVsbG8=".equals(new String(bout.toByteArray(), "US-ASCII")), "wrap(OutputStream)");
    }

    public static void main(String[] args) throws Exception {
        testEncoder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
