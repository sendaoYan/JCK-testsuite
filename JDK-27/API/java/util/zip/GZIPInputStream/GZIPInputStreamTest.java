/*
 * @test
 * @summary java.util.zip.GZIPInputStream conformance: white-box structure + black-box gzip decompress.
 * @run main GZIPInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class GZIPInputStreamTest {
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
        Class<?> c = clazz("java.util.zip.GZIPInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == InflaterInputStream.class, "extends InflaterInputStream");
        check(GZIPInputStream.GZIP_MAGIC == 0x8b1f, "GZIP_MAGIC == 0x8b1f");
        c.getConstructor(Class.forName("java.io.InputStream"));
        c.getConstructor(Class.forName("java.io.InputStream"), int.class);
        c.getMethod("read", byte[].class, int.class, int.class);
        c.getMethod("close");
        check(true, "constructors and read/close present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "gzip data gzip data gzip data".getBytes("UTF-8");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        gzout.write(data);
        gzout.close();

        GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] result = gzin.readAllBytes();
        check(Arrays.equals(data, result), "multi-byte payload round-trip");
        gzin.close();

        // partial read via read(byte[],off,len) then drain
        GZIPInputStream gzin2 = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] head = new byte[4];
        int h = gzin2.read(head, 0, head.length);
        check(h == 4 && Arrays.equals(head, Arrays.copyOf(data, 4)), "partial read returns prefix");
        byte[] tail = new byte[data.length];
        int t = gzin2.read(tail);
        check(t == data.length - 4, "remaining bytes after partial read");
        gzin2.close();

        // constructor rejects non-gzip bytes
        expectThrows(ZipException.class,
                () -> new GZIPInputStream(new ByteArrayInputStream("not gzip data".getBytes("UTF-8"))));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
