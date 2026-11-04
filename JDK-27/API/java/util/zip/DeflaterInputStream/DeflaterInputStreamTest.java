/*
 * @test
 * @summary java.util.zip.DeflaterInputStream conformance: white-box structure + black-box compressing read.
 * @run main DeflaterInputStreamTest
 */

import java.io.ByteArrayInputStream;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
