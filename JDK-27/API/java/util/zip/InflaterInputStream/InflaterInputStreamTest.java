/*
 * @test
 * @summary java.util.zip.InflaterInputStream conformance: white-box structure + black-box decompressing read.
 * @run main InflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
