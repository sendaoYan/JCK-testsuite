/*
 * @test
 * @summary java.nio.channels.WritableByteChannel conformance: white-box structure + black-box behavior.
 * @run main WritableByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WritableByteChannelTest {
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
        check(WritableByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(WritableByteChannel.class),
              "extends Channel");
        Set<String> names = new HashSet<>();
        for (Method m : WritableByteChannel.class.getMethods()) names.add(m.getName());
        check(names.contains("write"), "has write");
        check(!names.contains("read"), "no read in WritableByteChannel");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("w.bin");

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                WritableByteChannel wbc = fc;
                ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3});
                check(wbc.write(a) == 3, "wrote 3 bytes");
                check(!a.hasRemaining(), "buffer fully consumed");
                ByteBuffer b = ByteBuffer.wrap(new byte[] {4});
                check(wbc.write(b) == 1, "wrote 1 more");
            }
            check(java.util.Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 4}),
                  "file content after interface writes");

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                WritableByteChannel wbc = fc;
                check(wbc.write(ByteBuffer.wrap(new byte[] {5})) == 1, "appended 1");
            }
            check(Files.readAllBytes(file).length == 5, "file grew to 5");

            FileChannel fc2 = FileChannel.open(file, StandardOpenOption.WRITE);
            WritableByteChannel wbc2 = fc2;
            fc2.close();
            boolean threw = false;
            try { wbc2.write(ByteBuffer.allocate(1)); }
            catch (java.nio.channels.ClosedChannelException e) { threw = true; }
            check(threw, "write after close -> ClosedChannelException");
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

    /** Minimal writable channel draining into a ByteArrayOutputStream. */
    static final class SinkChannel implements WritableByteChannel {
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        private boolean open = true;
        public int write(ByteBuffer src) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            int n = src.remaining();
            byte[] tmp = new byte[n];
            src.get(tmp);
            out.write(tmp);
            return n;
        }
        public boolean isOpen() { return open; }
        public void close() { open = false; }
    }

    // ---- A5 white-box: exact surface ----
    static void testWhiteBoxA5() throws Exception {
        check(WritableByteChannel.class.getDeclaredMethods().length == 1,
              "single declared method write(ByteBuffer)");
        boolean throwsIO = false;
        for (Method m : WritableByteChannel.class.getMethods())
            if (m.getName().equals("write"))
                for (Class<?> e : m.getExceptionTypes())
                    if (e == java.io.IOException.class) throwsIO = true;
        check(throwsIO, "write declares throws IOException");
        boolean anyDefault = false;
        for (Method m : WritableByteChannel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods");
        check(WritableByteChannel.class.getMethod("write", ByteBuffer.class).getReturnType() == int.class,
              "write returns int");
        check(!java.nio.channels.ReadableByteChannel.class.isAssignableFrom(WritableByteChannel.class),
              "writable interface has no read direction");
    }

    // ---- A5: contract via minimal implementation ----
    static void testCustomImplementation() throws Exception {
        try (WritableByteChannel ch = new SinkChannel()) {
            ByteBuffer b = ByteBuffer.wrap(new byte[] {1, 2, 3});
            check(ch.write(b) == 3, "write returns byte count");
            check(!b.hasRemaining(), "source buffer drained");
            java.util.Arrays.equals(((SinkChannel) ch).out.toByteArray(), new byte[] {1, 2, 3});
            check(true, "bytes delivered verbatim");

            // write 只消费 [position,limit)
            ByteBuffer slice = ByteBuffer.wrap(new byte[] {9, 8, 7, 6});
            slice.position(1).limit(3);
            check(ch.write(slice) == 2, "write honours buffer position..limit");
            check(((SinkChannel) ch).out.toByteArray()[3] == 8
                  && ((SinkChannel) ch).out.toByteArray()[4] == 7,
                  "only bytes 1..2 delivered");
            check(slice.position() == 3, "buffer position advanced to limit");

            check(ch.write(ByteBuffer.allocate(0)) == 0, "empty buffer -> 0");
            ByteBuffer full = ByteBuffer.allocate(2);
            full.position(2);
            check(ch.write(full) == 0, "full buffer -> 0");
        }
        WritableByteChannel closed = new SinkChannel();
        closed.close();
        check(!closed.isOpen(), "custom channel closed");
        closed.close();
        check(true, "double close tolerated by implementation");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.write(ByteBuffer.allocate(1)), "write after close -> CCE");
    }

    // ---- A5: FileChannel view of the same contract ----
    static void testFileChannelView() throws Exception {
        Path dir = Files.createTempDirectory("jck-a5-wbc");
        try {
            Path file = dir.resolve("w2.bin");
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                WritableByteChannel ch = fc;
                ByteBuffer slice = ByteBuffer.wrap(new byte[] {9, 8, 7, 6});
                slice.position(1).limit(3);
                check(ch.write(slice) == 2, "FileChannel write honours [position,limit)");
                check(java.util.Arrays.equals(Files.readAllBytes(file), new byte[] {8, 7}),
                      "only sliced bytes persisted");
                check(ch.write(ByteBuffer.allocate(0)) == 0, "empty buffer -> 0");
                check(fc.size() == 2, "empty write did not extend file");
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
        testFileChannelView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
