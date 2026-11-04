/*
 * @test
 * @summary java.nio.charset.Charset API conformance: name, default, and encode/decode
 *          roundtrip.
 * @run main CharsetTest
 */

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class CharsetTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCharset() {
        Charset cs = Charset.forName("UTF-8");
        check("UTF-8".equals(cs.name()), "name");
        check(cs.canEncode(), "canEncode");
        check(Charset.defaultCharset() != null, "default charset non-null");

        ByteBuffer encoded = cs.encode("hello");
        check(encoded.remaining() == 5, "encode length");
        check("hello".equals(cs.decode(encoded).toString()), "decode roundtrip");
    }

    public static void main(String[] args) {
        testCharset();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
