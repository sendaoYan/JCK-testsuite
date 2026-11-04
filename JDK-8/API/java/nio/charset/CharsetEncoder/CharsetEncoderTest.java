/*
 * @test
 * @summary java.nio.charset.CharsetEncoder conformance: encode via UTF-8 encoder.
 * @run main CharsetEncoderTest
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;

public class CharsetEncoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharsetEncoder() {
        CharsetEncoder enc = StandardCharsets.UTF_8.newEncoder();
        check(enc.charset().equals(StandardCharsets.UTF_8), "charset");
        check(enc.averageBytesPerChar() >= 1.0f, "averageBytesPerChar");
        check(enc.maxBytesPerChar() >= 1.0f, "maxBytesPerChar");

        enc.reset();
        CharBuffer in = CharBuffer.wrap("hello");
        ByteBuffer out = ByteBuffer.allocate(16);
        CoderResult r = enc.encode(in, out, true);
        check(r.isUnderflow(), "encode underflow");
        enc.flush(out);
        out.flip();
        byte[] bytes = new byte[out.remaining()];
        out.get(bytes);
        check("hello".equals(new String(bytes, StandardCharsets.UTF_8)), "encoded hello");
    }

    public static void main(String[] args) {
        testCharsetEncoder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
