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
        testNullAndDstMatrix();
    }

    static void testNullAndDstMatrix() {
        assertThrows(NullPointerException.class, () -> Base64.getEncoder().encode((byte[]) null),
                "encode(null) NPE");
        assertThrows(NullPointerException.class, () -> Base64.getEncoder().encodeToString(null),
                "encodeToString(null) NPE");
        assertThrows(NullPointerException.class, () -> Base64.getDecoder().decode((byte[]) null),
                "decode(null byte[]) NPE");
        assertThrows(NullPointerException.class, () -> Base64.getDecoder().decode((String) null),
                "decode(null String) NPE");
        assertThrows(NullPointerException.class, () -> Base64.getEncoder().wrap(null),
                "wrap(null OutputStream) NPE");
        assertThrows(NullPointerException.class, () -> Base64.getDecoder().wrap(null),
                "wrap(null InputStream) NPE");

        // encode/decode into destination array (IAE when too small).
        byte[] src = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] dst = new byte[8];
        check(Base64.getEncoder().encode(src, dst) == 8, "encode(src,dst) returns 8");
        check("aGVsbG8=".equals(new String(dst, StandardCharsets.US_ASCII)), "encode(src,dst) content");
        assertThrows(IllegalArgumentException.class,
                () -> Base64.getEncoder().encode(src, new byte[7]), "encode(src,dst) too small IAE");
        byte[] enc = "aGVsbG8=".getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[5];
        check(Base64.getDecoder().decode(enc, out) == 5, "decode(src,dst) returns 5");
        check("hello".equals(new String(out, StandardCharsets.UTF_8)), "decode(src,dst) content");
        assertThrows(IllegalArgumentException.class,
                () -> Base64.getDecoder().decode(enc, new byte[4]), "decode(src,dst) too small IAE");

        // ByteBuffer roundtrip.
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap("hello".getBytes(StandardCharsets.UTF_8));
        java.nio.ByteBuffer e = Base64.getEncoder().encode(bb);
        byte[] eb = new byte[e.remaining()];
        e.get(eb);
        check("aGVsbG8=".equals(new String(eb, StandardCharsets.US_ASCII)), "encode(ByteBuffer)");
        java.nio.ByteBuffer d = Base64.getDecoder().decode(java.nio.ByteBuffer.wrap(eb));
        byte[] db = new byte[d.remaining()];
        d.get(db);
        check("hello".equals(new String(db, StandardCharsets.UTF_8)), "decode(ByteBuffer)");

        // MIME custom line length/separator; null separator NPE; base64-char separator IAE.
        byte[] data = new byte[20];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        byte[] sep = {'|'};
        String custom = Base64.getMimeEncoder(4, sep).encodeToString(data);
        check(custom.contains("|"), "custom MIME separator used");
        check(Arrays.equals(data, Base64.getMimeDecoder().decode(custom)), "custom MIME roundtrip");
        String noSep = Base64.getMimeEncoder(0, sep).encodeToString(data);
        check(!noSep.contains("|") && !noSep.contains("\r\n"), "lineLength<=0 no separator");
        assertThrows(NullPointerException.class, () -> Base64.getMimeEncoder(76, null),
                "MIME null separator NPE");
        assertThrows(IllegalArgumentException.class,
                () -> Base64.getMimeEncoder(76, new byte[]{'a'}), "MIME base64-char separator IAE");
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
        assertThrows(IllegalArgumentException.class, () -> Base64.getDecoder().decode("Zm9v\r\nYmFy"),
                     "basic decoder rejects CRLF");
        check("foobar".equals(new String(
                Base64.getMimeDecoder().decode("Zm9v\r\nYmFy"), StandardCharsets.UTF_8)),
              "MIME decoder ignores CRLF");
        // MIME 解码器忽略任意非字母表字符（含注释式垃圾）
        check("fo".equals(new String(
                Base64.getMimeDecoder().decode("Zm!8="), StandardCharsets.UTF_8)),
              "MIME decoder ignores illegal chars");
        // URL 解码器拒绝 basic 字母表的 '+'
        assertThrows(IllegalArgumentException.class, () -> Base64.getUrlDecoder().decode("++8="),
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
        testWhiteBox();
        testBlackBox();
        testRfc4648Vectors();
        testDecodeErrorMessages();
        testEncoderIntoArrayErrors();
        testMimeLineLayout();
        testWrapStreamRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
