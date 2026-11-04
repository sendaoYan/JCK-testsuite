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
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    // ---- 深化追加（契约级），行为全部经 JDK-8 实测锁定 ----

    /** 三静态字段：类型、二次访问同一实例（static final 身份）。 */
    static void testStaticFieldsDeep() throws Exception {
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
    static void testToStringAndHashCode() throws Exception {
        check(FileDescriptor.out.toString().startsWith("java.io.FileDescriptor@"),
              "toString format java.io.FileDescriptor@<hash>: " + FileDescriptor.out);
        FileDescriptor bare = new FileDescriptor();
        check(bare.toString().startsWith("java.io.FileDescriptor@"), "bare toString format");
        check(bare.hashCode() == bare.hashCode(), "hashCode stable within an instance");
        check(FileDescriptor.in.hashCode() == FileDescriptor.in.hashCode(),
              "hashCode stable for static fds");
    }

    /** bare 构造器 + sync 精确消息（JDK-8 实测 "sync failed"）。 */
    static void testBareDescriptorDeep() throws Exception {
        FileDescriptor bare = new FileDescriptor();
        check(!bare.valid(), "bare fd invalid");
        try {
            bare.sync();
            check(false, "bare sync must throw");
        } catch (SyncFailedException sfe) {
            check("sync failed".equals(sfe.getMessage()),
                  "JDK-8 sync message exact: " + sfe.getMessage());
        }
        check(!bare.equals(FileDescriptor.in), "bare != stdin");
        check(!bare.equals(new Object()), "bare != unrelated object");
        check(FileDescriptor.in.equals(FileDescriptor.in), "reflexive static");
    }

    /** RAF 的 FD 生命周期：open→valid、close→invalid、getFD 同实例。 */
    static void testRafFdLifecycle() throws Exception {
        File tmp = File.createTempFile("fdraf8", ".tmp");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            FileDescriptor fd = raf.getFD();
            check(fd.valid(), "RAF fd valid while open");
            check(raf.getFD() == fd, "RAF getFD same instance");
            raf.write(1);
            fd.sync();
            passed++;                                    // fsync on real fd must not throw
            raf.close();
            check(!fd.valid(), "RAF fd invalid after close");
            try {
                fd.sync();
                check(false, "sync on closed fd must throw");
            } catch (SyncFailedException expected) {
                passed++;                                // 已关 fd 的 sync 失败是 SyncFailed
            }
        } finally {
            tmp.delete();
        }
    }

    /** FIS 与 FOS 的 FD：各自有效、close 后失效、两个流的 FD 互不相等。 */
    static void testFileStreamFds() throws Exception {
        File tmp = File.createTempFile("fdst8", ".tmp");
        try {
            FileInputStream in = new FileInputStream(tmp);
            FileOutputStream out = new FileOutputStream(tmp);
            FileDescriptor fin = in.getFD();
            FileDescriptor fout = out.getFD();
            check(fin.valid() && fout.valid(), "both fds valid while streams open");
            check(fin != fout && !fin.equals(fout), "distinct fds are not equal");
            check(!fin.equals(FileDescriptor.out), "file fd != stdout fd");
            out.write(0x41);
            fout.sync();
            passed++;                                    // fsync after write must not throw
            in.close();
            out.close();
            check(!fin.valid() && !fout.valid(), "both fds invalid after close");
        } finally {
            tmp.delete();
        }
    }

    /** 白盒：final 类、sync() 在 JDK-8 是 native（JDK-27 改为非 native，差异记录）。 */
    static void testWhiteBoxDeep() throws Exception {
        Class<?> c = FileDescriptor.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getMethod("valid").getReturnType() == boolean.class, "valid() returns boolean");
        check(java.lang.reflect.Modifier.isNative(c.getMethod("sync").getModifiers()),
              "JDK-8: sync() is native");
        java.lang.reflect.Field f = c.getField("in");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers())
              && java.lang.reflect.Modifier.isFinal(f.getModifiers()),
              "in is static final");
    }

    /** sync 幂等、valid 一致性、身份可用作集合 key。 */
    static void testSyncIdempotenceAndIdentity() throws Exception {
        File tmp = File.createTempFile("fdid8", ".tmp");
        try {
            RandomAccessFile raf = new RandomAccessFile(tmp, "rw");
            FileDescriptor fd = raf.getFD();
            raf.write(0x7F);
            fd.sync();
            fd.sync();
            passed++;                                    // repeated sync on live fd is fine
            check(fd.valid() && fd.valid(), "valid() stable across calls");
            java.util.HashSet<FileDescriptor> set = new java.util.HashSet<FileDescriptor>();
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
            passed++;                                    // stderr sync succeeded here
        } catch (SyncFailedException allowed) {
            passed++;                                    // permitted for non-fsync targets
        }
        FileDescriptor bare = new FileDescriptor();
        try {
            bare.sync();
        } catch (SyncFailedException expected) {
            passed++;
        }
        check(!bare.valid(), "bare fd stays invalid after failed sync");
    }

    /** 白盒补充：不实现任何接口（纯 final 类）。 */
    static void testNoInterfaces() throws Exception {
        Class<?>[] ifaces = FileDescriptor.class.getInterfaces();
        check(ifaces.length == 0, "FileDescriptor implements no interfaces: " + ifaces.length);
        check(FileDescriptor.class.getSuperclass() == Object.class, "direct subclass of Object");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        testStaticFieldsDeep();
        testToStringAndHashCode();
        testBareDescriptorDeep();
        testRafFdLifecycle();
        testFileStreamFds();
        testWhiteBoxDeep();
        testSyncIdempotenceAndIdentity();
        testNoInterfaces();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
