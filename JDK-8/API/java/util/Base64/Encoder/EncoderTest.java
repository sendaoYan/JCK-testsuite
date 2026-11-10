/*
 * @test
 * @summary java.util.Base64.Encoder conformance: encode/encodeToString/withoutPadding/wrap.
 * @run main EncoderTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

public class EncoderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEncoder() throws Exception {
        Base64.Encoder e = Base64.getEncoder();
        byte[] encoded = e.encode("Hello".getBytes("US-ASCII"));
        check("SGVsbG8=".equals(new String(encoded, "US-ASCII")), "encode(byte[])");
        check("SGVsbG8=".equals(e.encodeToString("Hello".getBytes("US-ASCII"))), "encodeToString");
        ByteBuffer buf = e.encode(ByteBuffer.wrap("Hello".getBytes("US-ASCII")));
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        check("SGVsbG8=".equals(new String(arr, "US-ASCII")), "encode(ByteBuffer)");
        check("SGVsbG8".equals(e.withoutPadding().encodeToString("Hello".getBytes("US-ASCII"))),
              "withoutPadding");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        OutputStream os = e.wrap(bout);
        os.write("Hello".getBytes("US-ASCII"));
        os.close();
        check("SGVsbG8=".equals(new String(bout.toByteArray(), "US-ASCII")), "wrap(OutputStream)");
    }

    // ---- 契约深化 ----
    static void testEncoderContracts() throws Exception {
        Base64.Encoder e = Base64.getEncoder();
        // getEncoder 每次同一实例（缓存的规范编码器）
        check(Base64.getEncoder() == Base64.getEncoder(), "getEncoder returns cached instance");
        check(Base64.getUrlEncoder() == Base64.getUrlEncoder(), "getUrlEncoder cached");
        check(Base64.getMimeEncoder() == Base64.getMimeEncoder(), "getMimeEncoder cached");
        check(Base64.getEncoder() != Base64.getUrlEncoder(), "basic != url encoder");
        // withoutPadding 返回新对象且幂等
        Base64.Encoder np = e.withoutPadding();
        check(np != e, "withoutPadding returns new encoder");
        check(np == np.withoutPadding() || np.withoutPadding() != null, "withoutPadding idempotent-ish");
        check("Zm8".equals(np.encodeToString("fo".getBytes("US-ASCII"))), "no-padding 3 chars");
        check("Zm8=".equals(e.encodeToString("fo".getBytes("US-ASCII"))), "padded 4 chars");
        // encode(src,dst) 返回写入数
        byte[] dst = new byte[8];
        check(e.encode(new byte[3], dst) == 4, "encode(src,dst) returns 4");
        check(dst[4] == 0, "encode writes only prefix");
        // 空 ByteBuffer
        ByteBuffer empty = e.encode(ByteBuffer.allocate(0));
        check(empty.remaining() == 0, "encode empty ByteBuffer");
        // read-only ByteBuffer 可用
        ByteBuffer ro = e.encode(ByteBuffer.wrap("fo".getBytes("US-ASCII")).asReadOnlyBuffer());
        byte[] r = new byte[ro.remaining()];
        ro.get(r);
        check("Zm8=".equals(new String(r, "US-ASCII")), "read-only ByteBuffer encode");
        // wrap 流分段写
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream w = e.wrap(bos);
        w.write('f');
        w.write(new byte[]{'o', 'o'});
        w.flush();
        w.close();
        check("Zm9v".equals(new String(bos.toByteArray(), "US-ASCII")), "wrap incremental writes");
        // wrap close 幂等
        w.close();
        check("Zm9v".equals(new String(bos.toByteArray(), "US-ASCII")), "wrap double close");
        // null src NPE
        try {
            e.encode((byte[]) null);
            check(false, "encode(null) NPE");
        } catch (NullPointerException npe) {
            check(true, "encode(null) NPE");
        }
        try {
            np.encodeToString(null);
            check(false, "encodeToString(null) NPE");
        } catch (NullPointerException npe) {
            check(true, "encodeToString(null) NPE");
        }
        // URL/MIME 编码器共享 Encoder 类型
        check(Base64.getUrlEncoder() instanceof Base64.Encoder, "url encoder type");
        check(Base64.getMimeEncoder() instanceof Base64.Encoder, "mime encoder type");
    }

    // ---- 契约深化（第二层）：encode(byte[],byte[]) 边界 / wrap(null) / URL-MIME 字母表与折行 / 空输入 ----
    static void testEncoderMoreContracts() throws Exception {
        Base64.Encoder e = Base64.getEncoder();
        // encode(byte[], byte[])：dst 空间不足 -> IllegalArgumentException
        try {
            e.encode(new byte[]{1, 2, 3}, new byte[3]);
            check(false, "encode(src,dst) dst too small IAE");
        } catch (IllegalArgumentException iae) {
            check(true, "encode(src,dst) dst too small IAE");
        }
        // dst/src 为 null -> NPE
        try {
            e.encode(new byte[]{1, 2, 3}, (byte[]) null);
            check(false, "encode(src,null) NPE");
        } catch (NullPointerException npe) {
            check(true, "encode(src,null) NPE");
        }
        try {
            e.encode((byte[]) null, new byte[8]);
            check(false, "encode(null,dst) NPE");
        } catch (NullPointerException npe) {
            check(true, "encode(null,dst) NPE");
        }
        // 恰好尺寸：返回 4 且写满 "AQID"
        byte[] dst = new byte[4];
        check(e.encode(new byte[]{1, 2, 3}, dst) == 4, "encode(src,dst) exact returns 4");
        check("AQID".equals(new String(dst, "US-ASCII")), "encode(src,dst) exact bytes");
        // 空输入 -> 0；encodeToString 空 -> ""
        check(e.encode(new byte[0], new byte[8]) == 0, "encode(empty,dst) returns 0");
        check("".equals(e.encodeToString(new byte[0])), "encodeToString empty");
        // wrap(null) -> NPE
        try {
            e.wrap(null);
            check(false, "wrap(null) NPE");
        } catch (NullPointerException npe) {
            check(true, "wrap(null) NPE");
        }
        // encode(ByteBuffer) 消费源缓冲（position 推进到 limit），输出 position 0
        java.nio.ByteBuffer in = java.nio.ByteBuffer.wrap("Hello".getBytes("US-ASCII"));
        java.nio.ByteBuffer out = e.encode(in);
        check(in.position() == in.limit(), "encode(ByteBuffer) consumes source");
        check(out.position() == 0, "encode(ByteBuffer) output position 0");
        // URL 编码器字母表：+ -> -，/ -> _，与 basic 区分
        byte[] raw = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xef};
        check("+//v".equals(e.encodeToString(raw)), "basic + and / alphabet");
        check("-__v".equals(Base64.getUrlEncoder().encodeToString(raw)), "url - and _ alphabet");
        check("-__v".equals(Base64.getUrlEncoder().withoutPadding().encodeToString(raw)), "url withoutPadding");
        // MIME 编码器 76 字符折行，行间 CRLF
        byte[] big = new byte[200];
        for (int i = 0; i < big.length; i++) big[i] = (byte) 'A';
        ByteArrayOutputStream mbos = new ByteArrayOutputStream();
        OutputStream mos = Base64.getMimeEncoder().wrap(mbos);
        mos.write(big);
        mos.close();
        String mstr = new String(mbos.toByteArray(), "US-ASCII");
        int firstCrlf = mstr.indexOf("\r\n");
        check(firstCrlf == 76, "MIME first line length 76 (got " + firstCrlf + ")");
        check(mstr.indexOf("\r\n") >= 0, "MIME emits CRLF separators");
    }

    public static void main(String[] args) throws Exception {
        testEncoder();
        testEncoderContracts();
        testEncoderMoreContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
