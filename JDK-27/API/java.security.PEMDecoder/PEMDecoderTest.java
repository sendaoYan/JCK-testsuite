/*
 * @test
 * @summary java.security.PEMDecoder conformance: of and decode.
 * @enablePreview
 * @run main PEMDecoderTest
 */

import java.security.PEMDecoder;
import java.security.BinaryEncodable;

public class PEMDecoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        String pem = "-----BEGIN TEST-----\naGVsbG8=\n-----END TEST-----";
        BinaryEncodable be = PEMDecoder.of().decode(pem);
        check(be != null, "decode non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
