/*
 * @test
 * @summary java.nio.MappedByteBuffer conformance: white-box structure (extends ByteBuffer,
 *          force/load/isLoaded) plus black-box READ_ONLY/READ_WRITE/PRIVATE mappings,
 *          view sharing, persistence and legacy RandomAccessFile entry point.
 * @run main MappedByteBufferTest
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MappedByteBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    // 清理不是被测契约。Windows 上映射句柄由 Cleaner 在 GC 后才释放，期间
    // delete 会抛 ERROR_SHARING_VIOLATION；重试并促 GC，仍失败就 deleteOnExit。
    static void deleteBestEffort(Path p) {
        for (int i = 0; i < 10; i++) {
            try {
                Files.deleteIfExists(p);
                return;
            } catch (IOException e) {
                System.gc();
                try { Thread.sleep(50); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        p.toFile().deleteOnExit();
    }

    /** An operation that may throw a checked IOException (FileChannel.map & co.). */
    interface IORunnable { void run() throws IOException; }

    /** checkThrows for operations whose signature declares IOException. */
    static void checkThrowsIO(Class<? extends Throwable> type, IORunnable r, String msg)
            throws IOException {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.MappedByteBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "MappedByteBuffer is abstract");
        check(java.nio.ByteBuffer.class.isAssignableFrom(c), "extends ByteBuffer");
        for (String n : new String[] {"force", "load", "isLoaded"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
        // Version snapshot (JDK-8): the ranged force(int,int) overload only exists
        // from JDK 13 on.
        check(java.util.Arrays.stream(c.getMethods())
                        .noneMatch(m -> m.getName().equals("force") && m.getParameterCount() == 2),
                "JDK-8 MappedByteBuffer declares no force(int,int) (added in JDK-13+)");
    }

    /**
     * Contract matrix beyond the basics: direct-memory property set, load/force return
     * values, channel-position neutrality, map parameter validation, beyond-EOF and
     * closed/immutable-channel failures, PRIVATE fork isolation, cursor behavior on a
     * mapped buffer, and cross-family equality with a heap buffer. Owns its own temp
     * file and cleans up in finally.
     */
    static void testContractMatrix() throws Exception {
        Path dir = Files.createTempDirectory("jck-mapped-mx");
        try {
            Path f = dir.resolve("matrix.bin");
            byte[] data = new byte[256];
            for (int i = 0; i < data.length; i++) data[i] = (byte) i;
            Files.write(f, data);

            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                MappedByteBuffer ro = ch.map(FileChannel.MapMode.READ_ONLY, 0, 256);
                check(ro.isDirect(), "mapped buffer isDirect");
                check(!ro.hasArray(), "mapped buffer hasArray false");
                checkThrows(UnsupportedOperationException.class, () -> ro.array(),
                        "mapped array() -> UnsupportedOperationException");
                checkThrows(UnsupportedOperationException.class, () -> ro.arrayOffset(),
                        "mapped arrayOffset() -> UnsupportedOperationException");
                check(ro.load() == ro, "load() returns this");
                check(!ro.isLoaded() || ro.isLoaded(), "isLoaded() callable without throwing");
                check(ro.force() == ro, "read-only map force() returns this without throwing");
                check(ro.asReadOnlyBuffer().isReadOnly(),
                        "asReadOnlyBuffer of a mapped buffer is read-only");

                // map() does not move the channel position.
                ch.position(16);
                ch.map(FileChannel.MapMode.READ_ONLY, 0, 16);
                check(ch.position() == 16, "map leaves the channel position unchanged");

                // Views stay direct and stay MappedByteBuffer.
                check(ro.slice().isDirect() && ro.slice() instanceof MappedByteBuffer,
                        "slice of a mapped buffer is a direct MappedByteBuffer");

                // Relative reads follow the normal cursor contract.
                MappedByteBuffer rel = ch.map(FileChannel.MapMode.READ_ONLY, 0, 4);
                check(rel.get() == 0 && rel.position() == 1,
                        "relative get on a mapped buffer advances the cursor");

                // Parameter validation: negative offset/size, oversize length, beyond EOF.
                checkThrowsIO(IllegalArgumentException.class,
                        () -> ch.map(FileChannel.MapMode.READ_ONLY, -1, 8),
                        "map(-1,8) -> IllegalArgumentException");
                checkThrowsIO(IllegalArgumentException.class,
                        () -> ch.map(FileChannel.MapMode.READ_ONLY, 0, -1),
                        "map(0,-1) -> IllegalArgumentException");
                checkThrowsIO(IllegalArgumentException.class,
                        () -> ch.map(FileChannel.MapMode.READ_ONLY, 0, (long) Integer.MAX_VALUE + 1),
                        "map(0,MAX+1) size beyond int range -> IllegalArgumentException");
                checkThrowsIO(IOException.class,
                        () -> ch.map(FileChannel.MapMode.READ_ONLY, (long) Integer.MAX_VALUE + 1, 1),
                        "map(MAX+1,1) would extend the file -> IOException on a read-only channel");
                checkThrowsIO(IOException.class,
                        () -> ch.map(FileChannel.MapMode.READ_ONLY, 250, 10),
                        "map(READ_ONLY,250,10) beyond EOF -> IOException");
            }

            // Closed channel and immutable channel failures.
            FileChannel cc = FileChannel.open(f, StandardOpenOption.READ);
            cc.close();
            checkThrowsIO(java.nio.channels.ClosedChannelException.class,
                    () -> cc.map(FileChannel.MapMode.READ_ONLY, 0, 4),
                    "map on a closed channel -> ClosedChannelException");
            try (RandomAccessFile raf = new RandomAccessFile(f.toFile(), "r")) {
                checkThrowsIO(java.nio.channels.NonWritableChannelException.class,
                        () -> raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 4),
                        "READ_WRITE map on a read-only channel -> NonWritableChannelException");
            }

            // READ_WRITE write-back through force, verified from the file system.
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer rw = ch.map(FileChannel.MapMode.READ_WRITE, 100, 4);
                rw.put(0, (byte) 55);
                check(rw.force() == rw, "READ_WRITE force() returns this");
            }
            check(Files.readAllBytes(f)[100] == 55, "mapped write persisted to the file after force");

            // PRIVATE mappings fork: writes stay private per mapping and never reach the file.
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer p1 = ch.map(FileChannel.MapMode.PRIVATE, 0, 16);
                MappedByteBuffer p2 = ch.map(FileChannel.MapMode.PRIVATE, 0, 16);
                p1.put(0, (byte) 77);
                check(p1.get(0) == 77, "private map sees its own write");
                check(p2.get(0) == 0, "sibling private map does not see the write");
            }
            check(Files.readAllBytes(f)[0] == 0, "PRIVATE writes never reach the file");

            // Cross-family equality with a heap buffer holding the same content.
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                MappedByteBuffer ro = ch.map(FileChannel.MapMode.READ_ONLY, 0, 256);
                check(ro.equals(java.nio.ByteBuffer.wrap(Files.readAllBytes(f))),
                        "mapped buffer equals a heap buffer with identical content");
                checkThrows(java.nio.ReadOnlyBufferException.class, () -> ro.compact(),
                        "compact on a read-only mapped buffer -> ReadOnlyBufferException");
            }
        } finally {
            deleteBestEffort(dir.resolve("matrix.bin"));
            deleteBestEffort(dir);
        }
    }

    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-mapped");
        try {
            Path f = dir.resolve("mapped.bin");
            byte[] data = new byte[256];
            for (int i = 0; i < data.length; i++) data[i] = (byte) i;
            Files.write(f, data);

            // READ_ONLY mapping reads the file content and forbids writes
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                MappedByteBuffer ro = ch.map(FileChannel.MapMode.READ_ONLY, 0, 256);
                check(ro.get(0) == 0 && ro.get(255) == (byte) 255, "read-only map reads");
                check(ro.capacity() == 256, "capacity matches region");
                checkThrows(java.nio.ReadOnlyBufferException.class,
                        () -> ro.put(0, (byte) 1), "READ_ONLY map put");
            }

            // READ_WRITE mapping: modifications visible after force + reopen
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer rw = ch.map(FileChannel.MapMode.READ_WRITE, 0, 256);
                check(!rw.isReadOnly(), "READ_WRITE map is writable");
                rw.put(0, (byte) 42);
                rw.put(255, (byte) 42);
                rw.force();
                check(rw.get(0) == 42, "write then read through the same map");
                check(rw.slice() instanceof MappedByteBuffer, "slice stays MappedByteBuffer");
                check(rw.duplicate() instanceof MappedByteBuffer,
                      "duplicate stays MappedByteBuffer");
                rw.position(100).limit(200);
                check(rw.remaining() == 100, "position/limit honored");
                rw.load();
                check(rw.isLoaded() || !rw.isLoaded(), "isLoaded callable");
            }
            byte[] after = Files.readAllBytes(f);
            check(after[0] == 42 && after[255] == 42, "modifications persisted to the file");

            // PRIVATE mapping: modifications invisible outside the map
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                MappedByteBuffer pv = ch.map(FileChannel.MapMode.PRIVATE, 0, 256);
                pv.put(0, (byte) 99);
                pv.force();
                check(pv.get(0) == 99, "private map sees its own write");
            }
            check(Files.readAllBytes(f)[0] == 42, "PRIVATE map left the file untouched");

            // zero-length mapping is allowed
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                MappedByteBuffer empty = ch.map(FileChannel.MapMode.READ_ONLY, 0, 0);
                check(empty.capacity() == 0, "zero-length mapping");
            }

            // offset+length mapping into a larger file
            try (FileChannel ch = FileChannel.open(f, StandardOpenOption.READ)) {
                MappedByteBuffer part = ch.map(FileChannel.MapMode.READ_ONLY, 16, 8);
                check(part.capacity() == 8 && part.get(0) == 16, "offset mapping");
            }

            // RandomAccessFile.getChannel().map is the legacy entry point
            try (RandomAccessFile raf = new RandomAccessFile(f.toFile(), "rw")) {
                MappedByteBuffer legacy = raf.getChannel().map(
                        FileChannel.MapMode.READ_WRITE, 0, raf.length());
                check(legacy.capacity() == 256, "legacy map");
            }
        } finally {
            deleteBestEffort(dir.resolve("mapped.bin"));
            deleteBestEffort(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
