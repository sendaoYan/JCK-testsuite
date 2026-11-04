/*
 * @test
 * @summary java.util.Base64 conformance: white-box structure plus black-box behavior of
 *          basic/URL/MIME encode-decode roundtrips, canonical encodings, withoutPadding,
 *          wrap output stream, and invalid decode throwing IllegalArgumentException.
 * @run main Base64Test
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Base64Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Base64");
        check(!c.isInterface(), "Base64 is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Base64 is final");
        c.getMethod("getEncoder");
        c.getMethod("getDecoder");
        c.getMethod("getUrlEncoder");
        c.getMethod("getUrlDecoder");
        c.getMethod("getMimeEncoder");
        c.getMethod("getMimeDecoder");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testBasic();
        testUrlSafe();
        testWithoutPadding();
        testMime();
        testWrapStream();
        testInvalidDecode();
        testBoundaryEquivalence();
    }

    static void testBasic() {
        String enc = Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        check("aGVsbG8=".equals(enc), "encode hello");
        byte[] dec = Base64.getDecoder().decode(enc);
        check("hello".equals(new String(dec, StandardCharsets.UTF_8)), "decode hello");

        byte[] empty = Base64.getEncoder().encode(new byte[0]);
        check(empty.length == 0, "empty input encodes to empty");
    }

    static void testUrlSafe() {
        byte[] raw = new byte[]{-1, -2, 0, 127};
        String enc = Base64.getUrlEncoder().encodeToString(raw);
        check("__4Afw==".equals(enc), "URL-safe encoding of 0xff 0xfe 0x00 0x7f");
        byte[] dec = Base64.getUrlDecoder().decode(enc);
        check(Arrays.equals(raw, dec), "URL-safe roundtrip");
    }

    static void testWithoutPadding() {
        String enc = Base64.getEncoder().withoutPadding().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        check("aGVsbG8".equals(enc), "withoutPadding drops '='");
        check(Arrays.equals("hello".getBytes(StandardCharsets.UTF_8),
                Base64.getDecoder().decode(enc)), "withoutPadding decode roundtrip");
    }

    static void testMime() {
        byte[] longBytes = new byte[200];
        for (int i = 0; i < longBytes.length; i++) longBytes[i] = (byte) i;
        String enc = Base64.getMimeEncoder().encodeToString(longBytes);
        byte[] dec = Base64.getMimeDecoder().decode(enc);
        check(Arrays.equals(longBytes, dec), "MIME encode/decode roundtrip");
    }

    static void testWrapStream() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        java.io.OutputStream wrapped = Base64.getEncoder().wrap(out);
        try {
            wrapped.write("hi".getBytes(StandardCharsets.UTF_8));
            wrapped.close();
        } catch (Exception e) {
            throw new AssertionError("wrap write/close failed: " + e);
        }
        check("aGk=".equals(out.toString(StandardCharsets.UTF_8)), "wrap encodes to stream");
    }

    static void testInvalidDecode() {
        assertThrows(IllegalArgumentException.class,
                () -> Base64.getDecoder().decode("not valid base64!"),
                "decode invalid throws IllegalArgumentException");
    }

    static void testBoundaryEquivalence() {
        // Encoded length formula 4*ceil(n/3) for n = 0..5.
        for (int n = 0; n <= 5; n++) {
            int expected = ((n + 2) / 3) * 4;
            check(Base64.getEncoder().encode(new byte[n]).length == expected,
                  "encode length for " + n + " bytes");
        }

        // withoutPadding drops '=' for non-multiple-of-3 inputs.
        check(Base64.getEncoder().withoutPadding().encode(new byte[1]).length == 2,
              "withoutPadding length 1 byte");
        check(Base64.getEncoder().withoutPadding().encode(new byte[2]).length == 3,
              "withoutPadding length 2 bytes");

        // Basic alphabet uses +/; URL alphabet uses -/_.
        byte[] all = {(byte) 0xfb, (byte) 0xef};
        check(Base64.getEncoder().encodeToString(all).equals("++8="), "basic alphabet ++8=");
        check(Base64.getUrlEncoder().encodeToString(all).equals("--8="), "url alphabet --8=");

        // MIME wraps at 76 columns with CRLF.
        byte[] line = new byte[57];   // 76 base64 chars, exactly one line
        String mime1 = Base64.getMimeEncoder().encodeToString(line);
        check(!mime1.contains("\r\n"), "MIME single line <= 76 has no CRLF");
        byte[] wraps = new byte[58];  // 80 base64 chars, forces a wrap
        String mime2 = Base64.getMimeEncoder().encodeToString(wraps);
        check(mime2.contains("\r\n"), "MIME wraps after 76 chars");

        // Basic decoder tolerates missing padding.
        check("hello".equals(new String(Base64.getDecoder().decode("aGVsbG8"),
                                        StandardCharsets.UTF_8)),
              "decoder accepts unpadded input");

        // Invalid length (1 mod 4) is rejected.
        assertThrows(IllegalArgumentException.class, () -> Base64.getDecoder().decode("a"),
                "decode length 1 mod 4 rejected");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
