/*
 * @test
 * @summary java.nio.MappedByteBuffer conformance: white-box structure (extends ByteBuffer,
 *          force/load/isLoaded) plus black-box READ_ONLY/READ_WRITE/PRIVATE mappings,
 *          view sharing, persistence and legacy RandomAccessFile entry point.
 * @run main MappedByteBufferTest
 */

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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.MappedByteBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "MappedByteBuffer is abstract");
        check(java.nio.ByteBuffer.class.isAssignableFrom(c), "extends ByteBuffer");
        for (String n : new String[] {"force", "load", "isLoaded"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
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
            Files.deleteIfExists(dir.resolve("mapped.bin"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
