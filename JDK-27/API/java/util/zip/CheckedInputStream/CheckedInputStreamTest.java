/*
 * @test
 * @summary java.util.zip.CheckedInputStream conformance: white-box structure + black-box checksum-verified read.
 * @run main CheckedInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class CheckedInputStreamTest {
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
        Class<?> c = clazz("java.util.zip.CheckedInputStream");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == FilterInputStream.class, "extends FilterInputStream");
        c.getConstructor(Class.forName("java.io.InputStream"), Checksum.class);
        c.getMethod("read");
        c.getMethod("read", byte[].class, int.class, int.class);
        c.getMethod("skip", long.class);
        c.getMethod("getChecksum");
        check(true, "constructor/read/skip/getChecksum present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "The quick brown fox jumps over the lazy dog".getBytes("UTF-8");
        CRC32 direct = new CRC32();
        direct.update(data);

        // multi-byte read loop, checksum equals directly-computed value
        CRC32 cs = new CRC32();
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(data), cs);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        check(Arrays.equals(data, out.toByteArray()), "all bytes read through");
        check(in.getChecksum() == cs, "getChecksum returns the supplied checksum");
        check(cs.getValue() == direct.getValue(), "checksum equals directly-computed CRC32");
        in.close();

        // single-byte read path
        CRC32 cs2 = new CRC32();
        CheckedInputStream in2 = new CheckedInputStream(new ByteArrayInputStream(data), cs2);
        int total = 0;
        while (in2.read() != -1) total++;
        check(total == data.length, "read() single bytes count");
        check(cs2.getValue() == direct.getValue(), "single-byte read checksum matches");
        in2.close();

        // skip path: read 1, skip 4, read the remainder
        CheckedInputStream in3 = new CheckedInputStream(new ByteArrayInputStream(data), new CRC32());
        check(in3.read() != -1, "read first byte");
        long skipped = in3.skip(4);
        check(skipped == 4, "skip 4 bytes");
        byte[] rest = new byte[data.length];
        int m, totalRest = 0;
        while ((m = in3.read(rest)) != -1) totalRest += m;
        check(totalRest == data.length - 5, "remaining bytes after read+skip");
        in3.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
