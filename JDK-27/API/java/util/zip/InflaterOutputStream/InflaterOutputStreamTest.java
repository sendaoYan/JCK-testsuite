/*
 * @test
 * @summary java.util.zip.InflaterOutputStream conformance: white-box structure + black-box decompressing write.
 * @run main InflaterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.*;
import java.util.zip.*;
import java.io.FilterOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class InflaterOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.InflaterOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterOutputStream.class, "extends FilterOutputStream");
        c.getConstructor(Class.forName("java.io.OutputStream"));
        c.getConstructor(Class.forName("java.io.OutputStream"), Inflater.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), Inflater.class, int.class);
        c.getMethod("close");
        c.getMethod("flush");
        c.getMethod("finish");
        c.getMethod("write", int.class);
        c.getMethod("write", byte[].class, int.class, int.class);
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

        ByteArrayOutputStream decomp = new ByteArrayOutputStream();
        InflaterOutputStream iout = new InflaterOutputStream(decomp);
        iout.write(comp.toByteArray());
        iout.finish();
        iout.close();
        check(Arrays.equals(data, decomp.toByteArray()), "round-trip restores content");

        // write(int) single-byte input path
        ByteArrayOutputStream decomp2 = new ByteArrayOutputStream();
        InflaterOutputStream iout2 = new InflaterOutputStream(decomp2);
        for (byte b : comp.toByteArray()) iout2.write(b);
        iout2.finish();
        iout2.close();
        check(Arrays.equals(data, decomp2.toByteArray()), "write(int) round-trip restores content");
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

    // =====================契约深化：round-trip 细节/finish/close 状态机/损坏输入/构造负例 =====================
    static void testContracts() throws Exception {
        byte[] raw = "stream data stream data".getBytes("UTF-8");
        byte[] zipped = compress(raw);
        // 分块写 + finish
        java.io.ByteArrayOutputStream out1 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io1 = new InflaterOutputStream(out1);
        for (int i = 0; i < zipped.length; i += 5) {
            io1.write(zipped, i, Math.min(5, zipped.length - i));
        }
        io1.finish();
        io1.close();
        check(java.util.Arrays.equals(out1.toByteArray(), raw), "chunked write round-trip");
        // write(int) 逐字节等价
        java.io.ByteArrayOutputStream out2 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io2 = new InflaterOutputStream(out2);
        for (byte b : zipped) io2.write(b);
        io2.finish();
        io2.close();
        check(java.util.Arrays.equals(out2.toByteArray(), raw), "single-byte write round-trip");
        // finish 后 write -> IOException
        java.io.ByteArrayOutputStream out3 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io3 = new InflaterOutputStream(out3);
        io3.write(zipped);
        io3.finish();
        try {
            io3.write(1);
            check(false, "write after finish throws");
        } catch (Throwable e) {
            // 版本差异：JDK-8 finish() 即 end() inflater，再写抛 NPE("Inflater has been closed")；
            // JDK-27 抛 IOException("Inflater closed")
            check((e instanceof NullPointerException && "Inflater has been closed".equals(e.getMessage()))
                  || (e instanceof java.io.IOException && "Inflater closed".equals(e.getMessage())),
                  "write-after-finish: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        io3.close();
        io3.close();  // close 幂等
        // 关闭后 write -> IOException("Stream closed")
        java.io.ByteArrayOutputStream out4 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io4 = new InflaterOutputStream(out4);
        io4.close();
        try {
            io4.write(1);
            check(false, "write after close throws");
        } catch (java.io.IOException e) {
            check("Stream closed".equals(e.getMessage()), "write-after-close message: " + e.getMessage());
        }
        // flush 透传
        java.io.ByteArrayOutputStream out5 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io5 = new InflaterOutputStream(out5);
        io5.write(zipped, 0, 4);
        io5.flush();
        io5.close();
        check(true, "flush tolerated mid-stream");
        // 损坏输入 -> ZipException("incorrect header check")
        byte[] garbage = new byte[16];
        for (int i = 0; i < garbage.length; i++) garbage[i] = (byte) (i * 7 + 1);
        java.io.ByteArrayOutputStream out6 = new java.io.ByteArrayOutputStream();
        InflaterOutputStream io6 = new InflaterOutputStream(out6);
        try {
            io6.write(garbage);
            io6.finish();
            check(false, "corrupt input throws ZipException");
        } catch (ZipException e) {
            check("incorrect header check".equals(e.getMessage()), "corrupt exact message");
        }
        try { io6.close(); } catch (java.io.IOException expected) { }
        // 构造负例
        try {
            new InflaterOutputStream(null);
            check(false, "null out NPE");
        } catch (NullPointerException e) { check(true, "null out NPE"); }
        try {
            new InflaterOutputStream(new java.io.ByteArrayOutputStream(), null);
            check(false, "null inflater NPE");
        } catch (NullPointerException e) { check(true, "null inflater NPE"); }
        try {
            new InflaterOutputStream(new java.io.ByteArrayOutputStream(), new Inflater(), 0);
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
