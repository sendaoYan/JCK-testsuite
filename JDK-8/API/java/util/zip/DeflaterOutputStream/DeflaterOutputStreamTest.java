/*
 * @test
 * @summary java.util.zip.DeflaterOutputStream conformance: compressing write.
 * @run main DeflaterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.util.zip.*;
import java.util.zip.DeflaterOutputStream;

public class DeflaterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeflaterOutputStream() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DeflaterOutputStream out = new DeflaterOutputStream(bout);
        out.write("stream data stream data".getBytes("UTF-8"));
        out.finish();
        out.close();
        check(bout.size() > 0, "compressed output produced");
    }


    static byte[] compress(byte[] raw) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d = new DeflaterOutputStream(bos);
        d.write(raw);
        d.close();
        return bos.toByteArray();
    }

    static byte[] decompress(byte[] zipped) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        InflaterInputStream in = new InflaterInputStream(new java.io.ByteArrayInputStream(zipped));
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
        in.close();
        return bos.toByteArray();
    }

    /** 记录 close 调用的底层流。 */
    static class RecordingStream extends java.io.ByteArrayInputStream {
        int closes;
        RecordingStream(byte[] data) { super(data); }
        public void close() throws java.io.IOException { closes++; super.close(); }
    }

    // ===================== 契约深化：写通道矩阵/finish vs close/syncFlush/异常 =====================
    static void testContracts() throws Exception {
        byte[] raw = "payload payload payload".getBytes("UTF-8");
        // 写通道三重载等价
        java.io.ByteArrayOutputStream b1 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d1 = new DeflaterOutputStream(b1);
        d1.write(raw, 0, raw.length);
        d1.close();
        java.io.ByteArrayOutputStream b2 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d2 = new DeflaterOutputStream(b2);
        d2.write(raw);
        d2.close();
        java.io.ByteArrayOutputStream b3 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d3 = new DeflaterOutputStream(b3);
        for (byte x : raw) d3.write(x);
        d3.close();
        check(java.util.Arrays.equals(b1.toByteArray(), b2.toByteArray()), "write(byte[]) == write(b,off,len)");
        check(java.util.Arrays.equals(b1.toByteArray(), b3.toByteArray()), "write(int) accumulation == bulk");
        check(java.util.Arrays.equals(decompress(b1.toByteArray()), raw), "round-trip restored");
        // finish 幂等；finish 后 write -> IOException("write beyond end of stream")
        java.io.ByteArrayOutputStream b4 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d4 = new DeflaterOutputStream(b4);
        d4.write(raw);
        d4.finish();
        d4.finish();
        check(b4.size() > 0, "finish flushed compressed bytes");
        try {
            d4.write(1);
            check(false, "write after finish throws");
        } catch (java.io.IOException e) {
            check("write beyond end of stream".equals(e.getMessage()), "write-after-finish message");
        }
        d4.close();
        d4.close();  // close 幂等
        // close 隐式 finish
        java.io.ByteArrayOutputStream b5 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d5 = new DeflaterOutputStream(b5);
        d5.write(raw);
        d5.close();
        check(java.util.Arrays.equals(b5.toByteArray(), b1.toByteArray()), "close() implies finish()");
        // write 后 close 前 flush
        java.io.ByteArrayOutputStream b6 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream syncFlush = new DeflaterOutputStream(b6, true);
        syncFlush.write(raw);
        syncFlush.flush();
        check(b6.size() > 0, "syncFlush=true flush pushes compressed bytes");
        syncFlush.close();
        check(java.util.Arrays.equals(decompress(b6.toByteArray()), raw), "syncFlush stream round-trip");
        // 非同步 flush 不保证立即下推——只断言 flush 不抛
        java.io.ByteArrayOutputStream b7 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d7 = new DeflaterOutputStream(b7);
        d7.write(raw);
        d7.flush();
        d7.close();
        check(true, "default flush tolerated");
        // 关闭后 write -> IOException("Stream closed")
        java.io.ByteArrayOutputStream b8 = new java.io.ByteArrayOutputStream();
        DeflaterOutputStream d8 = new DeflaterOutputStream(b8);
        d8.close();
        try {
            d8.write(1);
            check(false, "write after close throws");
        } catch (java.io.IOException e) {
            check("write beyond end of stream".equals(e.getMessage()),
                  "write-after-close (close 已隐式 finish) message: " + e.getMessage());
        }
        // 构造负例
        try {
            new DeflaterOutputStream(null);
            check(false, "null out NPE");
        } catch (NullPointerException e) { check(true, "null out NPE"); }
        try {
            new DeflaterOutputStream(new java.io.ByteArrayOutputStream(), new Deflater(), 0);
            check(false, "bad size IAE");
        } catch (IllegalArgumentException e) { check(true, "size <= 0 IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testDeflaterOutputStream();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
