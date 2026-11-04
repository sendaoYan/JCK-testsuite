/*
 * @test
 * @summary java.io.SyncFailedException conformance (pure black-box): the single
 *          String constructor; real trigger: FileDescriptor.sync() on an unbound
 *          descriptor; contrast: sync succeeding on a real file.
 * @run main SyncFailedExceptionTest
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;

public class SyncFailedExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructor keeps the message (the reason sync failed)
        SyncFailedException sfe = new SyncFailedException("sync failed");
        check("sync failed".equals(sfe.getMessage()), "(String) message kept");
        check(sfe.getCause() == null, "no cause");

        // 2. real trigger: syncing a descriptor that is not backed by a real fd
        try {
            new FileDescriptor().sync();
            check(false, "sync on an unbound descriptor must throw");
        } catch (SyncFailedException expected) {
            check(expected.getMessage() != null,
                  "sync failure reports a reason: " + expected.getMessage());
        }

        // 3. contrast: sync succeeds on a descriptor belonging to a real open file
        File tmp = File.createTempFile("sfe", ".dat");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            raf.write(42);
            FileDescriptor fd = raf.getFD();
            check(fd.valid(), "descriptor valid while open");
            fd.sync();
            passed++;                                  // returned normally
        } finally {
            tmp.delete();
        }

        // 4. caught as IOException by callers that only know about IOException
        try {
            throw new SyncFailedException("m");
        } catch (IOException io) {
            check(io instanceof SyncFailedException, "catchable as IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
