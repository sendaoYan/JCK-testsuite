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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
