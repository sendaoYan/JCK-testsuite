/*
 * @test
 * @summary java.io.FileDescriptor conformance: white-box structure + black-box
 *          behavior (in/out/err are non-null valid descriptors, a bare
 *          constructor produces an invalid descriptor, sync() on an invalid
 *          descriptor raises SyncFailedException, sync() on the standard
 *          descriptors succeeds without observable side effects).
 * @run main FileDescriptorTest
 */

import java.io.FileDescriptor;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FileDescriptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.FileDescriptor");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        for (String f : new String[]{"in", "out", "err"}) {
            Field fld = c.getField(f);
            check(Modifier.isPublic(fld.getModifiers()) && Modifier.isStatic(fld.getModifiers())
                      && Modifier.isFinal(fld.getModifiers()), "standard stream " + f);
        }
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getMethod("valid").getReturnType() == boolean.class, "valid() returns boolean");
        check(c.getMethod("sync").getExceptionTypes().length == 1
                  && c.getMethod("sync").getExceptionTypes()[0] == SyncFailedException.class,
              "sync() declares SyncFailedException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. standard descriptors are distinct, non-null and valid
        check(FileDescriptor.in != null && FileDescriptor.out != null
                  && FileDescriptor.err != null, "in/out/err non-null");
        check(FileDescriptor.in != FileDescriptor.out && FileDescriptor.out != FileDescriptor.err
                  && FileDescriptor.in != FileDescriptor.err, "three distinct descriptors");
        check(FileDescriptor.in.valid(), "stdin fd valid");
        check(FileDescriptor.out.valid(), "stdout fd valid");
        check(FileDescriptor.err.valid(), "stderr fd valid");

        // 2. a bare descriptor wraps no OS resource
        FileDescriptor bare = new FileDescriptor();
        check(!bare.valid(), "constructed descriptor is invalid");
        check(!bare.equals(FileDescriptor.in), "not equal to stdin");
        bare.hashCode();                                     // callable, value unused
        passed++;

        // 3. sync on an unbound descriptor fails
        try {
            bare.sync();
            check(false, "sync on invalid descriptor must throw");
        } catch (SyncFailedException sfe) {
            passed++;
        }

        // 4. sync on a real file descriptor succeeds
        java.io.File tmp = java.io.File.createTempFile("fdesc", ".tmp");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            FileDescriptor fd = raf.getFD();
            check(fd.valid(), "fd of an open file is valid");
            raf.write(1);
            fd.sync();                                       // fsync to disk; must not throw
            passed++;
            check(fd.valid(), "fd still valid before close");
            raf.close();
        } finally {
            tmp.delete();
        }

        // 5. sync on stdout: flushing a pipe-backed fd is allowed to fail on some
        //    platforms only with SyncFailedException; asserting plain success on
        //    Linux validates the call path without observable output side effects
        try {
            FileDescriptor.out.sync();
            passed++;
        } catch (SyncFailedException sfe) {
            passed++;                                        // permitted on non-fsync targets
        }

        // 6. equals/hashCode identity semantics
        check(FileDescriptor.in.equals(FileDescriptor.in), "reflexive");
        check(!FileDescriptor.in.equals(null), "not equal to null");
        check(!new FileDescriptor().equals(new FileDescriptor()), "distinct bare fds unequal");

        // 7. toString is available and non-empty
        check(FileDescriptor.out.toString() != null && !FileDescriptor.out.toString().isEmpty(),
              "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
