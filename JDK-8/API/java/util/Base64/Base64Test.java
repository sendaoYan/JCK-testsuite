/*
 * @test
 * @summary java.util.Base64 API conformance (Java 8 surface): complete contract coverage of
 *          basic/URL/MIME encode-decode roundtrips, canonical encodings, withoutPadding,
 *          wrap streams, byte[]-into-byte[] encode/decode (IAE on too-small dst), ByteBuffer
 *          roundtrip, invalid decode IAE, and the full null NPE matrix.
 * @run main Base64Test
 */

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Base64Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static void testBasic() {
        String enc = Base64.getEncoder().encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        check("aGVsbG8=".equals(enc), "encode hello");
        byte[] dec = Base64.getDecoder().decode(enc);
        check("hello".equals(new String(dec, StandardCharsets.UTF_8)), "decode hello");

        check(Base64.getEncoder().encode(new byte[0]).length == 0, "empty encodes to empty");
        check(Base64.getDecoder().decode("").length == 0, "empty decodes to empty");
        // decoder tolerates missing padding.
        check("hello".equals(new String(Base64.getDecoder().decode("aGVsbG8"),
                                        StandardCharsets.UTF_8)),
              "decoder accepts unpadded input");
        // encode length formula 4*ceil(n/3).
        for (int n = 0; n <= 5; n++) {
            int expected = ((n + 2) / 3) * 4;
            check(Base64.getEncoder().encode(new byte[n]).length == expected,
                  "encode length for " + n + " bytes");
        }
    }

    static void testUrlSafe() {
        byte[] raw = new byte[]{-1, -2, 0, 127};
        String enc = Base64.getUrlEncoder().encodeToString(raw);
        check("__4Afw==".equals(enc), "URL-safe encoding of 0xff 0xfe 0x00 0x7f");
        check(Arrays.equals(raw, Base64.getUrlDecoder().decode(enc)), "URL-safe roundtrip");
        // basic uses +/, url uses -/_.
        byte[] all = {(byte) 0xfb, (byte) 0xef};
        check("++8=".equals(Base64.getEncoder().encodeToString(all)), "basic alphabet ++8=");
        check("--8=".equals(Base64.getUrlEncoder().encodeToString(all)), "url alphabet --8=");
        // basic decoder rejects URL alphabet; url decoder accepts it.
        expectThrows(IllegalArgumentException.class, () -> Base64.getDecoder().decode("--8="),
            "basic decoder rejects URL alphabet");
        check(Arrays.equals(all, Base64.getUrlDecoder().decode("--8=")),
              "url decoder accepts URL alphabet");
    }

    static void testWithoutPadding() {
        String enc = Base64.getEncoder().withoutPadding()
            .encodeToString("hello".getBytes(StandardCharsets.UTF_8));
        check("aGVsbG8".equals(enc), "withoutPadding drops '='");
        check(Arrays.equals("hello".getBytes(StandardCharsets.UTF_8),
                Base64.getDecoder().decode(enc)), "withoutPadding decode roundtrip");
        check(Base64.getEncoder().withoutPadding().encode(new byte[1]).length == 2,
              "withoutPadding length 1 byte");
        check(Base64.getEncoder().withoutPadding().encode(new byte[2]).length == 3,
              "withoutPadding length 2 bytes");
    }

    static void testMime() {
        byte[] longBytes = new byte[200];
        for (int i = 0; i < longBytes.length; i++) longBytes[i] = (byte) i;
        String enc = Base64.getMimeEncoder().encodeToString(longBytes);
        check(Arrays.equals(longBytes, Base64.getMimeDecoder().decode(enc)),
              "MIME roundtrip");
        check(enc.contains("\r\n"), "MIME uses CRLF line separator");

        // exactly one 76-char line for 57 bytes (76 base64 chars).
        byte[] line = new byte[57];
        check(!Base64.getMimeEncoder().encodeToString(line).contains("\r\n"),
              "MIME single line <= 76 has no CRLF");
        byte[] wraps = new byte[58];
        check(Base64.getMimeEncoder().encodeToString(wraps).contains("\r\n"),
              "MIME wraps after 76 chars");
        // MIME decoder ignores line separators.
        check(Arrays.equals(longBytes, Base64.getMimeDecoder().decode(enc)),
              "MIME decoder ignores CRLF");
    }

    static void testMimeCustom() {
        byte[] data = new byte[20];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        // custom line separator and short line length forces many separators.
        byte[] sep = {'|'};
        String enc = Base64.getMimeEncoder(4, sep).encodeToString(data);
        check(enc.contains("|"), "custom MIME separator used");
        check(Arrays.equals(data, Base64.getMimeDecoder().decode(enc)),
              "custom MIME roundtrip (decoder ignores any separator)");

        // lineLength <= 0 -> no line separation (RFC4648 basic encoder).
        String noSep = Base64.getMimeEncoder(0, sep).encodeToString(data);
        check(!noSep.contains("|") && !noSep.contains("\r\n"),
              "lineLength <= 0 -> no separator");
        check(Arrays.equals(data, Base64.getMimeDecoder().decode(noSep)),
              "no-separator MIME decoder roundtrip");

        // invalid separator (base64 alphabet char) -> IAE; null separator -> NPE.
        expectThrows(IllegalArgumentException.class,
            () -> Base64.getMimeEncoder(76, new byte[]{'a'}), "MIME separator base64 char IAE");
        expectThrows(NullPointerException.class, () -> Base64.getMimeEncoder(76, null),
            "MIME null separator NPE");
    }

    static void testEncodeIntoArray() {
        byte[] src = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] dst = new byte[8];
        int n = Base64.getEncoder().encode(src, dst);
        check(n == 8, "encode(src,dst) returns written count 8");
        check("aGVsbG8=".equals(new String(dst, StandardCharsets.US_ASCII)), "encode(src,dst) contents");
        expectThrows(IllegalArgumentException.class,
            () -> Base64.getEncoder().encode(src, new byte[7]), "encode(src,dst) too small IAE");

        byte[] enc = "aGVsbG8=".getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[5];
        int m = Base64.getDecoder().decode(enc, out);
        check(m == 5, "decode(src,dst) returns written count 5");
        check("hello".equals(new String(out, StandardCharsets.UTF_8)), "decode(src,dst) contents");
        expectThrows(IllegalArgumentException.class,
            () -> Base64.getDecoder().decode(enc, new byte[4]), "decode(src,dst) too small IAE");
    }

    static void testByteBuffer() {
        ByteBuffer src = ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        ByteBuffer enc = Base64.getEncoder().encode(src);
        check(enc.position() == 0, "encode(ByteBuffer) position 0");
        byte[] encBytes = new byte[enc.remaining()];
        enc.get(encBytes);
        check("aGVsbG8=".equals(new String(encBytes, StandardCharsets.US_ASCII)),
              "encode(ByteBuffer) contents");

        ByteBuffer dec = Base64.getDecoder().decode(ByteBuffer.wrap(encBytes));
        byte[] decBytes = new byte[dec.remaining()];
        dec.get(decBytes);
        check("hello".equals(new String(decBytes, StandardCharsets.UTF_8)),
              "decode(ByteBuffer) contents");
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
        check("aGk=".equals(new String(out.toByteArray(), StandardCharsets.UTF_8)),
              "wrap encodes to stream");
    }

    static void testInvalidDecode() {
        expectThrows(IllegalArgumentException.class,
            () -> Base64.getDecoder().decode("not valid base64!"), "invalid decode IAE");
        expectThrows(IllegalArgumentException.class, () -> Base64.getDecoder().decode("a"),
            "decode length 1 mod 4 rejected");
    }

    static void testNullMatrix() {
        expectThrows(NullPointerException.class, () -> Base64.getEncoder().encode((byte[]) null),
            "encode(null) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getEncoder().encodeToString(null),
            "encodeToString(null) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getDecoder().decode((byte[]) null),
            "decode(null byte[]) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getDecoder().decode((String) null),
            "decode(null String) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getEncoder().wrap(null),
            "wrap(null OutputStream) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getDecoder().wrap(null),
            "wrap(null InputStream) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getEncoder().encode((ByteBuffer) null),
            "encode(null ByteBuffer) NPE");
        expectThrows(NullPointerException.class, () -> Base64.getDecoder().decode((ByteBuffer) null),
            "decode(null ByteBuffer) NPE");
    }

    public static void main(String[] args) {
        testBasic();
        testUrlSafe();
        testWithoutPadding();
        testMime();
        testMimeCustom();
        testEncodeIntoArray();
        testByteBuffer();
        testWrapStream();
        testInvalidDecode();
        testNullMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
