/*
 * @test
 * @summary java.nio.channels.ScatteringByteChannel conformance: white-box structure + black-box behavior.
 * @run main ScatteringByteChannelTest
 */

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScatteringByteChannelTest {
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
        check(ScatteringByteChannel.class.isInterface(), "is interface");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(ScatteringByteChannel.class),
              "extends ReadableByteChannel");
        int overloads = 0;
        for (Method m : ScatteringByteChannel.class.getMethods())
            if (m.getName().equals("read") && m.getParameterTypes().length >= 1
                && m.getParameterTypes()[0].getComponentType() == ByteBuffer.class)
                overloads++;
        check(overloads >= 2, "read(ByteBuffer[]) and read(ByteBuffer[],int,int)");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            Path file = dir.resolve("s.bin");
            Files.write(file, new byte[] {1, 2, 3, 4, 5, 6});

            try (FileChannel fc = FileChannel.open(file, StandardOpenOption.READ)) {
                ScatteringByteChannel sbc = fc;

                ByteBuffer a = ByteBuffer.allocate(2);
                ByteBuffer b = ByteBuffer.allocate(3);
                ByteBuffer c = ByteBuffer.allocate(1);
                long n = sbc.read(new ByteBuffer[] {a, b, c});
                check(n == 6, "scattered 6 bytes");
                check(a.get(0) == 1 && a.get(1) == 2, "buffer a");
                check(b.get(0) == 3 && b.get(2) == 5, "buffer b");
                check(c.get(0) == 6, "buffer c");

                fc.position(0);
                ByteBuffer x = ByteBuffer.allocate(2);
                ByteBuffer y = ByteBuffer.allocate(2);
                ByteBuffer z = ByteBuffer.allocate(2);
                long m = sbc.read(new ByteBuffer[] {x, y, z}, 1, 1);
                check(m == 2, "only buffer y received 2 bytes");
                check(x.position() == 0 && z.position() == 0, "x and z untouched");
                check(y.get(0) == 1 && y.get(1) == 2, "y got first bytes");

                fc.position(5);
                ByteBuffer p = ByteBuffer.allocate(4);
                ByteBuffer q = ByteBuffer.allocate(4);
                long partial = sbc.read(new ByteBuffer[] {p, q});
                check(partial == 1, "only 1 byte left to scatter");
                check(p.get(0) == 6 && p.position() == 1, "p holds the byte");
                check(q.position() == 0, "q empty");

                fc.position(6);
                check(sbc.read(new ByteBuffer[] {ByteBuffer.allocate(1)}) == -1, "scatter EOF -1");
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

    /** Minimal scattering source: fills buffers sequentially from one byte array. */
    static final class ScatteringSource implements ScatteringByteChannel {
        private final byte[] data;
        private int pos;
        private boolean open = true;
        final java.util.List<Integer> filled = new java.util.ArrayList<>();
        ScatteringSource(byte[] data) { this.data = data; }
        public long read(ByteBuffer[] dsts) throws java.io.IOException {
            return read(dsts, 0, dsts.length);
        }
        public int read(ByteBuffer dst) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            if (pos >= data.length) return -1;
            int n = Math.min(dst.remaining(), data.length - pos);
            dst.put(data, pos, n);
            pos += n;
            return n;
        }
        public long read(ByteBuffer[] dsts, int offset, int length) throws java.io.IOException {
            if (!open) throw new java.nio.channels.ClosedChannelException();
            if (offset < 0 || length < 0 || offset + length > dsts.length)
                throw new IndexOutOfBoundsException();
            long total = 0;
            for (int i = offset; i < offset + length; i++) {
                ByteBuffer d = dsts[i];
                if (d == null) throw new NullPointerException("null buffer at " + i);
                if (pos >= data.length) break;
                int n = Math.min(d.remaining(), data.length - pos);
                d.put(data, pos, n);
                pos += n;
                total += n;
                filled.add(n);
            }
            return total == 0 && length > 0 && pos >= data.length ? -1 : total;
        }
        public boolean isOpen() { return open; }
        public void close() { open = false; }
    }

    // ---- A5 white-box: exact surface ----
    static void testWhiteBoxA5() throws Exception {
        check(ScatteringByteChannel.class.getDeclaredMethods().length == 2,
              "exactly 2 declared read overloads");
        Method full = ScatteringByteChannel.class.getMethod("read", ByteBuffer[].class);
        Method win = ScatteringByteChannel.class.getMethod("read", ByteBuffer[].class,
                int.class, int.class);
        check(full.getReturnType() == long.class, "read(ByteBuffer[]) returns long");
        check(win.getReturnType() == long.class, "read(ByteBuffer[],int,int) returns long");
        boolean throwsIO = false;
        for (Class<?> e : win.getExceptionTypes())
            if (e == java.io.IOException.class) throwsIO = true;
        check(throwsIO, "windowed read declares throws IOException");
        boolean anyDefault = false;
        for (Method m : ScatteringByteChannel.class.getMethods())
            if (m.isDefault()) anyDefault = true;
        check(!anyDefault, "no default methods");
        check(!java.nio.channels.WritableByteChannel.class
                .isAssignableFrom(ScatteringByteChannel.class),
              "scattering interface has no write direction");
    }

    // ---- A5: window semantics + sequential distribution + bounds via impl and FileChannel ----
    static void testScatterMatrix() throws Exception {
        // custom impl: exact sequential distribution and window selection
        try (ScatteringByteChannel ch = new ScatteringSource(new byte[] {1, 2, 3, 4})) {
            ScatteringSource src = (ScatteringSource) ch;
            ByteBuffer a = ByteBuffer.allocate(2);
            ByteBuffer b = ByteBuffer.allocate(1);
            ByteBuffer c = ByteBuffer.allocate(4);
            check(ch.read(new ByteBuffer[] {a, b, c}, 1, 2) == 4, "window [1,3) scattered 4 bytes");
            check(b.get(0) == 1 && b.position() == 1, "first window buffer filled first");
            check(c.get(0) == 2 && c.get(1) == 3 && c.get(2) == 4, "overflow continues into next");
            check(a.position() == 0, "buffer outside window untouched");
            check(src.filled.equals(java.util.Arrays.asList(1, 3)),
                  "distribution recorded per buffer (1 then 3)");

            check(ch.read(new ByteBuffer[0]) == 0, "empty array -> 0 (channel not at EOF yet)");
            check(ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 0) == 0,
                  "zero length -> 0");
            check(ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}) == -1,
                  "exhausted source -> -1");
            checkThrows(IndexOutOfBoundsException.class,
                    () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 2),
                    "length past array end -> IndexOutOfBoundsException");
            checkThrows(IndexOutOfBoundsException.class,
                    () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, -1, 1),
                    "negative offset -> IndexOutOfBoundsException");
            checkThrows(NullPointerException.class,
                    () -> ch.read(new ByteBuffer[] {null}, 0, 1),
                    "null element -> NullPointerException");
        }
        ScatteringByteChannel closed = new ScatteringSource(new byte[] {1});
        closed.close();
        check(!closed.isOpen(), "custom source closed");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                () -> closed.read(new ByteBuffer[] {ByteBuffer.allocate(1)}),
                "read after close -> CCE");

        // FileChannel view: all-full -> 0, bounds identical
        Path dir = Files.createTempDirectory("jck-a5-scc");
        try {
            Path file = dir.resolve("m.bin");
            Files.write(file, new byte[] {9, 8, 7});
            try (ScatteringByteChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
                ByteBuffer full = ByteBuffer.allocate(2);
                full.position(2);
                check(ch.read(new ByteBuffer[] {full}) == 0, "all-full array -> 0");
                check(ch.read(new ByteBuffer[] {full, full}, 0, 2) == 0, "all-full window -> 0");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 0, 5),
                        "FileChannel length past end -> IndexOutOfBoundsException");
                checkThrows(IndexOutOfBoundsException.class,
                        () -> ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}, 2, 1),
                        "FileChannel offset past end -> IndexOutOfBoundsException");
                checkThrows(NullPointerException.class,
                        () -> ch.read(null, 0, 1),
                        "FileChannel null array -> NullPointerException");
                check(ch.read(new ByteBuffer[0]) == 0, "FileChannel empty scatter -> 0");
                FileChannel fc = (FileChannel) ch;
                fc.position(fc.size());
                check(ch.read(new ByteBuffer[] {ByteBuffer.allocate(1)}) == -1,
                        "FileChannel at EOF -> -1");
            }
        } finally {
            cleanup(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxA5();
        testScatterMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
