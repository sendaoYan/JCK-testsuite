/*
 * @test
 * @summary java.util.zip.DeflaterInputStream conformance: white-box structure + black-box compressing read.
 * @run main DeflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.*;
import java.util.zip.*;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

public class DeflaterInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static <T extends Throwable> T expectThrows(Class<T> type, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (type.isInstance(t)) return type.cast(t);
            throw new AssertionError("Expected " + type.getName() + " but got " + t, t);
        }
        throw new AssertionError("Expected " + type.getName() + " but none thrown");
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.DeflaterInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterInputStream.class, "extends FilterInputStream");
        c.getConstructor(Class.forName("java.io.InputStream"));
        c.getConstructor(Class.forName("java.io.InputStream"), Deflater.class);
        c.getConstructor(Class.forName("java.io.InputStream"), Deflater.class, int.class);
        c.getMethod("read");
        c.getMethod("read", byte[].class, int.class, int.class);
        c.getMethod("skip", long.class);
        c.getMethod("available");
        c.getMethod("markSupported");
        c.getMethod("mark", int.class);
        c.getMethod("reset");
        c.getMethod("close");
        check(true, "constructors and stream methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "stream data stream data stream data".getBytes("UTF-8");

        DeflaterInputStream in = new DeflaterInputStream(new ByteArrayInputStream(data));
        ByteArrayOutputStream comp = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) comp.write(buf, 0, n);
        check(comp.size() > 0, "compressed output produced");
        check(!in.markSupported(), "markSupported false");
        in.close();

        // mark/reset is unsupported and throws IOException
        DeflaterInputStream in2 = new DeflaterInputStream(new ByteArrayInputStream(data));
        in2.mark(16);
        expectThrows(IOException.class, in2::reset);
        in2.close();

        // round-trip through InflaterInputStream
        InflaterInputStream iin = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        ByteArrayOutputStream decomp = new ByteArrayOutputStream();
        byte[] b2 = new byte[64];
        int m;
        while ((m = iin.read(b2)) != -1) decomp.write(b2, 0, m);
        check(Arrays.equals(data, decomp.toByteArray()), "round-trip restores content");
        iin.close();
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
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
