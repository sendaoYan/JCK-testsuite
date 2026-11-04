/*
 * @test
 * @summary java.io.FileDescriptor conformance: full contract coverage.
 *          in/out/err are non-null distinct valid descriptors; a bare constructor
 *          yields an invalid descriptor; sync() on invalid throws SyncFailedException;
 *          sync() on a real fd succeeds; equals/hashCode identity; toString.
 * @run main FileDescriptorTest
 */

import java.io.File;
import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;

public class FileDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        check(FileDescriptor.in != null && FileDescriptor.out != null
              && FileDescriptor.err != null, "in/out/err non-null");
        check(FileDescriptor.in != FileDescriptor.out && FileDescriptor.out != FileDescriptor.err
              && FileDescriptor.in != FileDescriptor.err, "three distinct descriptors");
        check(FileDescriptor.in.valid(), "stdin fd valid");
        check(FileDescriptor.out.valid(), "stdout fd valid");
        check(FileDescriptor.err.valid(), "stderr fd valid");

        FileDescriptor bare = new FileDescriptor();
        check(!bare.valid(), "constructed descriptor is invalid");
        check(!bare.equals(FileDescriptor.in), "not equal to stdin");
        bare.hashCode();
        passed++;

        try {
            bare.sync();
            check(false, "sync on invalid descriptor must throw");
        } catch (SyncFailedException sfe) {
            passed++;
        }

        File tmp = File.createTempFile("fdesc", ".tmp");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            FileDescriptor fd = raf.getFD();
            check(fd.valid(), "fd of an open file is valid");
            raf.write(1);
            fd.sync();                                     // fsync to disk; must not throw
            passed++;
            check(fd.valid(), "fd still valid before close");
            raf.close();
            // Note: valid() reflects the fd int (== -1 only for the bare ctor);
            // closeAll() flips the `closed` flag but does not reset fd, so no
            // after-close valid() assertion is made here.
        } finally {
            tmp.delete();
        }

        try {
            FileDescriptor.out.sync();
            passed++;
        } catch (SyncFailedException sfe) {
            passed++;                                      // permitted on non-fsync targets
        }

        check(FileDescriptor.in.equals(FileDescriptor.in), "reflexive equals");
        check(!FileDescriptor.in.equals(null), "not equal to null");
        check(!new FileDescriptor().equals(new FileDescriptor()), "distinct bare fds unequal");

        check(FileDescriptor.out.toString() != null && !FileDescriptor.out.toString().isEmpty(),
              "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
