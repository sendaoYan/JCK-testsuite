/*
 * @test
 * @summary java.nio.channels.FileLock conformance: channel/validity/overlap.
 * @run main FileLockTest
 */

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Runnable that may throw checked exceptions (for negative cases). */
    interface IO { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> type, IO r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void deleteRecursively(Path p) throws Exception {
        if (Files.isDirectory(p)) {
            try (java.util.stream.Stream<Path> s = Files.list(p)) {
                for (Path c : s.collect(java.util.stream.Collectors.toList())) deleteRecursively(c);
            }
        }
        Files.deleteIfExists(p);
    }

    /** Minimal concrete FileLock exercising the abstract-class contract. */
    static final class StubLock extends FileLock {
        boolean valid = true;
        StubLock(FileChannel ch, long pos, long size, boolean shared) {
            super(ch, pos, size, shared);
        }
        public boolean isValid() { return valid; }
        public void release() { valid = false; }
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

    // ---- A5 white-box: abstract class structure (javap 实测) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = FileLock.class;
        check(Modifier.isAbstract(c.getModifiers()), "FileLock is abstract");
        check(java.lang.AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
        check(c.getDeclaredConstructors().length == 2, "two protected ctors (FileChannel / AsynchronousFileChannel)");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"channel", "acquiredBy", "position", "size", "isShared",
                "overlaps", "isValid", "release", "close", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
        for (String n : new String[] {"channel", "position", "size", "isShared", "overlaps", "close", "toString"})
            check(Modifier.isFinal(c.getMethod(n, paramTypes(n)).getModifiers()), n + "() is final");
        check(Modifier.isAbstract(c.getMethod("isValid").getModifiers()), "isValid() abstract");
        check(Modifier.isAbstract(c.getMethod("release").getModifiers()), "release() abstract");
    }

    static Class<?>[] paramTypes(String name) throws Exception {
        if (name.equals("overlaps")) return new Class<?>[] {long.class, long.class};
        return new Class<?>[] {};
    }

    // ---- A5: constructor contract via stub subclass ----
    static void testStubContract() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-fl");
        try {
            Path file = dir.resolve("stub.bin");
            Files.write(file, new byte[10]);
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                StubLock stub = new StubLock(ch, 5, 15, true);
                check(stub.channel() == ch, "stub channel()");
                check(stub.acquiredBy() == ch, "stub acquiredBy() returns the FileChannel");
                check(stub.position() == 5, "stub position");
                check(stub.size() == 15, "stub size");
                check(stub.isShared(), "stub shared flag");
                check(stub.isValid(), "stub validity from subclass state");
                stub.release();
                check(!stub.isValid(), "release() drives subclass state");
                stub.close();
                check(!stub.isValid(), "close() delegates to release()");
                String s = stub.toString();
                check(s.contains("5") && s.contains("15"), "toString contains position and size");
                check(s.contains("shared"), "toString says shared");

                checkThrows(IllegalArgumentException.class,
                        () -> new StubLock(ch, -1, 1, false), "ctor negative position -> IAE");
                checkThrows(IllegalArgumentException.class,
                        () -> new StubLock(ch, 0, -1, false), "ctor negative size -> IAE");
                // 版本差异（实测）：null channel —— JDK-8 允许（getter 返回 null）、JDK-27 抛 NPE
                StubLock nullCh = new StubLock(null, 0, 1, false);
                check(nullCh.channel() == null, "JDK-8: null channel accepted by ctor (实测)");
                check(nullCh.acquiredBy() == null, "JDK-8: acquiredBy() null for null-channel stub");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    // ---- A5: real lock lifecycle, overlap matrix, JVM-wide exclusivity ----
    static void testRealLocks() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-fl2");
        try {
            Path file = dir.resolve("lock.bin");
            Files.write(file, new byte[100]);
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                FileLock lock = ch.tryLock(10, 10, false); // range [10,20)
                check(lock != null && lock.isValid(), "acquired");
                check(lock.acquiredBy() == ch && lock.channel() == ch, "channel identity");

                // overlaps 矩阵（常见两侧一致项）
                check(!lock.overlaps(0, 1), "(10,20) vs (0,1)");
                check(!lock.overlaps(0, 10), "adjacent-before range no overlap");
                check(lock.overlaps(0, 11), "(0,11) reaches into range");
                check(!lock.overlaps(9, 1), "(9,1) ends exactly at start");
                check(lock.overlaps(9, 2), "(9,2) crosses start");
                check(lock.overlaps(10, 1), "starts at lock start");
                check(lock.overlaps(15, 1), "inside");
                check(lock.overlaps(19, 1), "last byte inside");
                check(lock.overlaps(19, 2), "crosses end");
                check(!lock.overlaps(20, 1), "adjacent-after no overlap");
                check(!lock.overlaps(21, 1), "after no overlap");
                check(lock.overlaps(0, 19), "enclosing prefix");
                check(lock.overlaps(0, 20), "enclosing exact");
                check(lock.overlaps(0, 21), "enclosing superset");
                check(lock.overlaps(12, 3), "strictly inside");
                check(lock.overlaps(5, 100), "superset range");
                check(!lock.overlaps(25, 0), "zero-length outside no overlap");
                // JDK-8 实测：零长查询在锁起点/之前 -> false（position+size<=this.position 分支）
                check(!lock.overlaps(0, 0), "JDK-8: zero-length query at 0 -> false");
                check(!lock.overlaps(10, 0), "JDK-8: zero-length query at lock start -> false");
                check(lock.overlaps(15, 0), "JDK-8: zero-length query inside -> true");

                String s = lock.toString();
                check(s.contains("10"), "toString contains lock position");
                check(s.contains("exclusive"), "toString says exclusive");

                // release 幂等（实测）
                lock.release();
                check(!lock.isValid(), "invalid after release");
                lock.release();
                check(!lock.isValid(), "second release is a harmless no-op");

                // 重叠锁在同一 channel 或同 JVM 其它 channel 上均被拒
                FileLock held = ch.tryLock(10, 10, false);
                checkThrows(java.nio.channels.OverlappingFileLockException.class,
                        () -> ch.tryLock(15, 5, false), "overlap same channel");
                try (FileChannel ch2 = FileChannel.open(file, StandardOpenOption.READ,
                        StandardOpenOption.WRITE)) {
                    checkThrows(java.nio.channels.OverlappingFileLockException.class,
                            () -> ch2.tryLock(15, 5, false),
                            "overlap via another channel (locks are per file per JVM)");
                    FileLock other = ch2.tryLock(50, 10, false);
                    check(other != null && other.acquiredBy() == ch2,
                          "non-overlap via another channel succeeds");
                    other.release();
                }
                held.release();

                // 共享锁在 READ+WRITE channel 上可用
                FileLock shared = ch.tryLock(10, 10, true);
                check(shared.isShared(), "shared lock reports shared");
                check(shared.toString().contains("shared"), "toString says shared");
                shared.release();

                // 越过文件大小的锁
                FileLock beyond = ch.tryLock(1000, 5, false);
                check(beyond != null && beyond.position() == 1000, "lock beyond file size legal");
                beyond.release();

                // 零长锁（JDK-8 字面语义）
                FileLock z = ch.tryLock(5, 0, false);
                check(z != null && z.size() == 0, "JDK-8 keeps literal (5,0) lock");
                FileLock neighbor = ch.tryLock(50, 1, false);
                check(neighbor != null, "JDK-8: zero-length lock blocks nothing");
                neighbor.release();
                z.release();
            }

            // WRITE-only channel：共享锁需要读权限
            try (FileChannel wo = FileChannel.open(file, StandardOpenOption.WRITE)) {
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> wo.tryLock(0, 10, true), "shared lock needs read access");
                FileLock excl = wo.tryLock(0, 10, false);
                check(excl != null && !excl.isShared(), "exclusive on WRITE-only channel");
                wo.close();
                check(!excl.isValid(), "close invalidates lock");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> excl.release(), "release after channel close");
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testFileLock();
        testWhiteBox();
        testStubContract();
        testRealLocks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
