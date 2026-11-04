/*
 * @test
 * @summary java.util.zip.InflaterInputStream conformance: white-box structure + black-box decompressing read.
 * @run main InflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.*;
import java.util.zip.*;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class InflaterInputStreamTest {
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
        Class<?> c = clazz("java.util.zip.InflaterInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterInputStream.class, "extends FilterInputStream");
        c.getConstructor(Class.forName("java.io.InputStream"));
        c.getConstructor(Class.forName("java.io.InputStream"), Inflater.class);
        c.getConstructor(Class.forName("java.io.InputStream"), Inflater.class, int.class);
        c.getMethod("read");
        c.getMethod("read", byte[].class, int.class, int.class);
        c.getMethod("available");
        c.getMethod("skip", long.class);
        c.getMethod("close");
        c.getMethod("markSupported");
        c.getMethod("mark", int.class);
        c.getMethod("reset");
        check(true, "constructors and stream methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "stream data stream data".getBytes("UTF-8");
        ByteArrayOutputStream comp = new ByteArrayOutputStream();
        DeflaterOutputStream dout = new DeflaterOutputStream(comp);
        dout.write(data);
        dout.finish();
        dout.close();

        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        byte[] result = new byte[256];
        int n = in.read(result);
        check(n == data.length, "read returns decompressed length");
        check(Arrays.equals(data, Arrays.copyOf(result, n)), "content restored");
        in.close();

        // mark/reset unsupported
        InflaterInputStream in2 = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        check(!in2.markSupported(), "markSupported false");
        in2.mark(16);
        expectThrows(IOException.class, in2::reset);
        in2.close();

        // single-byte read path
        InflaterInputStream in3 = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        int total = 0;
        while (in3.read() != -1) total++;
        check(total == data.length, "read() single bytes count");
        in3.close();
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

    // ===================== 契约深化：available 语义/skip/mark/close/损坏输入/构造负例 =====================
    static void testContracts() throws Exception {
        byte[] raw = "inflate me please!".getBytes("UTF-8");
        byte[] zipped = compress(raw);
        InflaterInputStream in = new InflaterInputStream(new java.io.ByteArrayInputStream(zipped));
        check(in.available() == 1, "available 1 before drained");
        byte[] restored = new byte[raw.length];
        int total = 0, n;
        while ((n = in.read(restored, total, restored.length - total)) > 0) total += n;
        check(total == raw.length && java.util.Arrays.equals(restored, raw), "offset read restores all");
        check(in.read(new byte[4]) == -1, "final read -1");
        check(in.available() == 0, "available 0 after finished");
        check(in.read(new byte[4]) == -1, "read -1 after end");
        in.close();
        // read() 单字节与 read(byte[]) 等价
        InflaterInputStream one = new InflaterInputStream(new java.io.ByteArrayInputStream(zipped));
        int first = one.read();
        check(first == (raw[0] & 0xff), "read() returns first raw byte (stored/deflated立即解出)");
        one.close();
        // skip 语义
        InflaterInputStream sk = new InflaterInputStream(new java.io.ByteArrayInputStream(zipped));
        long s1 = sk.skip(2);
        check(s1 == 2, "skip 2 decompressed bytes");
        check((byte) sk.read() == raw[2], "skip then read continues sequence");
        try {
            sk.skip(-1);
            check(false, "skip(-1) throws");
        } catch (IllegalArgumentException e) { check(true, "skip(-1) IllegalArgumentException"); }
        sk.close();
        // mark 不支持
        InflaterInputStream mr = new InflaterInputStream(new java.io.ByteArrayInputStream(zipped));
        check(!mr.markSupported(), "markSupported false");
        try {
            mr.reset();
            check(false, "reset throws");
        } catch (java.io.IOException e) { check(true, "reset IOException"); }
        mr.close();
        // close 幂等/关底层/关后 read
        RecordingStream rec = new RecordingStream(zipped);
        InflaterInputStream cl = new InflaterInputStream(rec);
        cl.close();
        cl.close();
        check(rec.closes == 1, "underlying closed exactly once");
        try {
            cl.read();
            check(false, "read after close throws");
        } catch (java.io.IOException e) {
            check("Stream closed".equals(e.getMessage()), "read-after-close message");
        }
        // 损坏输入 -> ZipException("incorrect header check")
        byte[] garbage = new byte[16];
        for (int i = 0; i < garbage.length; i++) garbage[i] = (byte) (i * 7 + 1);
        InflaterInputStream bad = new InflaterInputStream(new java.io.ByteArrayInputStream(garbage));
        try {
            bad.read(new byte[16]);
            check(false, "corrupt input throws ZipException");
        } catch (ZipException e) {
            check("incorrect header check".equals(e.getMessage()), "corrupt exact message: " + e.getMessage());
        }
        bad.close();
        // 构造负例
        try {
            new InflaterInputStream(null);
            check(false, "null in NPE");
        } catch (NullPointerException e) { check(true, "null in NPE"); }
        try {
            new InflaterInputStream(new java.io.ByteArrayInputStream(zipped), new Inflater(), 0);
            check(false, "bad size IAE");
        } catch (IllegalArgumentException e) { check(true, "size <= 0 IAE"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
