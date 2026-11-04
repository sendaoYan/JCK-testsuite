/*
 * @test
 * @summary java.util.zip.GZIPOutputStream conformance: white-box structure + black-box gzip write + header.
 * @run main GZIPOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamTest {
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
        Class<?> c = clazz("java.util.zip.GZIPOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == DeflaterOutputStream.class, "extends DeflaterOutputStream");
        c.getConstructor(Class.forName("java.io.OutputStream"));
        c.getConstructor(Class.forName("java.io.OutputStream"), int.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), boolean.class);
        c.getConstructor(Class.forName("java.io.OutputStream"), int.class, boolean.class);
        c.getMethod("write", byte[].class, int.class, int.class);
        c.getMethod("finish");
        check(true, "constructors and write/finish present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "gzip data gzip data gzip data".getBytes("UTF-8");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gzout = new GZIPOutputStream(bout);
        gzout.write(data);
        gzout.close();
        byte[] gz = bout.toByteArray();
        check(gz.length > 0, "gzip output produced");
        check((gz[0] & 0xff) == 0x1f && (gz[1] & 0xff) == 0x8b, "gzip magic header 1f 8b");

        GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(gz));
        check(Arrays.equals(data, gzin.readAllBytes()), "multi-byte payload round-trip");
        gzin.close();

        // level constructor still writes a valid gzip stream
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        GZIPOutputStream gz2 = new GZIPOutputStream(bout2, 5);
        gz2.write("x".getBytes("UTF-8"));
        gz2.finish();
        gz2.close();
        byte[] gz2b = bout2.toByteArray();
        check((gz2b[0] & 0xff) == 0x1f && (gz2b[1] & 0xff) == 0x8b, "level ctor writes gzip header");
        GZIPInputStream gzin2 = new GZIPInputStream(new ByteArrayInputStream(gz2b));
        check("x".equals(new String(gzin2.readAllBytes(), "UTF-8")), "level ctor round-trip");
        gzin2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
