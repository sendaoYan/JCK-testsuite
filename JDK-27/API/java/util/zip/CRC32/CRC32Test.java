/*
 * @test
 * @summary java.util.zip.CRC32 contract: known vectors, update overloads (byte[], int,
 *          ByteBuffer), offset/length, reset, null->NPE, bad bounds->AIOOBE, value
 *          accumulation across multiple updates.
 * @run main CRC32Test
 */

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class CRC32Test {
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

    static void testKnownVectors() throws Exception {
        CRC32 c = new CRC32();
        check(c.getValue() == 0L, "initial value 0");
        c.update("123456789".getBytes("UTF-8"));
        check(c.getValue() == 0xCBF43926L, "crc32 of '123456789'");
        c.reset();
        c.update("The quick brown fox jumps over the lazy dog".getBytes("UTF-8"));
        check(c.getValue() == 0x414FA339L, "crc32 of pangram");
        CRC32 empty = new CRC32();
        empty.update(new byte[0]);
        check(empty.getValue() == 0L, "crc32 of empty");
    }

    static void testUpdateOverloads() throws Exception {
        byte[] data = "123456789".getBytes("UTF-8");
        CRC32 a = new CRC32();
        a.update(data, 0, data.length);
        check(a.getValue() == 0xCBF43926L, "update(byte[],off,len)");

        CRC32 b = new CRC32();
        for (byte x : data) b.update(x);
        check(b.getValue() == 0xCBF43926L, "update(int) byte-wise");

        CRC32 c = new CRC32();
        c.update(ByteBuffer.wrap(data));
        check(c.getValue() == 0xCBF43926L, "update(ByteBuffer)");

        byte[] padded = new byte[11];
        padded[0] = (byte) 'x';
        System.arraycopy(data, 0, padded, 1, data.length);
        padded[10] = (byte) 'y';
        ByteBuffer positioned = ByteBuffer.wrap(padded);
        positioned.position(1).limit(10);
        CRC32 d = new CRC32();
        d.update(positioned);
        check(d.getValue() == 0xCBF43926L, "update(ByteBuffer) respects position/limit");
    }

    static void testPartialAndAccumulation() throws Exception {
        byte[] data = "123456789".getBytes("UTF-8");
        CRC32 c = new CRC32();
        c.update(data, 0, 4);
        c.update(data, 4, 5);
        check(c.getValue() == 0xCBF43926L, "chained partial updates equal full");
        c.update(0);
        check(c.getValue() != 0xCBF43926L, "value changes after further update");
    }

    static void testResetAndIdempotence() {
        CRC32 c = new CRC32();
        c.update(42);
        long v = c.getValue();
        c.reset();
        check(c.getValue() == 0L, "reset to 0");
        c.update(42);
        check(c.getValue() == v, "same update after reset yields same value");
    }

    static void testNullAndBounds() {
        CRC32 c = new CRC32();
        checkThrows(NullPointerException.class, () -> c.update((byte[]) null, 0, 1), "update null byte[] NPE");
        checkThrows(NullPointerException.class, () -> c.update((ByteBuffer) null), "update null ByteBuffer NPE");
        byte[] data = new byte[5];
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.update(data, -1, 1), "negative offset AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.update(data, 0, 6), "len past end AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.update(data, 4, 2), "off+len past end AIOOBE");
    }

    public static void main(String[] args) throws Exception {
        testKnownVectors();
        testUpdateOverloads();
        testPartialAndAccumulation();
        testResetAndIdempotence();
        testNullAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
