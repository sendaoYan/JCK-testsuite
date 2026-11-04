/*
 * @test
 * @summary java.nio.channels.FileLock conformance: white-box structure + black-box behavior.
 * @run main FileLockTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileLockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void deleteRecursively(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            try (var s = Files.list(p)) { for (Path c : s.toList()) deleteRecursively(c); }
        }
        Files.deleteIfExists(p);
    }

    // a minimal concrete FileLock to exercise the abstract-class contract
    static final class StubLock extends FileLock {
        boolean valid = true;
        StubLock(FileChannel ch, long pos, long size, boolean shared) {
            super(ch, pos, size, shared);
        }
        StubLock(AsynchronousFileChannel ch, long pos, long size, boolean shared) {
            super(ch, pos, size, shared);
        }
        public boolean isValid() { return valid; }
        public void release() { valid = false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.FileLock");
        check(Modifier.isAbstract(c.getModifiers()), "FileLock is abstract");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
        check(c.getDeclaredConstructors().length == 2, "two protected ctors");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"channel", "acquiredBy", "position", "size", "isShared",
                "overlaps", "isValid", "release", "close", "toString"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("lock.bin");
            Files.write(file, new byte[100]);

            // real exclusive lock from FileChannel
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                FileLock lock = ch.lock(10, 40, false);
                check(lock.acquiredBy() == ch, "acquiredBy is FileChannel");
                check(lock.channel() == ch, "channel() is the FileChannel");
                check(lock.position() == 10, "position 10");
                check(lock.size() == 40, "size 40");
                check(!lock.isShared(), "exclusive");
                check(lock.isValid(), "valid while held");

                // overlap semantics
                check(lock.overlaps(0, 10) == false, "adjacent-before no overlap");
                check(lock.overlaps(9, 2), "overlapping start");
                check(lock.overlaps(49, 1), "inside");
                check(lock.overlaps(0, 100), "enclosing");
                check(!lock.overlaps(50, 10), "after no overlap");

                // toString mentions range and mode
                String s = lock.toString();
                check(s.contains("10") && s.contains("40"), "toString contains range");
                check(s.contains("exclusive"), "toString says exclusive");

                // overlapping lock on same channel is rejected
                boolean threw = false;
                try { ch.tryLock(20, 5, false); }
                catch (java.nio.channels.OverlappingFileLockException e) { threw = true; }
                check(threw, "overlapping tryLock -> OverlappingFileLockException");

                // release then re-lock the same range
                lock.release();
                check(!lock.isValid(), "invalid after release");
                FileLock second = ch.tryLock(10, 40, false);
                check(second != null && second.isValid(), "re-lock after release");
                second.close();
                check(!second.isValid(), "close releases too");

                // shared lock (advisory; same JVM/channel interplay covered above)
                FileLock shared = ch.tryLock(0, 5, true);
                check(shared != null && shared.isShared(), "shared lock via tryLock");
                String ss = shared.toString();
                check(ss.contains("shared"), "toString says shared");
                shared.release();
            }

            // FileChannel-based stub exercises constructor contract directly
            try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                StubLock stub = new StubLock(ch, 5, 15, true);
                check(stub.channel() == ch, "stub channel()");
                check(stub.acquiredBy() == ch, "stub acquiredBy()");
                check(stub.position() == 5 && stub.size() == 15, "stub range");
                check(stub.isShared(), "stub shared");
                check(stub.isValid(), "stub valid");
                stub.release();
                check(!stub.isValid(), "stub invalid after release");
                stub.close(); // close() delegates to release()
            }

            // AsynchronousFileChannel-based lock
            ExecutorService pool = Executors.newFixedThreadPool(1, r -> {
                Thread t = new Thread(r, "afc"); t.setDaemon(true); return t;
            });
            try {
                try (AsynchronousFileChannel ach = AsynchronousFileChannel.open(file,
                        StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    FileLock fl = ach.tryLock(0, 10, false);
                    check(fl != null, "async tryLock acquired");
                    check(fl.acquiredBy() == ach, "async acquiredBy");
                    check(fl.channel() == null, "channel() null for async lock");
                    check(fl.position() == 0 && fl.size() == 10, "async range");
                    check(fl.isValid(), "async valid");
                    fl.release();
                    check(!fl.isValid(), "async invalid after release");
                }
                try (AsynchronousFileChannel ach = AsynchronousFileChannel.open(file,
                        StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                    StubLock astub = new StubLock(ach, 1, 2, false);
                    check(astub.acquiredBy() == ach, "stub async acquiredBy");
                    check(astub.channel() == null, "stub async channel() returns null");
                }
            } finally {
                pool.shutdownNow();
            }
        } finally {
            deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
