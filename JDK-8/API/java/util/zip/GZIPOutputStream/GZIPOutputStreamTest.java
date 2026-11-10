/*
 * @test
 * @summary java.util.zip.GZIPOutputStream contract: round-trip, magic header,
 *          trailer CRC32/ISIZE verification, size<=0 IAE, null/bounds/closed
 *          exceptions, write() single byte, finish without close.
 * @run main GZIPOutputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testRoundTrip() throws Exception {
        byte[] data = "gzip output round trip".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write(data);
        gz.close();
        byte[] compressed = bout.toByteArray();

        check(compressed[0] == (byte) 0x1f && compressed[1] == (byte) 0x8b, "magic header 1f 8b");

        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[16];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        check(Arrays.equals(data, out.toByteArray()), "content restored");
        in.close();
    }

    static void testTrailer() throws Exception {
        byte[] data = "data".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write(data);
        gz.close();
        byte[] gzBytes = bout.toByteArray();
        int trailer = gzBytes.length - 8;
        // gzip trailer fields are little-endian
        long crc = (gzBytes[trailer] & 0xffL)
                 | ((long) (gzBytes[trailer + 1] & 0xff)) << 8
                 | ((long) (gzBytes[trailer + 2] & 0xff)) << 16
                 | ((long) (gzBytes[trailer + 3] & 0xff)) << 24;
        long isize = (gzBytes[trailer + 4] & 0xffL)
                   | ((long) (gzBytes[trailer + 5] & 0xff)) << 8
                   | ((long) (gzBytes[trailer + 6] & 0xff)) << 16
                   | ((long) (gzBytes[trailer + 7] & 0xff)) << 24;
        CRC32 c = new CRC32();
        c.update(data, 0, data.length);
        check(crc == c.getValue(), "trailer CRC32");
        check(isize == data.length, "trailer ISIZE");
    }

    static void testSingleByteWrite() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write('a');
        gz.write('b');
        gz.close();
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        check(in.read() == 'a' && in.read() == 'b', "single-byte write round-trip");
        in.close();
    }

    static void testFinishWithoutClose() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write("finish".getBytes(StandardCharsets.UTF_8));
        gz.finish();
        // underlying stream not closed: still usable
        check(bout.size() > 0, "finish flushes compressed data");
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        byte[] buf = new byte[16];
        int n = in.read(buf);
        check("finish".equals(new String(buf, 0, n, StandardCharsets.UTF_8)), "finish round-trip");
        in.close();
        gz.close();
    }

    static void testCtorValidation() {
        checkThrows(NullPointerException.class, () -> new GZIPOutputStream(null), "null ctor NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new GZIPOutputStream(new ByteArrayOutputStream(), 0),
                "size 0 IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new GZIPOutputStream(new ByteArrayOutputStream(), -1),
                "negative size IAE");
    }

    static void testNullAndBounds() throws Exception {
        GZIPOutputStream gz = new GZIPOutputStream(new ByteArrayOutputStream());
        checkThrows(NullPointerException.class, () -> gz.write((byte[]) null, 0, 0), "write null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> gz.write(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> gz.write(new byte[5], 0, 6), "len past end IOOBE");
        gz.close();
    }

    static void testClose() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.close();
        gz.close(); // idempotent
        check(bout.size() > 0, "close writes trailer");
    }

    // ---- 契约深化（第二层）：finish 后写 -> IOException、关流后写 -> IOException、空/大块 round-trip、合法 size ----
    static void testFinishThenWrite() throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write(new byte[]{1});
        gz.finish();
        checkThrows(IOException.class, () -> gz.write(2), "write after finish IOException");
        checkThrows(IOException.class, () -> gz.write(new byte[]{3}, 0, 1), "write(byte[],off,len) after finish IOException");
        gz.close();
    }

    static void testWriteAfterClose() throws Exception {
        GZIPOutputStream gz = new GZIPOutputStream(new ByteArrayOutputStream());
        gz.close();
        checkThrows(IOException.class, () -> gz.write(1), "write after close IOException");
    }

    static void testEmptyAndLarge() throws Exception {
        // 空数据 round-trip
        ByteArrayOutputStream ebout = new ByteArrayOutputStream();
        GZIPOutputStream egz = new GZIPOutputStream(ebout);
        egz.close();
        byte[] egzBytes = ebout.toByteArray();
        check(egzBytes[0] == (byte) 0x1f && egzBytes[1] == (byte) 0x8b, "empty gzip still has magic");
        GZIPInputStream ein = new GZIPInputStream(new ByteArrayInputStream(egzBytes));
        check(ein.read() == -1, "empty gzip round-trip read() -1");
        ein.close();

        // 大块数据 round-trip
        byte[] big = new byte[100000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) (i & 0xff);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write(big);
        gz.close();
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        check(Arrays.equals(big, out.toByteArray()), "large block round-trip");
        in.close();
    }

    static void testValidSizeCtor() throws Exception {
        byte[] data = "sized".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout, 512);
        gz.write(data);
        gz.close();
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(bout.toByteArray()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        check(Arrays.equals(data, out.toByteArray()), "valid size ctor round-trip");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testTrailer();
        testSingleByteWrite();
        testFinishWithoutClose();
        testCtorValidation();
        testNullAndBounds();
        testClose();
        testFinishThenWrite();
        testWriteAfterClose();
        testEmptyAndLarge();
        testValidSizeCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
