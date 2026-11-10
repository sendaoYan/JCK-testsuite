/*
 * @test
 * @summary java.util.zip.GZIPInputStream contract: round-trip, magic validation
 *          (ZipException on non-gzip), multi-member concatenation, size<=0 IAE,
 *          null/bounds/closed exceptions, read() single-byte, len-0 read.
 * @run main GZIPInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

public class GZIPInputStreamTest {
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

    static byte[] gzip(byte[] data) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(bout);
        gz.write(data);
        gz.close();
        return bout.toByteArray();
    }

    static void testRoundTrip() throws Exception {
        byte[] data = "gzip round trip data gzip round trip".getBytes(StandardCharsets.UTF_8);
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip(data)));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8];
        int n;
        int read = 0;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
            read += n;
        }
        check(read == data.length, "read full decompressed length");
        check(Arrays.equals(data, out.toByteArray()), "content restored");
        check(in.read() == -1, "read() -1 at end");
        in.close();
    }

    static void testSingleByteRead() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip(data)));
        check(in.read() == 'a', "read() byte 1");
        check(in.read() == 'b', "read() byte 2");
        check(in.read() == 'c', "read() byte 3");
        check(in.read() == -1, "read() -1 after data");
        in.close();
    }

    static void testMagicValidation() {
        check(GZIPInputStream.GZIP_MAGIC == 0x8b1f, "GZIP_MAGIC constant");
        checkThrows(ZipException.class,
                () -> new GZIPInputStream(new ByteArrayInputStream(new byte[]{0, 0, 0, 0})),
                "non-gzip magic ZipException");
    }

    static void testMultiMember() throws Exception {
        byte[] a = "first".getBytes(StandardCharsets.UTF_8);
        byte[] b = "second".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream concat = new ByteArrayOutputStream();
        concat.write(gzip(a));
        concat.write(gzip(b));
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(concat.toByteArray()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        check(Arrays.equals("firstsecond".getBytes(StandardCharsets.UTF_8), out.toByteArray()),
                "concatenated members read");
        in.close();
    }

    static void testCtorValidation() {
        checkThrows(NullPointerException.class, () -> new GZIPInputStream(null), "null ctor NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new GZIPInputStream(new ByteArrayInputStream(new byte[0]), 0),
                "size 0 IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new GZIPInputStream(new ByteArrayInputStream(new byte[0]), -1),
                "negative size IAE");
    }

    static void testNullAndBounds() throws Exception {
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip("x".getBytes(StandardCharsets.UTF_8))));
        checkThrows(NullPointerException.class, () -> in.read((byte[]) null, 0, 1), "read null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[5], 0, 6), "len past end IOOBE");
        check(in.read(new byte[5], 0, 0) == 0, "len-0 read returns 0");
        in.close();
    }

    static void testClose() throws Exception {
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip("x".getBytes(StandardCharsets.UTF_8))));
        in.close();
        in.close(); // idempotent
        checkThrows(IOException.class, () -> in.read(new byte[8]), "read after close IOException");
    }

    // ---- 契约深化（第二层）：空/大块 round-trip、read(byte[]) 单参、合法 size 构造、read() 关流后 IOException ----
    static void testEmptyAndLarge() throws Exception {
        // 空数据：写出为空 gzip，读回 read()/read(byte[]) 均 -1
        byte[] empty = gzip(new byte[0]);
        GZIPInputStream ein = new GZIPInputStream(new ByteArrayInputStream(empty));
        check(ein.read() == -1, "empty gzip read() -1");
        check(ein.read(new byte[8]) == -1, "empty gzip read(byte[]) -1");
        ein.close();

        // 大块数据：固定字节序列 (i & 0xff)，round-trip 等值
        byte[] big = new byte[100000];
        for (int i = 0; i < big.length; i++) big[i] = (byte) (i & 0xff);
        GZIPInputStream bin = new GZIPInputStream(new ByteArrayInputStream(gzip(big)));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = bin.read(buf)) != -1) bout.write(buf, 0, n);
        check(Arrays.equals(big, bout.toByteArray()), "large block round-trip");
        bin.close();
    }

    static void testReadAndCtor() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        // read(byte[]) 单参
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(gzip(data)));
        byte[] r = new byte[10];
        int n = in.read(r);
        check(n == 3 && Arrays.equals(data, Arrays.copyOf(r, 3)), "read(byte[]) single-arg");
        in.close();

        // 合法 size 构造 -> 可正常读回
        GZIPInputStream sized = new GZIPInputStream(new ByteArrayInputStream(gzip(data)), 512);
        ByteArrayOutputStream so = new ByteArrayOutputStream();
        byte[] sb = new byte[4];
        int m;
        while ((m = sized.read(sb)) != -1) so.write(sb, 0, m);
        check(Arrays.equals(data, so.toByteArray()), "valid size ctor round-trip");
        sized.close();

        // 关流后 read() 抛 IOException（read(byte[]) 已在 testClose 覆盖）
        GZIPInputStream closed = new GZIPInputStream(new ByteArrayInputStream(gzip(data)));
        closed.close();
        checkThrows(IOException.class, () -> closed.read(), "read() after close IOException");
    }

    public static void main(String[] args) throws Exception {
        testRoundTrip();
        testSingleByteRead();
        testMagicValidation();
        testMultiMember();
        testCtorValidation();
        testNullAndBounds();
        testClose();
        testEmptyAndLarge();
        testReadAndCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
