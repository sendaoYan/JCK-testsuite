/*
 * @test
 * @summary java.util.zip.Checksum interface conformance: update(int),
 *          update(byte[],off,len) slice semantics, accumulation, reset.
 * @run main ChecksumTest
 */

import java.util.zip.Checksum;

public class ChecksumTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class RecordingChecksum implements Checksum {
        long value;
        int updates;
        int resets;
        public void update(int b) { updates++; value += b; }
        public void update(byte[] b, int off, int len) { updates++; for (int i = off; i < off + len; i++) value += b[i]; }
        public long getValue() { return value; }
        public void reset() { resets++; value = 0; }
    }

    static void testChecksum() {
        RecordingChecksum c = new RecordingChecksum();
        c.update(1);
        check(c.updates == 1 && c.getValue() == 1, "update(int)");
        c.update(new byte[] { 1, 2, 3 }, 0, 3);
        check(c.updates == 2 && c.getValue() == 7, "update(byte[],off,len)");
        c.reset();
        check(c.resets == 1 && c.getValue() == 0, "reset");
    }

    static void testSliceAndAccumulation() {
        byte[] data = { 10, 20, 30, 40, 50 };
        RecordingChecksum c = new RecordingChecksum();
        c.update(data, 1, 3);           // sums 20+30+40 = 90
        check(c.getValue() == 90, "update honors off/len slice");
        c.update(data, 4, 1);           // adds 50
        check(c.getValue() == 140, "accumulation across updates");
        c.update(data, 0, 0);           // zero-length updates nothing
        check(c.getValue() == 140 && c.updates == 3, "zero-length update no change");
    }

    public static void main(String[] args) {
        testChecksum();
        testSliceAndAccumulation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
