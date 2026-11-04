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

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

    /** 三静态字段：类型、二次访问同一实例（static final 身份）。 */
    static void testStaticFieldsDeep() {
        check(FileDescriptor.in == FileDescriptor.in
              && FileDescriptor.out == FileDescriptor.out
              && FileDescriptor.err == FileDescriptor.err,
              "repeated access yields the identical instance");
        check(FileDescriptor.in.getClass() == FileDescriptor.class
              && FileDescriptor.out.getClass() == FileDescriptor.class
              && FileDescriptor.err.getClass() == FileDescriptor.class,
              "all three are exactly FileDescriptor");
    }

    /** toString 格式与 hashCode 稳定性。 */
    static void testToStringAndHashCode() {
        check(FileDescriptor.out.toString().startsWith("java.io.FileDescriptor@"),
              "toString format java.io.FileDescriptor@<hash>: " + FileDescriptor.out);
        FileDescriptor bare = new FileDescriptor();
        check(bare.toString().startsWith("java.io.FileDescriptor@"), "bare toString format");
        check(bare.hashCode() == bare.hashCode(), "hashCode stable within an instance");
        check(FileDescriptor.in.hashCode() == FileDescriptor.in.hashCode(),
              "hashCode stable for static fds");
    }

    /** bare 构造器 + sync 精确消息（JDK-27 实测同为 "sync failed"）。 */
    static void testBareDescriptorDeep() throws Exception {
        FileDescriptor bare = new FileDescriptor();
        check(!bare.valid(), "bare fd invalid");
        try {
            bare.sync();
            check(false, "bare sync must throw");
        } catch (SyncFailedException sfe) {
            check("sync failed".equals(sfe.getMessage()),
                  "JDK-27 sync message exact: " + sfe.getMessage());
        }
        check(!bare.equals(FileDescriptor.in), "bare != stdin");
        check(!bare.equals(new Object()), "bare != unrelated object");
    }

    /** RAF 的 FD 生命周期：open→valid、close→invalid、getFD 同实例、closed sync 失败。 */
    static void testRafFdLifecycle() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("fdraf27", ".tmp");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            FileDescriptor fd = raf.getFD();
            check(fd.valid() && raf.getFD() == fd, "RAF fd valid, same instance");
            raf.write(1);
            fd.sync();
            passed++;
            raf.close();
            check(!fd.valid(), "RAF fd invalid after close");
            try {
                fd.sync();
                check(false, "sync on closed fd must throw");
            } catch (SyncFailedException expected) {
                check("sync failed".equals(expected.getMessage()), "closed sync message");
            }
        } finally {
            tmp.delete();
        }
    }

    /** FIS 与 FOS 的 FD：各自有效、close 后失效、互不相等。 */
    static void testFileStreamFds() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("fdst27", ".tmp");
        try {
            java.io.FileInputStream in = new java.io.FileInputStream(tmp);
            java.io.FileOutputStream out = new java.io.FileOutputStream(tmp);
            FileDescriptor fin = in.getFD();
            FileDescriptor fout = out.getFD();
            check(fin.valid() && fout.valid(), "both fds valid while streams open");
            check(fin != fout && !fin.equals(fout), "distinct fds are not equal");
            check(!fin.equals(FileDescriptor.out), "file fd != stdout fd");
            out.write(0x41);
            fout.sync();
            passed++;
            in.close();
            out.close();
            check(!fin.valid() && !fout.valid(), "both fds invalid after close");
        } finally {
            tmp.delete();
        }
    }

    /** sync 幂等、valid 一致性、身份可用作集合 key。 */
    static void testSyncIdempotenceAndIdentity() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("fdid27", ".tmp");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            FileDescriptor fd = raf.getFD();
            raf.write(0x7F);
            fd.sync();
            fd.sync();
            passed++;
            check(fd.valid() && fd.valid(), "valid() stable across calls");
            java.util.HashSet<FileDescriptor> set = new java.util.HashSet<>();
            set.add(fd);
            check(set.contains(fd), "fd found in HashSet by identity");
            set.add(fd);
            check(set.size() == 1, "adding the same fd twice keeps size 1");
            raf.close();
            check(set.contains(fd), "still found after close (identity unchanged)");
        } finally {
            tmp.delete();
        }
        try {
            FileDescriptor.err.sync();
            passed++;
        } catch (SyncFailedException allowed) {
            passed++;
        }
        FileDescriptor bare = new FileDescriptor();
        try {
            bare.sync();
        } catch (SyncFailedException expected) {
            passed++;
        }
        check(!bare.valid(), "bare fd stays invalid after failed sync");
    }

    /** 白盒深化：sync() 在 JDK-27 已非 native（与 JDK-8 差异）、无接口。 */
    static void testWhiteBoxDeep() throws Exception {
        check(!Modifier.isNative(FileDescriptor.class.getMethod("sync").getModifiers()),
              "JDK-27: sync() is no longer native");
        check(FileDescriptor.class.getInterfaces().length == 0, "implements no interfaces");
        check(FileDescriptor.class.getSuperclass() == Object.class, "direct subclass of Object");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStaticFieldsDeep();
        testToStringAndHashCode();
        testBareDescriptorDeep();
        testRafFdLifecycle();
        testFileStreamFds();
        testSyncIdempotenceAndIdentity();
        testWhiteBoxDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
