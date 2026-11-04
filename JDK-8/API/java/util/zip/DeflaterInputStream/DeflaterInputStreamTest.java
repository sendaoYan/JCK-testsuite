/*
 * @test
 * @summary java.util.zip.DeflaterInputStream conformance: compressing read.
 * @run main DeflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.*;
import java.util.zip.*;
import java.util.zip.DeflaterInputStream;

public class DeflaterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDeflaterInputStream() throws Exception {
        byte[] data = "stream data stream data stream data".getBytes("UTF-8");
        DeflaterInputStream in = new DeflaterInputStream(new ByteArrayInputStream(data));
        byte[] buf = new byte[256];
        int n = in.read(buf);
        check(n > 0, "read returns compressed data");
        in.close();
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

    // ===================== 契约深化：round-trip/read 三重载/skip/mark/close 语义 =====================
    static void testContracts() throws Exception {
        byte[] raw = "deflate me please, deflate me please!".getBytes("UTF-8");
        // round-trip：DIS 输出即 zlib 流，可被 InflaterInputStream 解回
        java.io.ByteArrayOutputStream zipped = new java.io.ByteArrayOutputStream();
        DeflaterInputStream dis = new DeflaterInputStream(new java.io.ByteArrayInputStream(raw));
        byte[] buf = new byte[7];   // 小缓冲强制多次 read
        int n;
        while ((n = dis.read(buf, 0, buf.length)) > 0) zipped.write(buf, 0, n);
        check(java.util.Arrays.equals(decompress(zipped.toByteArray()), raw), "small-buffer read round-trip");
        dis.close();
        // read() 单字节路径
        DeflaterInputStream one = new DeflaterInputStream(new java.io.ByteArrayInputStream(raw));
        int first = one.read();
        check(first >= 0 && first <= 255, "single read() returns unsigned byte");
        one.close();
        // 完整排干后再 read -> -1
        DeflaterInputStream drain = new DeflaterInputStream(new java.io.ByteArrayInputStream(raw));
        long skipped = 0;
        while (drain.skip(16) > 0) skipped += 16;
        check(skipped > 0, "skip advances through compressed stream");
        check(drain.read(new byte[4]) == -1, "read -1 at end of stream");
        drain.close();
        // skip 负数 -> IllegalArgumentException
        DeflaterInputStream sk = new DeflaterInputStream(new java.io.ByteArrayInputStream(raw));
        try {
            sk.skip(-1);
            check(false, "skip(-1) throws");
        } catch (IllegalArgumentException e) { check(true, "skip(-1) IllegalArgumentException"); }
        check(sk.skip(0) == 0, "skip(0) is 0");
        sk.close();
        // available：流未排干时 >= 0
        DeflaterInputStream av = new DeflaterInputStream(new java.io.ByteArrayInputStream(raw));
        check(av.available() >= 0, "available non-negative");
        av.close();
        // mark/reset 不支持
        check(!av.markSupported(), "markSupported false");
        try {
            av.reset();
            check(false, "reset throws IOException");
        } catch (java.io.IOException e) { check(true, "reset IOException"); }
        // close 幂等 + 关底层 + 关后 read -> IOException("Stream closed")
        RecordingStream rec = new RecordingStream(raw);
        DeflaterInputStream cl = new DeflaterInputStream(rec);
        cl.close();
        cl.close();
        check(rec.closes == 1, "close exactly once on underlying");
        try {
            cl.read();
            check(false, "read after close throws");
        } catch (java.io.IOException e) {
            check("Stream closed".equals(e.getMessage()), "read-after-close message: " + e.getMessage());
        }
        // 构造负例
        try {
            new DeflaterInputStream(null);
            check(false, "null in NPE");
        } catch (NullPointerException e) { check(true, "null in NPE"); }
        try {
            new DeflaterInputStream(new java.io.ByteArrayInputStream(raw), new Deflater(), 0);
            check(false, "bad buffer size IAE");
        } catch (IllegalArgumentException e) { check(true, "buffer size <= 0 IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testDeflaterInputStream();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
