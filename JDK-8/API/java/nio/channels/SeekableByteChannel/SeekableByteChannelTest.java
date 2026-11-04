/*
 * @test
 * @summary java.nio.channels.SeekableByteChannel conformance: white-box structure + black-box behavior.
 * @run main SeekableByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SeekableByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void cleanup(Path dir) throws Exception {
        try (Stream<Path> s = Files.list(dir)) {
            for (Path p : s.collect(Collectors.toList())) Files.deleteIfExists(p);
        }
        Files.deleteIfExists(dir);
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        check(SeekableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ByteChannel.class.isAssignableFrom(SeekableByteChannel.class),
              "extends ByteChannel");
        Set<String> names = new HashSet<>();
        for (Method m : SeekableByteChannel.class.getMethods()) names.add(m.getName());
        for (String n : new String[] {"read", "write", "position", "size", "truncate"})
            check(names.contains(n), "has method " + n);
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("sb.bin");
            Files.write(file, new byte[] {1, 2, 3, 4});

            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ,
                    StandardOpenOption.WRITE)) {
                check(sbc.size() == 4, "size 4");
                check(sbc.position() == 0, "starts at 0");

                check(sbc.position(2) == sbc, "position(long) returns this");
                ByteBuffer r = ByteBuffer.allocate(2);
                check(sbc.read(r) == 2, "read from position 2");
                check(r.get(0) == 3 && r.get(1) == 4, "tail bytes");
                check(sbc.position() == 4, "position advanced");

                check(sbc.truncate(2) == sbc, "truncate returns this");
                check(sbc.size() == 2, "size 2 after truncate");
                check(sbc.position() == 2, "position clamped to size");

                sbc.write(ByteBuffer.wrap(new byte[] {9}));
                check(sbc.size() == 3, "write extended to 3");
                ByteBuffer all = ByteBuffer.allocate(3);
                sbc.position(0);
                check(sbc.read(all) == 3, "read all 3");
                check(all.get(0) == 1 && all.get(1) == 2 && all.get(2) == 9, "content after cycle");

                sbc.position(10);
                check(sbc.position() == 10, "seek beyond size allowed");
                check(sbc.size() == 3, "size unchanged by seeking");
                sbc.write(ByteBuffer.wrap(new byte[] {7}));
                check(sbc.size() == 11, "sparse write extended file to 11");
            }
        } finally {
            cleanup(dir);
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

    /** Minimal byte-array backed implementation used to pin the interface contract. */
    static final class ByteArrayChannel implements SeekableByteChannel {
        private byte[] data = new byte[0];
        private long pos;
        private boolean open = true;

        public synchronized long position() { return pos; }

        public synchronized SeekableByteChannel position(long newPosition) {
            if (newPosition < 0) throw new IllegalArgumentException("negative position");
            pos = newPosition;
            return this;
        }

        public synchronized long size() { return data.length; }

        public synchronized SeekableByteChannel truncate(long newSize) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            if (newSize < 0) throw new IllegalArgumentException("negative size");
            if (newSize < data.length) {
                byte[] cut = new byte[(int) newSize];
                System.arraycopy(data, 0, cut, 0, (int) newSize);
                data = cut;
                if (pos > newSize) pos = newSize;
            }
            return this;
        }

        public synchronized int read(ByteBuffer dst) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            if (pos >= data.length) return -1;
            int n = (int) Math.min(dst.remaining(), data.length - pos);
            dst.put(data, (int) pos, n);
            pos += n;
            return n;
        }

        public synchronized int write(ByteBuffer src) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            int n = src.remaining();
            long end = pos + n;
            if (end > data.length) {
                byte[] grown = new byte[(int) end];
                System.arraycopy(data, 0, grown, 0, data.length);
                data = grown;
            }
            src.get(data, (int) pos, n);
            pos += n;
            return n;
        }

        public synchronized boolean isOpen() { return open; }

        public synchronized void close() { open = false; }
    }

    // ---- A5 white-box additions: exact surface, no default methods ----
    static void testWhiteBoxA5() throws Exception {
        boolean anyDefault = false;
        for (Method m : SeekableByteChannel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods on the interface");
        check(SeekableByteChannel.class.getMethod("position", long.class).getReturnType()
              == SeekableByteChannel.class, "position(long) returns SeekableByteChannel");
        check(SeekableByteChannel.class.getMethod("truncate", long.class).getReturnType()
              == SeekableByteChannel.class, "truncate(long) returns SeekableByteChannel");
        check(SeekableByteChannel.class.getMethod("position").getReturnType() == long.class,
              "position() returns long");
        check(SeekableByteChannel.class.getMethod("size").getReturnType() == long.class,
              "size() returns long");
        check(SeekableByteChannel.class.getMethod("read", ByteBuffer.class).getReturnType() == int.class,
              "read returns int");
        check(SeekableByteChannel.class.getMethod("write", ByteBuffer.class).getReturnType() == int.class,
              "write returns int");
        check(java.nio.channels.FileChannel.class.isAssignableFrom(SeekableByteChannel.class) == false,
              "interface does not reference FileChannel");
        check(java.nio.channels.FileChannel.class.getInterfaces().length >= 1
              && java.util.Arrays.asList(java.nio.channels.FileChannel.class.getInterfaces())
                     .contains(SeekableByteChannel.class),
              "FileChannel implements SeekableByteChannel");
    }

    // ---- A5: contract pinned by a custom minimal implementation ----
    static void testCustomImplementation() throws Exception {
        try (SeekableByteChannel ch = new ByteArrayChannel()) {
            check(ch.size() == 0 && ch.position() == 0, "fresh channel empty at position 0");
            check(ch.read(ByteBuffer.allocate(1)) == -1, "read of empty channel -> -1");
            check(ch.write(ByteBuffer.wrap(new byte[] {1, 2, 3})) == 3, "write 3");
            check(ch.size() == 3 && ch.position() == 3, "size and position advanced by write");
            ch.position(1);
            ByteBuffer b = ByteBuffer.allocate(2);
            check(ch.read(b) == 2, "read 2 from position 1");
            check(b.get(0) == 2 && b.get(1) == 3, "read content from seeked position");
            check(ch.read(ByteBuffer.allocate(1)) == -1, "read at EOF -> -1");
            ch.position(10);
            check(ch.read(ByteBuffer.allocate(1)) == -1, "read beyond size -> -1");
            check(ch.size() == 3, "read never extends the channel");
            check(ch.write(ByteBuffer.wrap(new byte[] {9})) == 1, "write at 10");
            check(ch.size() == 11, "write beyond size extends with hole");
            ByteBuffer all = ByteBuffer.allocate(11);
            ch.position(0);
            while (all.hasRemaining()) check(ch.read(all) >= 0, "drain keeps returning bytes");
            check(all.get(2) == 3 && all.get(4) == 0 && all.get(10) == 9, "hole zero, tail byte written");
            ch.position(2);
            ch.truncate(1);
            check(ch.size() == 1, "truncate shrinks");
            check(ch.position() == 1, "truncate clamps position to new size");
            ch.truncate(5);
            check(ch.size() == 1, "truncate beyond size is a no-op");
            checkThrows(IllegalArgumentException.class, () -> ch.position(-1), "position(-1) -> IAE");
            checkThrows(IllegalArgumentException.class, () -> ch.truncate(-1), "truncate(-1) -> IAE");
        }
        SeekableByteChannel closed = new ByteArrayChannel();
        closed.close();
        check(!closed.isOpen(), "closed custom channel reports closed");
        closed.close();
        check(true, "close idempotent for the custom implementation");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.read(ByteBuffer.allocate(1)), "read after close -> CCE");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.write(ByteBuffer.allocate(1)), "write after close -> CCE");
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> closed.truncate(0),
                "truncate after close -> CCE");
        // position()/size() 无 throws 声明：接口未要求关闭后拒绝查询
        check(closed.size() == 0, "size() still answerable after close (no throws clause)");
        check(closed.position() == 0, "position() still answerable after close");
    }

    // ---- A5: real channel viewed as the interface (closed matrix + negatives) ----
    static void testInterfaceView() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-sbc");
        try {
            Path file = dir.resolve("v.bin");
            Files.write(file, new byte[] {1, 2, 3});
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ)) {
                check(sbc.size() == 3, "size via interface view");
                checkThrows(IllegalArgumentException.class, () -> sbc.position(-1),
                        "position(-1) via interface view -> IAE");
                ByteBuffer b = ByteBuffer.allocate(4);
                check(sbc.read(b) == 3, "read via interface view");
                check(sbc.read(ByteBuffer.allocate(1)) == -1, "EOF via interface view");
            }
            try (SeekableByteChannel sbc = Files.newByteChannel(file, StandardOpenOption.READ)) {
                sbc.close();
                check(!sbc.isOpen(), "closed through the interface reference");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sbc.read(ByteBuffer.allocate(1)), "read after close -> CCE");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sbc.position(), "position() after close -> CCE");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sbc.size(), "size() after close -> CCE");
                checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sbc.truncate(0), "truncate after close -> CCE");
            }
        } finally {
            cleanup(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxA5();
        testCustomImplementation();
        testInterfaceView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
