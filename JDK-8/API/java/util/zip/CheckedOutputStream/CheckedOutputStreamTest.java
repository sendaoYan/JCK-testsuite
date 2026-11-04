/*
 * @test
 * @summary java.util.zip.CheckedOutputStream contract: getChecksum identity,
 *          write(int)/write(byte[],off,len) update checksum, known CRC32 vector,
 *          null/bounds exceptions, underlying stream receives data.
 * @run main CheckedOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CheckedOutputStream;
import java.util.zip.CRC32;

public class CheckedOutputStreamTest {
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

    static void testIdentityAndWrite() throws Exception {
        CRC32 crc = new CRC32();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        CheckedOutputStream out = new CheckedOutputStream(bout, crc);
        check(out.getChecksum() == crc, "getChecksum returns same instance");

        byte[] data = "123456789".getBytes(StandardCharsets.UTF_8);
        out.write(data, 0, 4);
        out.write(data, 4, 4);
        out.write('9');
        out.flush();
        check(Arrays.equals(data, bout.toByteArray()), "underlying stream receives data");
        check(crc.getValue() == 0xCBF43926L, "CRC32 of '123456789'");
        out.close();
    }

    static void testNullAndBounds() throws Exception {
        CheckedOutputStream out = new CheckedOutputStream(new ByteArrayOutputStream(), new CRC32());
        checkThrows(NullPointerException.class, () -> out.write((byte[]) null, 0, 1), "write null NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[5], -1, 1), "negative off IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[5], 0, 6), "len past end IOOBE");
    }

    public static void main(String[] args) throws Exception {
        testIdentityAndWrite();
        testNullAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
