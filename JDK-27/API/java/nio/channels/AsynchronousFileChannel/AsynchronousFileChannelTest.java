/*
 * @test
 * @summary java.nio.channels.AsynchronousFileChannel conformance: white-box structure + black-box behavior.
 * @run main AsynchronousFileChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AsynchronousFileChannelTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.AsynchronousFileChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AsynchronousFileChannel is abstract");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");
        check(java.nio.channels.AsynchronousChannel.class.isAssignableFrom(c),
              "implements AsynchronousChannel");
        for (String n : new String[] {"open", "size", "truncate", "force", "lock", "tryLock",
                "read", "write", "close"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        ExecutorService pool = Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r, "afc-io"); t.setDaemon(true); return t;
        });
        try {
            Path file = dir.resolve("async.bin");

            // Future-style write/read at positions
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(file,
                    java.util.Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE), pool)) {
                check(ch.size() == 0, "initial size 0");
                Future<Integer> w1 = ch.write(ByteBuffer.wrap(new byte[] {10, 20, 30}), 0);
                check(w1.get(10, TimeUnit.SECONDS) == 3, "wrote 3 at 0");
                check(ch.size() == 3, "size 3");
                Future<Integer> w2 = ch.write(ByteBuffer.wrap(new byte[] {99}), 2);
                check(w2.get(10, TimeUnit.SECONDS) == 1, "wrote 1 at 2");
                ByteBuffer rbuf = ByteBuffer.allocate(3);
                Future<Integer> r1 = ch.read(rbuf, 0);
                check(r1.get(10, TimeUnit.SECONDS) == 3, "read 3");
                check(rbuf.get(0) == 10 && rbuf.get(1) == 20 && rbuf.get(2) == 99, "overlap write visible");
                // reading at EOF returns -1... at exact size returns 0 per spec; assert in {-1, 0}... use beyond-size
                ByteBuffer tail = ByteBuffer.allocate(4);
                Future<Integer> r2 = ch.read(tail, 3);
                check(r2.get(10, TimeUnit.SECONDS) == -1, "read past EOF returns -1");
                // truncate + force
                AsynchronousFileChannel tch = ch.truncate(2);
                check(tch == ch, "truncate returns this");
                check(ch.size() == 2, "size 2 after truncate");
                ch.force(true);

                // CompletionHandler variants
                CompletableFuture<Integer> hw = new CompletableFuture<>();
                ch.write(ByteBuffer.wrap(new byte[] {50}), 1, "att",
                        handler(hw));
                check(hw.get(10, TimeUnit.SECONDS) == 1, "handler write");
                CompletableFuture<Integer> hr = new CompletableFuture<>();
                ByteBuffer rb = ByteBuffer.allocate(1);
                ch.read(rb, 1, 42, handler(hr));
                check(hr.get(10, TimeUnit.SECONDS) == 1, "handler read");
                check(rb.get(0) == 50, "handler read content");

                // lock()/tryLock() + final convenience overloads
                Future<FileLock> lf = ch.lock();
                FileLock lock = lf.get(10, TimeUnit.SECONDS);
                check(lock.isValid() && !lock.isShared(), "exclusive lock via Future");
                lock.release();
                FileLock tl = ch.tryLock();
                check(tl != null && tl.isValid(), "tryLock full range");
                check(tl.position() == 0 && tl.size() == Long.MAX_VALUE,
                      "tryLock() locks 0..Long.MAX_VALUE");
                boolean overlapped = false;
                try { ch.tryLock(0, 2, false); }
                catch (java.nio.channels.OverlappingFileLockException e) { overlapped = true; }
                check(overlapped, "overlapping tryLock -> OverlappingFileLockException");
                overlapped = false;
                try {
                    ch.lock(0, Long.MAX_VALUE, true, "l", new CompletionHandler<FileLock, String>() {
                        public void completed(FileLock r, String a) { }
                        public void failed(Throwable t, String a) { }
                    });
                } catch (java.nio.channels.OverlappingFileLockException e) {
                    overlapped = true;
                }
                check(overlapped, "overlapping handler-style lock -> OverlappingFileLockException");
                tl.release();
                CompletableFuture<FileLock> lh2 = new CompletableFuture<>();
                ch.lock(0, 4, true, "l2", new CompletionHandler<FileLock, String>() {
                    public void completed(FileLock r, String a) { lh2.complete(r); }
                    public void failed(Throwable t, String a) { lh2.completeExceptionally(t); }
                });
                FileLock fl3 = lh2.get(10, TimeUnit.SECONDS);
                check(fl3 != null && fl3.isShared(), "shared lock after release");
                fl3.release();
            }
            check(Files.readAllBytes(file).length == 2, "file persisted with 2 bytes");

            // read on write-only channel fails
            try (AsynchronousFileChannel wo = AsynchronousFileChannel.open(file,
                    java.util.Set.of(StandardOpenOption.WRITE), pool)) {
                boolean threw = false;
                try { wo.read(ByteBuffer.allocate(1), 0).get(10, TimeUnit.SECONDS); }
                catch (java.nio.channels.NonReadableChannelException e) {
                    threw = true;
                }
                catch (java.util.concurrent.ExecutionException e) {
                    threw = e.getCause() instanceof java.nio.channels.NonReadableChannelException;
                }
                check(threw, "read on write-only async -> NonReadableChannelException");
            }

            // open with option set
            try (AsynchronousFileChannel ch = AsynchronousFileChannel.open(file,
                    java.util.Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE), pool)) {
                check(ch.size() == 2, "size via option-set open");
            }
            boolean existsThrew = false;
            try (AsynchronousFileChannel ignored = AsynchronousFileChannel.open(
                    dir.resolve("no-such"), java.util.Set.of(StandardOpenOption.READ), pool)) { }
            catch (java.nio.file.NoSuchFileException e) { existsThrew = true; }
            check(existsThrew, "open missing READ file -> NoSuchFileException");
        } finally {
            pool.shutdownNow();
            deleteRecursively(dir);
        }
    }

    static <A> CompletionHandler<Integer, A> handler(CompletableFuture<Integer> f) {
        return new CompletionHandler<>() {
            public void completed(Integer v, A a) { f.complete(v); }
            public void failed(Throwable t, A a) { f.completeExceptionally(t); }
        };
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
