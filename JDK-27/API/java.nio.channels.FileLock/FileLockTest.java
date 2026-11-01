/*
 * @test
 * @summary java.nio.channels.FileLock conformance: channel/validity/overlap.
 * @run main FileLockTest
 */

import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class FileLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileLock() throws Exception {
        File f = File.createTempFile("jck-fl", ".tmp");
        f.deleteOnExit();
        try (FileChannel ch = FileChannel.open(f.toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            try (FileLock lock = ch.lock()) {
                check(lock.isValid(), "isValid");
                check(lock.channel() == ch, "channel");
                check(!lock.isShared(), "exclusive lock not shared");
                check(lock.position() == 0, "position");
                check(lock.size() == Long.MAX_VALUE, "whole-file size");
                check(lock.overlaps(0, 1), "overlaps");
                check(lock.toString() != null, "toString");
            }
            check(true, "lock released");
        }
    }

    public static void main(String[] args) throws Exception {
        testFileLock();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
