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

    // ===================== 契约深化：RFC 4648 向量/解码错误消息/非规范输入 =====================

    static void testRfc4648Vectors() {
        // RFC 4648 第 10 节测试向量（basic 字母表）
        String[][] vec = {
            {"", ""},
            {"f", "Zg=="},
            {"fo", "Zm8="},
            {"foo", "Zm9v"},
            {"foob", "Zm9vYg=="},
            {"fooba", "Zm9vYmE="},
            {"foobar", "Zm9vYmFy"},
        };
        for (String[] v : vec) {
            String enc = Base64.getEncoder().encodeToString(v[0].getBytes(StandardCharsets.UTF_8));
            check(v[1].equals(enc), "RFC4648 vector encode '" + v[0] + "'");
            byte[] dec = Base64.getDecoder().decode(v[1]);
            check(v[0].equals(new String(dec, StandardCharsets.UTF_8)), "RFC4648 vector decode " + v[1]);
        }
        // 0xfb 0xef 0xbe 三字节：四个 6-bit 组均为 62 → basic "++++"、url "----"
        byte[] three = {(byte) 0xfb, (byte) 0xef, (byte) 0xbe};
        check("++++".equals(Base64.getEncoder().encodeToString(three)), "0xfbefbe -> ++++");
        check("----".equals(Base64.getUrlEncoder().encodeToString(three)), "0xfbefbe url -> ----");
        check(Arrays.equals(three, Base64.getUrlDecoder().decode("----")), "---- decode roundtrip");
    }

    static void testDecodeErrorMessages() {
        // 长度 <2 → IAE "at least have 2 bytes"
        try {
            Base64.getDecoder().decode("Z");
            check(false, "length-1 decode should fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("2 bytes"),
                  "length-1 message mentions 2 bytes");
        }
        // 非法字符 → IAE "Illegal base64 character"
        try {
            Base64.getDecoder().decode("Zm9*");
            check(false, "bad char decode should fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Illegal base64 character"),
                  "illegal char message");
        }
        // 中间 '=' 后又有数据 → IAE "incorrect ending byte"
        try {
            Base64.getDecoder().decode("Zg==Zg==");
            check(false, "mid-padding decode should fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("ending"),
                  "mid-padding message mentions ending byte");
        }
        // basic 拒绝 CRLF；MIME 容忍并忽略
        expectThrows(IllegalArgumentException.class, () -> Base64.getDecoder().decode("Zm9v\r\nYmFy"),
                     "basic decoder rejects CRLF");
        check("foobar".equals(new String(
                Base64.getMimeDecoder().decode("Zm9v\r\nYmFy"), StandardCharsets.UTF_8)),
              "MIME decoder ignores CRLF");
        // MIME 解码器忽略任意非字母表字符（含注释式垃圾）
        check("fo".equals(new String(
                Base64.getMimeDecoder().decode("Zm!8="), StandardCharsets.UTF_8)),
              "MIME decoder ignores illegal chars");
        // URL 解码器拒绝 basic 字母表的 '+'
        expectThrows(IllegalArgumentException.class, () -> Base64.getUrlDecoder().decode("++8="),
                     "URL decoder rejects '+'");
        // 无填充输入被接受（残余位为零）
        check("f".equals(new String(Base64.getDecoder().decode("Zg"), StandardCharsets.UTF_8)),
              "unpadded 2-char accepted");
        check("fo".equals(new String(Base64.getDecoder().decode("Zm8"), StandardCharsets.UTF_8)),
              "unpadded 3-char accepted");
        // 残余位非零：仍解码（前缀语义）
        check(Base64.getDecoder().decode("Zh").length == 1, "2-char with nonzero tail decodes 1 byte");
    }

    static void testEncoderIntoArrayErrors() {
        try {
            Base64.getEncoder().encode(new byte[2], new byte[1]);
            check(false, "encode into small dst should fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("too small"),
                  "encode dst-too-small message");
        }
        byte[] okDst = new byte[4];
        check(Base64.getEncoder().encode(new byte[2], okDst) == 4, "encode into dst returns bytes written");
        try {
            Base64.getDecoder().decode("Zm8=".getBytes(StandardCharsets.US_ASCII), new byte[1]);
            check(false, "decode into small dst should fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null, "decode dst-too-small message non-null");
        }
        // encode/decode(src,dst) 只写 dst 前缀
        byte[] decDst = new byte[8];
        int n = Base64.getDecoder().decode("Zm8=".getBytes(StandardCharsets.US_ASCII), decDst);
        check(n == 2 && decDst[0] == 'f' && decDst[1] == 'o' && decDst[2] == 0,
              "decode into dst returns count and leaves rest untouched");
    }

    static void testMimeLineLayout() {
        byte[] hundred = new byte[100];
        String mime = Base64.getMimeEncoder().encodeToString(hundred);
        // 100 字节 → 132 个数据字符 + 1 个 CRLF = 138；两行（76 + 56）
        check(mime.length() == 138, "MIME 100-byte length 138");
        String[] lines = mime.split("\r\n", -1);
        check(lines.length == 2 && lines[0].length() == 76, "MIME first line 76 chars");
        check(lines[1].endsWith("==")
              && !lines[1].substring(0, lines[1].length() - 2).contains("="),
              "MIME second line has padding only at end");
        check(mime.endsWith("="), "MIME output ends with padding");
        // 自定义分隔符与行宽
        Base64.Encoder custom = Base64.getMimeEncoder(10, "$".getBytes(StandardCharsets.US_ASCII));
        String c = custom.encodeToString(new byte[30]);
        check("AAAAAAAA$AAAAAAAA$AAAAAAAA$AAAAAAAA$AAAAAAAA".equals(c), "custom separator layout");
        // 解码忽略自定义分隔符
        check(new byte[30].length == Base64.getMimeDecoder().decode(c).length, "custom sep decode length");
    }

    static void testWrapStreamRoundtrip() throws Exception {
        // wrap 双向 round-trip（含 >76 字节触发分块）
        byte[] payload = new byte[250];
        for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        java.io.OutputStream os = Base64.getEncoder().wrap(bos);
        os.write(payload);
        os.close();
        java.io.InputStream is = Base64.getDecoder().wrap(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        byte[] back = new byte[250];
        int off = 0, r;
        while ((r = is.read(back, off, back.length - off)) > 0) off += r;
        check(off == 250 && Arrays.equals(payload, back), "wrap encode/decode 250-byte roundtrip");
        is.close();
        // MIME wrap 解码忽略 CRLF
        java.io.InputStream mis = Base64.getMimeDecoder().wrap(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        off = 0;
        while ((r = mis.read(back, off, back.length - off)) > 0) off += r;
        check(off == 250, "MIME wrap stream roundtrip length");
        mis.close();
    }

    public static void main(String[] args) throws Exception {
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
        testRfc4648Vectors();
        testDecodeErrorMessages();
        testEncoderIntoArrayErrors();
        testMimeLineLayout();
        testWrapStreamRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
