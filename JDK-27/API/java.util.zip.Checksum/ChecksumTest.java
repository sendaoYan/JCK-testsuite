/*
 * @test
 * @summary java.util.zip.Checksum interface conformance: update/getValue/reset.
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

    public static void main(String[] args) {
        testChecksum();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
