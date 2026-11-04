/*
 * @test
 * @summary java.util.zip.CheckedInputStream contract: getChecksum identity,
 *          read()/read(byte[],off,len) update checksum, skip updates checksum,
 *          known CRC32 vector, null/bounds exceptions, len-0 read no update.
 * @run main CheckedInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

public class CheckedInputStreamTest {
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

    static void testIdentityAndRead() throws Exception {
        CRC32 crc = new CRC32();
        byte[] data = "123456789".getBytes(StandardCharsets.UTF_8);
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(data), crc);
        check(in.getChecksum() == crc, "getChecksum returns same instance");

        byte[] buf = new byte[4];
        int n = in.read(buf);
        check(n == 4, "read returns count");
        in.read(buf, 0, 4);
        check(in.read() == '9', "read() single byte returns value");
        check(in.read() == -1, "read() -1 at end");
        check(crc.getValue() == 0xCBF43926L, "CRC32 of '123456789'");
        in.close();
    }

    static void testSkip() throws Exception {
        CRC32 crc = new CRC32();
        byte[] data = "0123456789".getBytes(StandardCharsets.UTF_8);
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(data), crc);
        long skipped = in.skip(4);
        check(skipped == 4, "skip returns count");
        byte[] rest = new byte[16];
        int n = in.read(rest);
        check(n == 6, "remaining read count");
        check(crc.getValue() == crc32(data), "skip+read equals full checksum");
        in.close();
    }

    static long crc32(byte[] data) {
        CRC32 c = new CRC32();
        c.update(data, 0, data.length);
        return c.getValue();
    }

    static void testNullAndBounds() throws Exception {
        CRC32 crc = new CRC32();
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(new byte[0]), crc);
        checkThrows(NullPointerException.class, () -> in.read((byte[]) null, 0, 1), "read null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> in.read(new byte[5], 0, 6), "len past end IOOBE");
    }

    static void testLenZero() throws Exception {
        CRC32 crc = new CRC32();
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        CheckedInputStream in = new CheckedInputStream(new ByteArrayInputStream(data), crc);
        check(in.read(new byte[8], 0, 0) == 0, "len-0 read returns 0");
        check(crc.getValue() == 0, "len-0 read does not update checksum");
        in.close();
    }

    public static void main(String[] args) throws Exception {
        testIdentityAndRead();
        testSkip();
        testNullAndBounds();
        testLenZero();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
