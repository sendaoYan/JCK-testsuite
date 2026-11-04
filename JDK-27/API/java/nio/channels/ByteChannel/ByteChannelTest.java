/*
 * @test
 * @summary java.nio.channels.ByteChannel conformance: white-box structure + black-box behavior.
 * @run main ByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class ByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(ByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(ByteChannel.class),
              "extends ReadableByteChannel");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(ByteChannel.class),
              "extends WritableByteChannel");
        Set<String> names = new HashSet<>();
        for (Method m : ByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("read") && names.contains("write"), "inherits read+write");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("b.bin");
            Files.write(file, new byte[] {5, 6, 7});

            // read+write through the same ByteChannel reference
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                ByteChannel bc = fc;
                ByteBuffer r = ByteBuffer.allocate(3);
                check(bc.read(r) == 3, "read 3 via ByteChannel");
                check(r.get(0) == 5 && r.get(2) == 7, "read content");
                fc.position(0);
                check(bc.write(ByteBuffer.wrap(new byte[] {8})) == 1, "write 1 via ByteChannel");
                ByteBuffer v = ByteBuffer.allocate(3);
                fc.position(0);
                check(bc.read(v) == 3, "re-read 3");
                check(v.get(0) == 8 && v.get(1) == 6 && v.get(2) == 7, "overwrite visible");
            }
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

    /** Minimal duplex channel over one byte array: read and write share the cursor. */
    static final class ArrayByteChannel implements ByteChannel {
        private final java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        private int pos;
        private boolean open = true;
        public int read(ByteBuffer dst) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            byte[] data = buf.toByteArray();
            if (pos >= data.length) return -1;
            int n = Math.min(dst.remaining(), data.length - pos);
            dst.put(data, pos, n);
            pos += n;
            return n;
        }
        public int write(ByteBuffer src) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            byte[] data = buf.toByteArray();
            int n = src.remaining();
            int end = pos + n;
            byte[] out = new byte[Math.max(data.length, end)];
            System.arraycopy(data, 0, out, 0, data.length);
            src.get(out, pos, n);
            buf.reset();
            buf.write(out, 0, out.length);
            pos = end;
            return n;
        }
        public boolean isOpen() { return open; }
        public void close() { open = false; }
    }

    // ---- A5 white-box: empty combining interface ----
    static void testWhiteBoxA5() throws Exception {
        check(ByteChannel.class.getDeclaredMethods().length == 0,
              "ByteChannel declares no methods of its own (pure combiner)");
        check(ByteChannel.class.getInterfaces().length == 2
              && java.util.Arrays.asList(ByteChannel.class.getInterfaces())
                       .contains(java.nio.channels.ReadableByteChannel.class)
              && java.util.Arrays.asList(ByteChannel.class.getInterfaces())
                       .contains(java.nio.channels.WritableByteChannel.class),
              "exactly ReadableByteChannel + WritableByteChannel");
        check(ByteChannel.class.getDeclaredFields().length == 0, "no fields");
        check(ByteChannel.class.isAssignableFrom(java.nio.channels.SeekableByteChannel.class),
              "SeekableByteChannel extends ByteChannel");
        check(ByteChannel.class.isAssignableFrom(FileChannel.class),
              "FileChannel is a ByteChannel");
        check(ByteChannel.class.isAssignableFrom(java.nio.channels.SocketChannel.class),
              "SocketChannel is a ByteChannel");
        check(ByteChannel.class.isAssignableFrom(java.nio.channels.DatagramChannel.class),
              "DatagramChannel is a ByteChannel");
        check(!ByteChannel.class.isAssignableFrom(java.nio.channels.Pipe.SourceChannel.class),
              "Pipe.SourceChannel is not duplex");
    }

    // ---- A5: duplex contract through one reference (custom impl) ----
    static void testCustomImplementation() throws Exception {
        try (ByteChannel ch = new ArrayByteChannel()) {
            check(ch.read(ByteBuffer.allocate(1)) == -1, "read empty -> -1");
            check(ch.write(ByteBuffer.wrap(new byte[] {1, 2})) == 2, "write 2");
            check(ch.write(ByteBuffer.wrap(new byte[] {3})) == 1, "write 1 more");
            ((ArrayByteChannel) ch).pos = 0;
            ByteBuffer b = ByteBuffer.allocate(3);
            check(ch.read(b) == 3, "read back all 3");
            check(b.get(0) == 1 && b.get(1) == 2 && b.get(2) == 3, "round-trip content");
            check(ch.read(ByteBuffer.allocate(1)) == -1, "cursor shared: write advanced read EOF");
        }
        ByteChannel closed = new ArrayByteChannel();
        closed.close();
        check(!closed.isOpen(), "custom channel closed");
        closed.close();
        check(true, "double close tolerated");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.read(ByteBuffer.allocate(1)), "read after close -> CCE");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.write(ByteBuffer.allocate(1)), "write after close -> CCE");
    }

    // ---- A5: FileChannel as ByteChannel, mode matrix + closed matrix ----
    static void testFileChannelView() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-bc");
        try {
            Path file = dir.resolve("v.bin");
            Files.write(file, new byte[] {10, 20});

            // READ-only face: write rejected
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ByteChannel bc = fc;
                ByteBuffer b = ByteBuffer.allocate(2);
                check(bc.read(b) == 2 && b.get(0) == 10, "read on READ-only channel");
                checkThrows(java.nio.channels.NonWritableChannelException.class,
                        () -> bc.write(ByteBuffer.allocate(1)),
                        "write on READ-only -> NonWritableChannelException");
            }

            // WRITE-only face: read rejected, write extends at EOF
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE)) {
                ByteChannel bc = fc;
                checkThrows(java.nio.channels.NonReadableChannelException.class,
                        () -> bc.read(ByteBuffer.allocate(1)),
                        "read on WRITE-only -> NonReadableChannelException");
                check(fc.position() == 0, "WRITE opens at position 0 (not at EOF)");
                fc.position(fc.size());
                check(bc.write(ByteBuffer.wrap(new byte[] {30})) == 1, "write extends at EOF");
                check(fc.size() == 3, "file grew to 3");
            }

            // closed matrix
            FileChannel fc = FileChannel.open(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE);
            ByteChannel bc = fc;
            fc.close();
            check(!bc.isOpen(), "isOpen false through ByteChannel ref");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> bc.read(ByteBuffer.allocate(1)), "read after close -> CCE");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> bc.write(ByteBuffer.allocate(1)), "write after close -> CCE");
            fc.close();
            check(!bc.isOpen(), "double close harmless");
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
