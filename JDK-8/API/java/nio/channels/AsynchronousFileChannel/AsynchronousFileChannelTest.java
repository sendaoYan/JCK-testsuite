/*
 * @test
 * @summary java.nio.channels.AsynchronousFileChannel conformance: open/size.
 * @run main AsynchronousFileChannelTest
 */

import java.io.File;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.channels.CompletionHandler;

public class AsynchronousFileChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousFileChannel() throws Exception {
        File f = File.createTempFile("jck-afc", ".tmp");
        f.deleteOnExit();
        AsynchronousFileChannel ch = AsynchronousFileChannel.open(f.toPath(),
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        check(ch.isOpen(), "isOpen");
        check(ch.size() == 0, "size 0");
        ch.force(true);
        check(true, "force callable");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousFileChannel();
        testOpenOptionsContract();
        testReadWriteContract();
        testLockContract();
        testSurfaceContract();
        testPooledOpenContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ===================== A7 batch: deepened contract coverage =====================

    static void checkThrows(Class<? extends Throwable> exc, Runnable action, String msg) {
        try {
            action.run();
            check(false, msg + ": expected " + exc.getSimpleName());
        } catch (Throwable t) {
            check(exc.isInstance(t), msg + ": expected " + exc.getSimpleName() + " got " + t);
        }
    }

    /** Latch-backed handler recording the completion path, result and attachment. */
    static final class Recorder<V, A> implements CompletionHandler<V, A> {
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(1);
        volatile V result;
        volatile A attachment;
        volatile Throwable failure;
        public void completed(V r, A a) { result = r; attachment = a; done.countDown(); }
        public void failed(Throwable t, A a) { failure = t; attachment = a; done.countDown(); }
        boolean awaitDone() throws InterruptedException { return done.await(10, java.util.concurrent.TimeUnit.SECONDS); }
    }

    static java.nio.file.Path tempDir() throws Exception {
        return java.nio.file.Files.createTempDirectory("jck-afc");
    }

    static void deleteTree(java.nio.file.Path dir) throws Exception {
        java.nio.file.Files.walkFileTree(dir, new java.nio.file.SimpleFileVisitor<java.nio.file.Path>() {
            public java.nio.file.FileVisitResult visitFile(java.nio.file.Path p,
                    java.nio.file.attribute.BasicFileAttributes a) throws java.io.IOException {
                java.nio.file.Files.delete(p);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
            public java.nio.file.FileVisitResult postVisitDirectory(java.nio.file.Path p,
                    java.io.IOException e) throws java.io.IOException {
                java.nio.file.Files.delete(p);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    static void testOpenOptionsContract() throws Exception {
        java.nio.file.Path dir = tempDir();
        try {
            java.nio.file.Path missing = dir.resolve("missing.bin");
            // READ on a missing file
            try {
                AsynchronousFileChannel.open(missing, StandardOpenOption.READ);
                check(false, "open READ missing must throw");
            } catch (java.nio.file.NoSuchFileException e) {
                check(true, "open READ missing -> NoSuchFileException");
            }
            // WRITE without CREATE on a missing file
            try {
                AsynchronousFileChannel.open(missing, StandardOpenOption.WRITE);
                check(false, "open WRITE missing must throw");
            } catch (java.nio.file.NoSuchFileException e) {
                check(true, "open WRITE missing -> NoSuchFileException");
            }
            // WRITE+CREATE creates an empty file
            java.nio.file.Path created = dir.resolve("created.bin");
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(created,
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                check(ch.isOpen(), "created channel open");
                check(ch.size() == 0, "new file size 0");
                check(java.nio.file.Files.exists(created), "file was created");
                checkThrows(java.nio.channels.NonReadableChannelException.class, new Runnable() {
                    public void run() { ch.read(java.nio.ByteBuffer.allocate(1), 0); }
                }, "read on write-only channel reports synchronously");
            }
            // READ-only channel: write and truncate are rejected precisely
            java.nio.file.Path f = dir.resolve("ro.bin");
            java.nio.file.Files.write(f, new byte[] {1, 2, 3});
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f, StandardOpenOption.READ)) {
                check(ch.size() == 3, "read-only size 3");
                checkThrows(java.nio.channels.NonWritableChannelException.class, new Runnable() {
                    public void run() { ch.write(java.nio.ByteBuffer.wrap(new byte[] {9}), 0); }
                }, "write on read-only channel reports synchronously");
                checkThrows(java.nio.channels.NonWritableChannelException.class, new Runnable() {
                    public void run() {
                        try { ch.truncate(1); }
                        catch (java.io.IOException e) { throw new RuntimeException(e); }
                    }
                }, "truncate on read-only channel");
                try { ch.force(true); check(true, "force(true) allowed on read-only channel"); }
                catch (Throwable t) { check(false, "force on read-only must not throw: " + t); }
                try { ch.force(false); check(true, "force(false) allowed on read-only channel"); }
                catch (Throwable t) { check(false, "force(false) must not throw: " + t); }
                // shared lock is permitted on a read-only channel, exclusive is not
                java.nio.channels.FileLock sh = ch.tryLock(0, 3, true);
                check(sh != null && sh.isValid() && sh.isShared(), "shared tryLock on read-only channel");
                sh.release();
                checkThrows(java.nio.channels.NonWritableChannelException.class, new Runnable() {
                    public void run() {
                        try { ch.tryLock(0, 3, false); }
                        catch (java.io.IOException e) { throw new RuntimeException(e); }
                    }
                }, "exclusive tryLock on read-only channel");
            }
            // close is idempotent
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f, StandardOpenOption.READ)) {
                ch.close();
                ch.close();
                check(!ch.isOpen(), "close idempotent");
            }
        } finally {
            deleteTree(dir);
        }
    }

    static void testReadWriteContract() throws Exception {
        java.nio.file.Path dir = tempDir();
        try {
            java.nio.file.Path f = dir.resolve("rw.bin");
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                // Future write: count and size growth
                java.util.concurrent.Future<Integer> w1 = ch.write(java.nio.ByteBuffer.wrap(new byte[] {10, 20, 30}), 0);
                check(w1.get(10, java.util.concurrent.TimeUnit.SECONDS) == 3, "write 3 bytes at 0");
                check(ch.size() == 3, "size grew to 3");
                check(w1.isDone() && !w1.isCancelled(), "write Future done, not cancelled");
                // overwrite in the middle
                check(ch.write(java.nio.ByteBuffer.wrap(new byte[] {99}), 2)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS) == 1, "overwrite 1 byte at 2");
                // Future read: count and content
                java.nio.ByteBuffer rb = java.nio.ByteBuffer.allocate(3);
                check(ch.read(rb, 0).get(10, java.util.concurrent.TimeUnit.SECONDS) == 3, "read 3 bytes");
                check(rb.get(0) == 10 && rb.get(1) == 20 && rb.get(2) == 99, "read sees overwriting write");
                // partial read: two-byte view at position 1
                java.nio.ByteBuffer rb2 = java.nio.ByteBuffer.allocate(2);
                check(ch.read(rb2, 1).get(10, java.util.concurrent.TimeUnit.SECONDS) == 2, "partial read 2");
                check(rb2.get(0) == 20 && rb2.get(1) == 99, "partial read content");
                // EOF: at size and beyond
                check(ch.read(java.nio.ByteBuffer.allocate(1), 3)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS) == -1, "read at size returns -1");
                check(ch.read(java.nio.ByteBuffer.allocate(1), 100)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS) == -1, "read beyond size returns -1");
                // handler forms with attachment passthrough
                Recorder<Integer, String> hw = new Recorder<Integer, String>();
                ch.write(java.nio.ByteBuffer.wrap(new byte[] {50}), 1, "w-att", hw);
                check(hw.awaitDone() && hw.failure == null && hw.result != null && hw.result == 1,
                      "handler write 1 byte");
                check("w-att".equals(hw.attachment), "write attachment passthrough");
                Recorder<Integer, Integer> hr = new Recorder<Integer, Integer>();
                java.nio.ByteBuffer hbuf = java.nio.ByteBuffer.allocate(1);
                ch.read(hbuf, 1, 7, hr);
                check(hr.awaitDone() && hr.failure == null && hr.result != null && hr.result == 1,
                      "handler read 1 byte");
                check(hr.attachment != null && hr.attachment == 7, "read attachment passthrough");
                check(hbuf.get(0) == 50, "handler read content");
                // concurrent reads at different positions both complete
                java.util.concurrent.Future<Integer> ra = ch.read(java.nio.ByteBuffer.allocate(1), 0);
                java.util.concurrent.Future<Integer> rb3 = ch.read(java.nio.ByteBuffer.allocate(1), 2);
                check(ra.get(10, java.util.concurrent.TimeUnit.SECONDS) == 1
                      && rb3.get(10, java.util.concurrent.TimeUnit.SECONDS) == 1,
                      "concurrent reads at two positions complete");
                // negative positions and read-only buffers are rejected synchronously
                checkThrows(IllegalArgumentException.class, new Runnable() {
                    public void run() { ch.read(java.nio.ByteBuffer.allocate(1), -1); }
                }, "read with negative position");
                checkThrows(IllegalArgumentException.class, new Runnable() {
                    public void run() { ch.write(java.nio.ByteBuffer.wrap(new byte[] {1}), -5); }
                }, "write with negative position");
                checkThrows(IllegalArgumentException.class, new Runnable() {
                    public void run() { ch.read(java.nio.ByteBuffer.allocate(1).asReadOnlyBuffer(), 0); }
                }, "read into read-only buffer");
                // sparse write extends the file and leaves a zero hole
                check(ch.write(java.nio.ByteBuffer.wrap(new byte[] {77}), 6)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS) == 1, "sparse write at 6");
                check(ch.size() == 7, "size extended to 7");
                java.nio.ByteBuffer hole = java.nio.ByteBuffer.allocate(5);
                check(ch.read(hole, 2).get(10, java.util.concurrent.TimeUnit.SECONDS) == 5, "read hole region");
                check(hole.get(0) == 99 && hole.get(1) == 0 && hole.get(2) == 0 && hole.get(3) == 0
                      && hole.get(4) == 77, "hole bytes are zero, tail byte written");
                // truncate contract
                check(ch.truncate(10) == ch && ch.size() == 7, "truncate to larger size is a no-op");
                check(ch.truncate(2) == ch && ch.size() == 2, "truncate to 2 shrinks");
                check(ch.read(java.nio.ByteBuffer.allocate(1), 2)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS) == -1, "EOF after truncate");
                check(ch.truncate(0) == ch && ch.size() == 0, "truncate to 0 empties");
                // force in both modes
                ch.force(true);
                check(true, "force(true) callable");
                ch.force(false);
                check(true, "force(false) callable");
            }
            check(java.nio.file.Files.exists(f), "file persisted after channel close");
        } finally {
            deleteTree(dir);
        }
    }

    static void testLockContract() throws Exception {
        java.nio.file.Path dir = tempDir();
        try {
            java.nio.file.Path f = dir.resolve("lock.bin");
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f,
                    StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                ch.write(java.nio.ByteBuffer.wrap(new byte[] {1, 2, 3, 4}), 0)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                // whole-file lock via the no-argument convenience overload
                java.util.concurrent.Future<java.nio.channels.FileLock> lf = ch.lock();
                java.nio.channels.FileLock lock = lf.get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(lock != null && lock.isValid(), "lock() acquired and valid");
                check(!lock.isShared(), "no-arg lock is exclusive");
                check(lock.position() == 0 && lock.size() == Long.MAX_VALUE,
                      "no-arg lock spans the whole file");
                check(lock.acquiredBy() == ch, "lock acquiredBy is the channel");
                check(lock.overlaps(0, 1), "whole-file lock overlaps region 0..1");
                // overlapping attempts are rejected synchronously
                checkThrows(java.nio.channels.OverlappingFileLockException.class, new Runnable() {
                    public void run() {
                        try { ch.tryLock(0, 2, false); }
                        catch (java.io.IOException e) { throw new RuntimeException(e); }
                    }
                }, "overlapping tryLock");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, new Runnable() {
                    public void run() { ch.lock(0, Long.MAX_VALUE, true); }
                }, "overlapping Future lock");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, new Runnable() {
                    public void run() {
                        ch.lock(0, 2, false, null, new CompletionHandler<java.nio.channels.FileLock, Object>() {
                            public void completed(java.nio.channels.FileLock r, Object a) { }
                            public void failed(Throwable t, Object a) { }
                        });
                    }
                }, "overlapping handler lock");
                lock.release();
                check(!lock.isValid(), "released lock invalid");
                // ranged exclusive lock via Future form
                java.nio.channels.FileLock range = ch.lock(2, 2, false)
                        .get(10, java.util.concurrent.TimeUnit.SECONDS);
                check(range.isValid() && !range.isShared(), "ranged exclusive lock");
                check(range.position() == 2 && range.size() == 2, "ranged lock bounds");
                checkThrows(java.nio.channels.OverlappingFileLockException.class, new Runnable() {
                    public void run() {
                        try { ch.tryLock(3, 1, false); }
                        catch (java.io.IOException e) { throw new RuntimeException(e); }
                    }
                }, "overlapping ranged tryLock");
                range.release();
                // shared ranged lock through the handler form
                Recorder<java.nio.channels.FileLock, String> sl = new Recorder<java.nio.channels.FileLock, String>();
                ch.lock(0, 4, true, "shared-att", sl);
                check(sl.awaitDone() && sl.failure == null && sl.result != null && sl.result.isValid(),
                      "handler lock acquired");
                check(sl.result.isShared(), "handler lock is shared");
                check("shared-att".equals(sl.attachment), "lock attachment passthrough");
                // closing the channel invalidates the held lock
                ch.close();
                check(!sl.result.isValid(), "lock invalid after channel close");
                check(!ch.isOpen(), "channel closed");
            }
            // operations on a closed channel fail with ClosedChannelException
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f,
                    StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                ch.close();
                try {
                    ch.read(java.nio.ByteBuffer.allocate(1), 0).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    check(false, "read on closed file channel must fail");
                } catch (java.util.concurrent.ExecutionException e) {
                    check(e.getCause() instanceof java.nio.channels.ClosedChannelException,
                          "closed read -> ClosedChannelException, got " + e.getCause());
                }
                try {
                    ch.write(java.nio.ByteBuffer.wrap(new byte[] {1}), 0).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    check(false, "write on closed file channel must fail");
                } catch (java.util.concurrent.ExecutionException e) {
                    check(e.getCause() instanceof java.nio.channels.ClosedChannelException,
                          "closed write -> ClosedChannelException, got " + e.getCause());
                }
                Recorder<Integer, Object> failedRead = new Recorder<Integer, Object>();
                ch.read(java.nio.ByteBuffer.allocate(1), 0, null, failedRead);
                check(failedRead.awaitDone(), "closed handler read invoked");
                check(failedRead.failure instanceof java.nio.channels.ClosedChannelException,
                      "closed handler read -> ClosedChannelException, got " + failedRead.failure);
            }
        } finally {
            deleteTree(dir);
        }
    }

    static void testSurfaceContract() {
        // AsynchronousFileChannel has no position(): reads and writes take explicit positions
        boolean noPosition = false;
        try { AsynchronousFileChannel.class.getMethod("position"); }
        catch (NoSuchMethodException e) { noPosition = true; }
        check(noPosition, "AsynchronousFileChannel has no position() method");
        // no unpositioned read(ByteBuffer)/write(ByteBuffer) either
        boolean noPlainRead = true;
        for (java.lang.reflect.Method m : AsynchronousFileChannel.class.getMethods())
            if (m.getName().equals("read") && m.getParameterTypes().length == 1) noPlainRead = false;
        check(noPlainRead, "no single-argument read overload");
        boolean noPlainWrite = true;
        for (java.lang.reflect.Method m : AsynchronousFileChannel.class.getMethods())
            if (m.getName().equals("write") && m.getParameterTypes().length == 1) noPlainWrite = false;
        check(noPlainWrite, "no single-argument write overload");
        check(java.nio.channels.AsynchronousChannel.class.isAssignableFrom(AsynchronousFileChannel.class),
              "implements AsynchronousChannel");
        check(!java.nio.channels.AsynchronousByteChannel.class.isAssignableFrom(AsynchronousFileChannel.class),
              "does not implement AsynchronousByteChannel");
        // open(Path, Set, ExecutorService, attrs) overload exists
        boolean hasPoolOpen = false;
        for (java.lang.reflect.Method m : AsynchronousFileChannel.class.getMethods())
            if (m.getName().equals("open") && m.getParameterTypes().length == 4) hasPoolOpen = true;
        check(hasPoolOpen, "open with an explicit thread pool exists");
    }

    static void testPooledOpenContract() throws Exception {
        java.nio.file.Path dir = tempDir();
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(1,
                new java.util.concurrent.ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "a7-afc");
                        t.setDaemon(true);
                        return t;
                    }
                });
        try {
            java.nio.file.Path f = dir.resolve("pooled.bin");
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(f,
                    new java.util.HashSet<java.nio.file.OpenOption>(java.util.Arrays.<java.nio.file.OpenOption>asList(
                            StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)),
                    pool)) {
                Recorder<Integer, String> hw = new Recorder<Integer, String>();
                ch.write(java.nio.ByteBuffer.wrap(new byte[] {11, 22}), 0, "pool-att", hw);
                check(hw.awaitDone() && hw.failure == null && hw.result != null && hw.result == 2,
                      "pooled handler write");
                check("pool-att".equals(hw.attachment), "pooled write attachment");
                java.nio.ByteBuffer rb = java.nio.ByteBuffer.allocate(2);
                check(ch.read(rb, 0).get(10, java.util.concurrent.TimeUnit.SECONDS) == 2, "pooled read");
                check(rb.get(0) == 11 && rb.get(1) == 22, "pooled read content");
            }
        } finally {
            pool.shutdownNow();
            deleteTree(dir);
        }
    }


}
