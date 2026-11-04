/*
 * @test
 * @summary java.nio.charset.CharsetDecoder conformance: decode via UTF-8 decoder.
 * @run main CharsetDecoderTest
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class CharsetDecoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharsetDecoder() {
        CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder();
        check(dec.charset().equals(StandardCharsets.UTF_8), "charset");
        check(!dec.isAutoDetecting(), "isAutoDetecting false");
        try {
            dec.isCharsetDetected();
            check(false, "isCharsetDetected should throw when not auto-detecting");
        } catch (UnsupportedOperationException expected) {
            check(true, "isCharsetDetected throws UnsupportedOperationException");
        }
        dec.onMalformedInput(CodingErrorAction.REPLACE);
        dec.onUnmappableCharacter(CodingErrorAction.REPLACE);
        check(dec.malformedInputAction() == CodingErrorAction.REPLACE, "malformedInputAction");
        check(dec.unmappableCharacterAction() == CodingErrorAction.REPLACE, "unmappableCharacterAction");
        check(dec.replacement().length() > 0, "replacement non-empty");

        dec.reset();
        ByteBuffer in = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        CharBuffer out = CharBuffer.allocate(16);
        CoderResult r = dec.decode(in, out, true);
        check(r.isUnderflow(), "decode underflow");
        dec.flush(out);
        out.flip();
        check("hello".equals(out.toString()), "decoded hello");
    }

    public static void main(String[] args) {
        testCharsetDecoder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
