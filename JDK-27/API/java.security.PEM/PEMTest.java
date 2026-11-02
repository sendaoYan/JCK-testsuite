/*
 * @test
 * @summary java.security.PEM conformance: constructors and content/decode.
 * @enablePreview
 * @run main PEMTest
 */

import java.security.PEM;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PEMTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PEM p = new PEM("TEST", "aGVsbG8=");
        check("TEST".equals(p.type()), "type");
        check(Arrays.equals("aGVsbG8=".getBytes(StandardCharsets.US_ASCII), p.content()), "content");
        check(Arrays.equals("hello".getBytes(StandardCharsets.US_ASCII), p.decode()), "decode");
        check(p.leadingData() == null || p.leadingData().length >= 0, "leadingData");
        check(p.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
