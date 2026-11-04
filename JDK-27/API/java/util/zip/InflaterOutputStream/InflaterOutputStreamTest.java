/*
 * @test
 * @summary java.util.zip.InflaterOutputStream conformance: white-box structure + black-box decompressing write.
 * @run main InflaterOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
