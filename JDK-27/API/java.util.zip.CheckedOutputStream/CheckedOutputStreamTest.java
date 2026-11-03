/*
 * @test
 * @summary java.util.zip.CheckedOutputStream conformance: white-box structure + black-box checksum-verified write.
 * @run main CheckedOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;

public class CheckedOutputStreamTest {
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
        Class<?> c = clazz("java.util.zip.CheckedOutputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterOutputStream.class, "extends FilterOutputStream");
        c.getConstructor(Class.forName("java.io.OutputStream"), Checksum.class);
        c.getMethod("write", int.class);
        c.getMethod("write", byte[].class, int.class, int.class);
        c.getMethod("getChecksum");
        check(true, "constructor/write/getChecksum present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "The quick brown fox jumps over the lazy dog".getBytes("UTF-8");
        Adler32 direct = new Adler32();
        direct.update(data);

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Adler32 cs = new Adler32();
        CheckedOutputStream out = new CheckedOutputStream(bout, cs);
        out.write(data);
        out.flush();
        check(Arrays.equals(data, bout.toByteArray()), "write reached underlying stream");
        check(out.getChecksum() == cs, "getChecksum returns supplied checksum");
        check(cs.getValue() == direct.getValue(), "checksum equals directly-computed Adler32");
        out.close();

        // single-byte write path
        ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
        Adler32 cs2 = new Adler32();
        CheckedOutputStream out2 = new CheckedOutputStream(bout2, cs2);
        for (byte b : data) out2.write(b);
        out2.flush();
        check(Arrays.equals(data, bout2.toByteArray()), "write(int) reached underlying stream");
        check(cs2.getValue() == direct.getValue(), "write(int) checksum matches");
        out2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
