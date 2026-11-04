/*
 * @test
 * @summary java.nio.channels.ReadableByteChannel conformance: white-box structure + black-box behavior.
 * @run main ReadableByteChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ReadableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(ReadableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(ReadableByteChannel.class),
              "extends Channel");
        Set<String> names = new HashSet<>();
        for (Method m : ReadableByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("read"), "has read");
        check(!names.contains("write"), "no write in ReadableByteChannel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("r.bin");
            Files.write(file, new byte[] {10, 20, 30});

            // read via a FileChannel viewed as the interface
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ReadableByteChannel rbc = fc;
                ByteBuffer a = ByteBuffer.allocate(2);
                check(rbc.read(a) == 2, "first read fills 2");
                check(a.get(0) == 10 && a.get(1) == 20, "first chunk content");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(rbc.read(b) == 1, "second read returns remaining 1");
                check(b.get(0) == 30, "last byte content");
                ByteBuffer c = ByteBuffer.allocate(2);
                check(rbc.read(c) == -1, "read at EOF returns -1");
                check(c.position() == 0, "EOF read leaves buffer untouched");
            }

            // read into a read-only buffer is rejected (buffer contract enforced)
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ReadableByteChannel rbc = fc;
                boolean threw = false;
                try { rbc.read(ByteBuffer.allocate(4).asReadOnlyBuffer()); }
                catch (IllegalArgumentException e) { threw = true; }
                check(threw, "read into read-only buffer -> IllegalArgumentException");
            }

            // read on a closed channel
            FileChannel fc2 = FileChannel.open(file, StandardOpenOption.READ);
            ReadableByteChannel rbc2 = fc2;
            fc2.close();
            boolean threw = false;
            try { rbc2.read(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.ClosedChannelException e) { threw = true; }
            check(threw, "read after close -> ClosedChannelException");
        } finally {
            try (var s = Files.list(dir)) { s.forEach(p -> { try { Files.delete(p); } catch (Exception ignore) { } }); }
            Files.deleteIfExists(dir);
        }
    }

    // ===================== A5 contract deepening =====================

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

    /** Minimal readable channel over a byte array. */
    static final class ArrayChannel implements ReadableByteChannel {
        private final byte[] data;
        private int pos;
        private boolean open = true;
        ArrayChannel(byte[] data) { this.data = data; }
        public int read(ByteBuffer dst) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            if (pos >= data.length) return -1;
            int n = Math.min(dst.remaining(), data.length - pos);
            dst.put(data, pos, n);
            pos += n;
            return n;
        }
        public boolean isOpen() { return open; }
        public void close() { open = false; }
    }

    // ---- A5 white-box: exact surface ----
    static void testWhiteBoxA5() throws Exception {
        check(ReadableByteChannel.class.getDeclaredMethods().length == 1,
              "single declared method read(ByteBuffer)");
        boolean throwsIO = false;
        for (Method m : ReadableByteChannel.class.getMethods())
            if (m.getName().equals("read"))
                for (Class<?> e : m.getExceptionTypes())
                    if (e == java.io.IOException.class) throwsIO = true;
        check(throwsIO, "read declares throws IOException");
        boolean anyDefault = false;
        for (Method m : ReadableByteChannel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods");
        check(ReadableByteChannel.class.getMethod("read", ByteBuffer.class).getReturnType() == int.class,
              "read returns int");
        check(!java.nio.channels.WritableByteChannel.class.isAssignableFrom(ReadableByteChannel.class),
              "readable interface has no write direction");
    }

    // ---- A5: contract via minimal implementation ----
    static void testCustomImplementation() throws Exception {
        try (ReadableByteChannel ch = new ArrayChannel(new byte[] {1, 2, 3})) {
            ByteBuffer b = ByteBuffer.allocate(2);
            check(ch.read(b) == 2, "read fills up to buffer capacity");
            check(b.get(0) == 1 && b.get(1) == 2, "bytes delivered in order");
            check(b.position() == 2, "buffer position advanced by n");
            ByteBuffer c = ByteBuffer.allocate(2);
            check(ch.read(c) == 1, "subsequent read returns remainder only");
            check(c.get(0) == 3 && c.position() == 1, "tail byte delivered");
            check(ch.read(ByteBuffer.allocate(1)) == -1, "exhausted channel -> -1");

            // read 只写 [position,limit)
            ByteBuffer offset = ByteBuffer.allocate(4);
            offset.position(2);
            try (ReadableByteChannel ch2 = new ArrayChannel(new byte[] {7, 8})) {
                check(ch2.read(offset) == 2, "read honours buffer position..limit");
                check(offset.get(2) == 7 && offset.get(3) == 8, "bytes land at offset 2");
            }
            ByteBuffer full = ByteBuffer.allocate(2);
            full.position(2);
            try (ReadableByteChannel ch3 = new ArrayChannel(new byte[] {1})) {
                check(ch3.read(full) == 0, "full buffer read returns 0");
            }
        }
        ReadableByteChannel closed = new ArrayChannel(new byte[] {1});
        closed.close();
        check(!closed.isOpen(), "custom channel closed");
        closed.close();
        check(true, "double close tolerated by implementation");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.read(ByteBuffer.allocate(1)), "read after close -> CCE");
    }

    // ---- A5: FileChannel view of the same contract ----
    static void testFileChannelView() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-rbc");
        try {
            Path file = dir.resolve("r2.bin");
            Files.write(file, new byte[] {10, 20, 30});
            try (ReadableByteChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                ByteBuffer offset = ByteBuffer.allocate(4);
                offset.position(1).limit(3);
                check(ch.read(offset) == 2, "FileChannel read honours [position,limit)");
                check(offset.get(1) == 10 && offset.get(2) == 20, "bytes at buffer offsets 1..2");
                checkThrows(IllegalArgumentException.class,
                        () -> ch.read(ByteBuffer.allocate(2).asReadOnlyBuffer()),
                        "read-only destination buffer -> IAE (实测)");
                check(ch.read(ByteBuffer.allocate(0)) == 0, "empty buffer -> 0");
            }
        } finally {
            try (var s = Files.list(dir)) { s.forEach(p -> { try { Files.delete(p); } catch (Exception ignore) { } }); }
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxA5();
        testCustomImplementation();
        testFileChannelView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
