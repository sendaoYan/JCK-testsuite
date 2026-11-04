/*
 * @test
 * @summary java.util.zip.DeflaterOutputStream conformance: white-box structure + black-box compressing write.
 * @run main DeflaterOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class DeflaterOutputStreamTest {
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
        Class<?> c = clazz("java.util.zip.DeflaterOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterOutputStream.class, "extends FilterOutputStream");
        c.getConstructor(Class.forName("java.io.OutputStream"));
        c.getConstructor(Class.forName("java.io.OutputStream"), Deflater.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), Deflater.class, int.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), Deflater.class, boolean.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), boolean.class);
        c.getMethod("write", int.class);
        c.getMethod("write", byte[].class, int.class, int.class);
        c.getMethod("finish");
        c.getMethod("close");
        c.getMethod("flush");
        check(true, "constructors and stream methods present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "stream data stream data".getBytes("UTF-8");

        ByteArrayOutputStream comp = new ByteArrayOutputStream();
        DeflaterOutputStream out = new DeflaterOutputStream(comp);
        out.write(data);
        out.finish();
        out.close();
        check(comp.size() > 0, "compressed output produced");

        InflaterInputStream iin = new InflaterInputStream(new ByteArrayInputStream(comp.toByteArray()));
        ByteArrayOutputStream decomp = new ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = iin.read(buf)) != -1) decomp.write(buf, 0, n);
        check(Arrays.equals(data, decomp.toByteArray()), "round-trip restores content");
        iin.close();

        // write(int) single-byte path with a custom Deflater and buffer size
        ByteArrayOutputStream comp2 = new ByteArrayOutputStream();
        DeflaterOutputStream out2 = new DeflaterOutputStream(comp2, new Deflater(), 64);
        for (byte b : data) out2.write(b);
        out2.finish();
        out2.close();
        InflaterInputStream iin2 = new InflaterInputStream(new ByteArrayInputStream(comp2.toByteArray()));
        check(Arrays.equals(data, iin2.readAllBytes()), "write(int) round-trip restores content");
        iin2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
