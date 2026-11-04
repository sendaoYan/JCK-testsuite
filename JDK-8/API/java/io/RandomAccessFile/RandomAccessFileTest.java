/*
 * @test
 * @summary java.io.RandomAccessFile API conformance: write primitives, seek, and read
 *          back.
 * @run main RandomAccessFileTest
 */

import java.io.File;
import java.io.RandomAccessFile;

public class RandomAccessFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRandomAccessFile() throws Exception {
        File f = File.createTempFile("jck", ".tmp");
        f.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.writeInt(42);
            raf.writeLong(99L);
            check(raf.length() == 12, "length");
            raf.seek(0);
            check(raf.readInt() == 42, "readInt");
            check(raf.readLong() == 99L, "readLong");
        }
    }

    public static void main(String[] args) throws Exception {
        testRandomAccessFile();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
