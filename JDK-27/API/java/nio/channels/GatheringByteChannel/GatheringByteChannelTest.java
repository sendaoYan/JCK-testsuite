/*
 * @test
 * @summary java.nio.channels.GatheringByteChannel conformance: white-box structure + black-box behavior.
 * @run main GatheringByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class GatheringByteChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        check(GatheringByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(GatheringByteChannel.class),
              "extends WritableByteChannel");
        int overloads = 0;
        for (Method m : GatheringByteChannel.class.getMethods())
            if (m.getName().equals("write") && m.getParameterCount() >= 1
                && m.getParameterTypes()[0].getComponentType() == ByteBuffer.class)
                overloads++;
        check(overloads >= 2, "write(ByteBuffer[]) and write(ByteBuffer[],int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("g.bin");

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                GatheringByteChannel gbc = fc;

                // full-array gather: drains buffers in order
                ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2});
                ByteBuffer b = ByteBuffer.wrap(new byte[] {3});
                long n = gbc.write(new ByteBuffer[] {a, b});
                check(n == 3, "gathered 3 bytes");
                check(!a.hasRemaining() && !b.hasRemaining(), "both drained");

                // (offset,length) form gathers only the selected subrange
                ByteBuffer x = ByteBuffer.wrap(new byte[] {9});
                ByteBuffer y = ByteBuffer.wrap(new byte[] {8, 8});
                ByteBuffer z = ByteBuffer.wrap(new byte[] {7});
                long m = gbc.write(new ByteBuffer[] {x, y, z}, 1, 1);
                check(m == 2, "only y gathered (2 bytes)");
                check(x.hasRemaining() && z.hasRemaining(), "x and z not consumed");
                check(!y.hasRemaining(), "y drained");
            }
            check(java.util.Arrays.equals(Files.readAllBytes(file), new byte[] {1, 2, 3, 8, 8}),
                  "gathered file content in order");

            // empty buffers array writes nothing
            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND)) {
                GatheringByteChannel gbc = fc;
                check(gbc.write(new ByteBuffer[0]) == 0, "empty gather is 0");
                check(Files.readAllBytes(file).length == 5, "file unchanged");
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

    /** Minimal gathering sink: records exactly which buffers were drained, in order. */
    static final class GatheringSink implements GatheringByteChannel {
        final java.util.List<String> order = new java.util.ArrayList<>();
        final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        private boolean open = true;
        public long write(ByteBuffer[] srcs) throws java.io.IOException {
            return write(srcs, 0, srcs.length);
        }
        public int write(ByteBuffer src) throws java.io.IOException {
            return (int) write(new ByteBuffer[] {src});
        }
        public long write(ByteBuffer[] srcs, int offset, int length) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                ByteBuffer s = srcs[i];
                if (s == null) throw new NullPointerException("null buffer at " + i);
                order.add("b" + (i - offset) + ":" + s.remaining());
                byte[] tmp = new byte[s.remaining()];
                s.get(tmp);
                out.write(tmp, 0, tmp.length);
                total += tmp.length;
            }
            return total;
        }
        public boolean isOpen() { return open; }
        public void close() { open = false; }
    }

    // ---- A5 white-box: exact surface ----
    static void testWhiteBoxA5() throws Exception {
        check(GatheringByteChannel.class.getDeclaredMethods().length == 2,
              "exactly 2 declared write overloads");
        Method full = GatheringByteChannel.class.getMethod("write", ByteBuffer[].class);
        Method win = GatheringByteChannel.class.getMethod("write", ByteBuffer[].class,
                int.class, int.class);
        check(full.getReturnType() == long.class, "write(ByteBuffer[]) returns long");
        check(win.getReturnType() == long.class, "write(ByteBuffer[],int,int) returns long");
        boolean throwsIO = false;
        for (Class<?> e : win.getExceptionTypes())
            if (e == java.io.IOException.class) throwsIO = true;
        check(throwsIO, "windowed write declares throws IOException");
        boolean anyDefault = false;
        for (Method m : GatheringByteChannel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods");
        check(!java.nio.channels.ReadableByteChannel.class
                .isAssignableFrom(GatheringByteChannel.class),
              "gathering interface has no read direction");
        check(!java.nio.channels.ScatteringByteChannel.class.equals(GatheringByteChannel.class),
              "gathering and scattering are distinct interfaces");
    }

    // ---- A5: window semantics + bounds + NPE via minimal impl and FileChannel ----
    static void testGatherMatrix() throws Exception {
        // custom impl: order and window
        try (GatheringByteChannel ch = new GatheringSink()) {
            ByteBuffer a = ByteBuffer.wrap(new byte[] {1});
            ByteBuffer b = ByteBuffer.wrap(new byte[] {2, 3});
            ByteBuffer c = ByteBuffer.wrap(new byte[] {4});
            GatheringSink sink = (GatheringSink) ch;
            check(ch.write(new ByteBuffer[] {a, b, c}, 1, 2) == 3, "window [1,3) gathered 3");
            check(a.hasRemaining() && !b.hasRemaining() && !c.hasRemaining(),
                  "only window buffers drained");
            check(sink.order.equals(java.util.Arrays.asList("b0:2", "b1:1")),
                  "buffers visited in sequence order");

            check(ch.write(new ByteBuffer[0]) == 0, "empty array -> 0");
            check(ch.write(new ByteBuffer[] {a}, 0, 0) == 0, "zero length -> 0");
            checkThrows(IndexOutOfBoundsException.class,
                    () -> ch.write(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 2),
                    "length past array end -> IndexOutOfBoundsException");
            checkThrows(IndexOutOfBoundsException.class,
                    () -> ch.write(new ByteBuffer[] {ByteBuffer.allocate(1)}, -1, 1),
                    "negative offset -> IndexOutOfBoundsException");
            checkThrows(NullPointerException.class,
                    () -> ch.write(new ByteBuffer[] {null}, 0, 1),
                    "null element -> NullPointerException");
        }
        GatheringByteChannel closed = new GatheringSink();
        closed.close();
        check(!closed.isOpen(), "custom sink closed");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.write(new ByteBuffer[] {ByteBuffer.allocate(1)}),
                "write after close -> CCE");

        // FileChannel view: bounds identical, all-full -> 0, no-op window
        Path dir = Files.createTempDirectory("jck-a5-gbc");
        try {
            Path file = dir.resolve("m.bin");
            try (GatheringByteChannel ch = FileChannel.open(file, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                ByteBuffer full = ByteBuffer.allocate(2);
                full.position(2);
                check(ch.write(new ByteBuffer[] {full}) == 0, "all-full array -> 0");
                check(ch.write(new ByteBuffer[] {full, full}, 0, 2) == 0, "all-full window -> 0");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 5),
                        "FileChannel length past end -> IndexOutOfBoundsException");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.write(new ByteBuffer[] {ByteBuffer.allocate(1)}, 2, 1),
                        "FileChannel offset past end -> IndexOutOfBoundsException");
                checkThrows(NullPointerException.class,
                        () -> ch.write(null, 0, 1),
                        "FileChannel null array -> NullPointerException");
                check(ch.write(new ByteBuffer[0]) == 0, "FileChannel empty gather -> 0");
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
        testGatherMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
