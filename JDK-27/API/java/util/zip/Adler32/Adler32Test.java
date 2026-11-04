/*
 * @test
 * @summary java.util.zip.Adler32 contract: known vectors, update overloads (byte[], int,
 *          ByteBuffer), offset/length, reset to 1, null->NPE, bad bounds->AIOOBE,
 *          accumulation across updates.
 * @run main Adler32Test
 */

import java.nio.ByteBuffer;
import java.util.zip.Adler32;

public class Adler32Test {
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
        Adler32 a = new Adler32();
        check(a.getValue() == 1L, "initial value 1");
        a.update("Wikipedia".getBytes("UTF-8"));
        check(a.getValue() == 0x11E60398L, "adler32 of 'Wikipedia'");
        a.reset();
        a.update("123456789".getBytes("UTF-8"));
        check(a.getValue() == 0x091E01DEL, "adler32 of '123456789'");
        Adler32 empty = new Adler32();
        empty.update(new byte[0]);
        check(empty.getValue() == 1L, "adler32 of empty is 1");
    }

    static void testUpdateOverloads() throws Exception {
        byte[] data = "Wikipedia".getBytes("UTF-8");
        Adler32 a = new Adler32();
        a.update(data, 0, data.length);
        check(a.getValue() == 0x11E60398L, "update(byte[],off,len)");

        Adler32 b = new Adler32();
        for (byte x : data) b.update(x);
        check(b.getValue() == 0x11E60398L, "update(int) byte-wise");

        Adler32 c = new Adler32();
        c.update(ByteBuffer.wrap(data));
        check(c.getValue() == 0x11E60398L, "update(ByteBuffer)");
    }

    static void testPartialAndAccumulation() throws Exception {
        byte[] data = "Wikipedia".getBytes("UTF-8");
        Adler32 a = new Adler32();
        a.update(data, 0, 4);
        a.update(data, 4, data.length - 4);
        check(a.getValue() == 0x11E60398L, "chained partial updates equal full");
    }

    static void testReset() {
        Adler32 a = new Adler32();
        a.update(42);
        long v = a.getValue();
        a.reset();
        check(a.getValue() == 1L, "reset to 1");
        a.update(42);
        check(a.getValue() == v, "same update after reset yields same value");
    }

    static void testNullAndBounds() {
        Adler32 a = new Adler32();
        checkThrows(NullPointerException.class, () -> a.update((byte[]) null, 0, 1), "update null byte[] NPE");
        checkThrows(NullPointerException.class, () -> a.update((ByteBuffer) null), "update null ByteBuffer NPE");
        byte[] data = new byte[5];
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> a.update(data, -1, 1), "negative offset AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> a.update(data, 0, 6), "len past end AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> a.update(data, 4, 2), "off+len past end AIOOBE");
    }

    public static void main(String[] args) throws Exception {
        testKnownVectors();
        testUpdateOverloads();
        testPartialAndAccumulation();
        testReset();
        testNullAndBounds();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
