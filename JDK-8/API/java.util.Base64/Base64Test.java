/*
 * @test
 * @summary java.util.Base64 API conformance: basic encode/decode roundtrip, the canonical
 *          "hello" encoding, and URL-safe roundtrip of arbitrary bytes.
 * @run main Base64Test
 */

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Base64Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasic() {
        String enc = Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        check("aGVsbG8=".equals(enc), "encode hello");
        byte[] dec = Base64.getDecoder().decode(enc);
        check("hello".equals(new String(dec, StandardCharsets.UTF_8)), "decode hello");
    }

    static void testUrlSafe() {
        byte[] raw = new byte[]{-1, -2, 0, 127};
        byte[] dec = Base64.getUrlDecoder().decode(Base64.getUrlEncoder().encodeToString(raw));
        check(Arrays.equals(raw, dec), "URL-safe roundtrip");
    }

    public static void main(String[] args) {
        testBasic();
        testUrlSafe();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
