/*
 * @test
 * @summary java.security.PEMEncoder conformance: of and encodeToString.
 * @enablePreview
 * @run main PEMEncoderTest
 */

import java.security.PEM;
import java.security.PEMEncoder;

public class PEMEncoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PEM p = new PEM("TEST", "aGVsbG8=");
        String s = PEMEncoder.of().encodeToString(p);
        check(s != null, "encodeToString non-null");
        check(s.contains("-----BEGIN TEST-----"), "has BEGIN");
        check(s.contains("-----END TEST-----"), "has END");
        byte[] b = PEMEncoder.of().encode(p);
        check(b != null && b.length > 0, "encode non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
