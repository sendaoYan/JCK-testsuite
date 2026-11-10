/*
 * @test
 * @summary java.util.Base64.Decoder conformance: decode(String/byte[]/ByteBuffer)/wrap.
 * @run main DecoderTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class DecoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDecoder() throws Exception {
        Base64.Decoder d = Base64.getDecoder();
        byte[] decoded = d.decode("SGVsbG8=");
        check("Hello".equals(new String(decoded, "US-ASCII")), "decode(String)");
        byte[] decoded2 = d.decode("SGVsbG8=".getBytes("US-ASCII"));
        check("Hello".equals(new String(decoded2, "US-ASCII")), "decode(byte[])");
        ByteBuffer buf = d.decode(ByteBuffer.wrap("SGVsbG8=".getBytes("US-ASCII")));
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        check("Hello".equals(new String(arr, "US-ASCII")), "decode(ByteBuffer)");
        InputStream in = d.wrap(new ByteArrayInputStream("SGVsbG8=".getBytes("US-ASCII")));
        byte[] read = new byte[16];
        int n = in.read(read);
        check(n == 5 && "Hello".equals(new String(read, 0, n, "US-ASCII")), "wrap(InputStream)");
        in.close();
    }

    // ---- 契约深化 ----
    static void testDecoderContracts() throws Exception {
        Base64.Decoder d = Base64.getDecoder();
        check(Base64.getDecoder() == Base64.getDecoder(), "getDecoder cached instance");
        check(Base64.getUrlDecoder() == Base64.getUrlDecoder(), "getUrlDecoder cached");
        check(Base64.getMimeDecoder() == Base64.getMimeDecoder(), "getMimeDecoder cached");
        check(Base64.getDecoder() != Base64.getMimeDecoder(), "basic != mime decoder");
        // RFC4648 向量解码
        check("foobar".equals(new String(d.decode("Zm9vYmFy"), "US-ASCII")), "vector foobar");
        check("f".equals(new String(d.decode("Zg=="), "US-ASCII")), "vector f");
        check("fo".equals(new String(d.decode("Zm8="), "US-ASCII")), "vector fo");
        check("".equals(new String(d.decode(""), "US-ASCII")), "empty decode");
        // decode(String) 与 decode(byte[]) 一致
        check(java.util.Arrays.equals(d.decode("Zm9v"), d.decode("Zm9v".getBytes("US-ASCII"))),
              "String and byte[] overloads agree");
        // ByteBuffer 解码：输出 position 从 0、limit 在结尾
        ByteBuffer in = ByteBuffer.wrap("Zm9vYg==".getBytes("US-ASCII"));
        ByteBuffer out = d.decode(in);
        check(out.position() == 0 && out.remaining() == 4, "ByteBuffer decode bounds");
        byte[] arr = new byte[out.remaining()];
        out.get(arr);
        check("foob".equals(new String(arr, "US-ASCII")), "ByteBuffer decode content");
        // 只读 ByteBuffer 输入
        ByteBuffer roOut = d.decode(ByteBuffer.wrap("Zg==".getBytes("US-ASCII")).asReadOnlyBuffer());
        check(roOut.remaining() == 1, "read-only input accepted");
        // 非 4 倍长度
        try {
            d.decode("abcde");
            check(false, "length 5 rejected");
        } catch (IllegalArgumentException iae) {
            check(true, "length 5 rejected");
        }
        try {
            d.decode("ab=c");
            check(false, "mid '=' rejected");
        } catch (IllegalArgumentException iae) {
            check(true, "mid '=' rejected");
        }
        // MIME 容忍垃圾；basic 不容忍
        check("fo".equals(new String(Base64.getMimeDecoder().decode("Z m8"), "US-ASCII")),
              "MIME ignores space");
        try {
            d.decode("Z m8");
            check(false, "basic rejects space");
        } catch (IllegalArgumentException iae) {
            check(true, "basic rejects space");
        }
        // null NPE
        try {
            d.decode((String) null);
            check(false, "decode(null String) NPE");
        } catch (NullPointerException npe) {
            check(true, "decode(null String) NPE");
        }
        try {
            d.decode((ByteBuffer) null);
            check(false, "decode(null ByteBuffer) NPE");
        } catch (NullPointerException npe) {
            check(true, "decode(null ByteBuffer) NPE");
        }
    }

    // ---- 契约深化（第二层）：decode(byte[],byte[]) 边界 / wrap(null) / 无 padding 与非法长度 / URL-MIME 字母表 ----
    static void testDecoderMoreContracts() throws Exception {
        Base64.Decoder d = Base64.getDecoder();
        // decode(byte[], byte[]) 返回写入字节数，恰好尺寸写满
        check(d.decode("Zm9v".getBytes("US-ASCII"), new byte[8]) == 3, "decode(src,dst) returns 3");
        byte[] ddst = new byte[3];
        check(d.decode("Zm9v".getBytes("US-ASCII"), ddst) == 3 && "foo".equals(new String(ddst, "US-ASCII")),
              "decode(src,dst) exact bytes");
        // dst 空间不足 -> IAE；null 参数 -> NPE
        try {
            d.decode("Zm9v".getBytes("US-ASCII"), new byte[2]);
            check(false, "decode dst too small IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "decode dst too small IAE");
        }
        try {
            d.decode("Zm9v".getBytes("US-ASCII"), (byte[]) null);
            check(false, "decode(src,null) NPE");
        } catch (NullPointerException npe) {
            check(true, "decode(src,null) NPE");
        }
        try {
            d.decode((byte[]) null, new byte[8]);
            check(false, "decode(null,dst) NPE");
        } catch (NullPointerException npe) {
            check(true, "decode(null,dst) NPE");
        }
        try {
            d.wrap(null);
            check(false, "wrap(null) NPE");
        } catch (NullPointerException npe) {
            check(true, "wrap(null) NPE");
        }
        // 无 padding 解码：长度 mod 4 == 2 或 3 合法；mod 4 == 1 非法
        check("fo".equals(new String(d.decode("Zm8"), "US-ASCII")), "no-pad 3 chars -> fo");
        check("foo".equals(new String(d.decode("Zm9v"), "US-ASCII")), "no-pad 4 chars -> foo");
        check("f".equals(new String(d.decode("Zm"), "US-ASCII")), "no-pad 2 chars -> f");
        check("".equals(new String(d.decode(""), "US-ASCII")), "empty decode");
        try {
            d.decode("Z");
            check(false, "single-char base64 IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "single-char base64 IAE");
        }
        // 非法字符 -> IAE
        try {
            d.decode("Z!m8");
            check(false, "invalid char IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "invalid char IAE");
        }
        // URL 解码器：- _ 对应 + /
        byte[] raw = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xef};
        check(java.util.Arrays.equals(raw, Base64.getUrlDecoder().decode("-__v")), "url decode - and _");
        // MIME 解码器忽略 CR/LF
        check("foobar".equals(new String(Base64.getMimeDecoder().decode("Zm9v\r\nYmFy"), "US-ASCII")),
              "MIME ignores CRLF");
    }

    public static void main(String[] args) throws Exception {
        testDecoder();
        testDecoderContracts();
        testDecoderMoreContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
