/*
 * @test
 * @summary java.lang.foreign.MemorySegment behavior in native interop: segments
 *          passed to and produced by real C calls, slicing/offset semantics as
 *          seen from C, read-only and NULL segments, string interop, and the
 *          heap-segment rejection.
 * @run main/othervm --enable-native-access=ALL-UNNAMED MemorySegmentNativeTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class MemorySegmentNativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    static final String C_SRC = """
            long jck_read_long(const long *p) { return *p; }
            void jck_write_long(long *p, long v) { *p = v; }
            int jck_read_int_at(const int *p, long idx) { return (int) p[idx]; }
            int jck_is_null(const void *p) { return p == 0; }
            """;

    static Path compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("MemorySegmentNativeTest_jckffm.c");
        Path so = dir.resolve("libMemorySegmentNativeTest_jckffm.so");
        Files.writeString(src, C_SRC);
        Process p = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), src.toString()).inheritIO().start();
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new IllegalStateException("gcc exited " + p.exitValue());
        return so;
    }

    static Linker LINKER;
    static SymbolLookup OWN;

    static MethodHandle downcall(String name, FunctionDescriptor fd) {
        try {
            return LINKER.downcallHandle(OWN.findOrThrow(name), fd);
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    // ---- values cross the boundary in both directions ----
    static void testRoundTrip() throws Throwable {
        MethodHandle readLong = downcall("jck_read_long",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        MethodHandle writeLong = downcall("jck_write_long",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);
            seg.set(ValueLayout.JAVA_LONG, 0, Long.MIN_VALUE);
            check((long) readLong.invoke(seg) == Long.MIN_VALUE, "Java-set Long.MIN_VALUE visible to C");
            writeLong.invoke(seg, Long.MAX_VALUE);
            check(seg.get(ValueLayout.JAVA_LONG, 0) == Long.MAX_VALUE, "C-set Long.MAX_VALUE visible to Java");
        }
    }

    // ---- C indexes relative to whatever base Java hands it ----
    static void testSlicingOffsets() throws Throwable {
        MethodHandle writeLong = downcall("jck_write_long",
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment base = a.allocate(32, 8);
            MemorySegment at8 = base.asSlice(8);
            writeLong.invoke(at8, 222L);
            check(base.get(ValueLayout.JAVA_LONG, 8) == 222L,
                    "C wrote through the sliced base: offset 8 in the original changed");
            check(base.get(ValueLayout.JAVA_LONG, 0) == 0L, "offset 0 untouched by write via asSlice(8)");
        }
    }

    // ---- indexed element access agrees with C's pointer arithmetic ----
    static void testIndexedAccess() throws Throwable {
        MethodHandle readIntAt = downcall("jck_read_int_at",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        try (Arena a = Arena.ofConfined()) {
            int[] data = {10, 20, 30, 40};
            MemorySegment arr = a.allocateFrom(ValueLayout.JAVA_INT, data);
            for (int i = 0; i < data.length; i++) {
                check((int) readIntAt.invoke(arr, (long) i) == data[i],
                        "C p[" + i + "] equals Java getAtIndex");
                check(arr.getAtIndex(ValueLayout.JAVA_INT, i) == data[i],
                        "Java getAtIndex(" + i + ") equals source element");
            }
            arr.setAtIndex(ValueLayout.JAVA_INT, 2, 99);
            check((int) readIntAt.invoke(arr, 2L) == 99, "Java setAtIndex visible to C");
        }
    }

    // ---- read-only segments can be read by C but not written by Java ----
    static void testReadOnly() throws Throwable {
        MethodHandle readLong = downcall("jck_read_long",
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);
            seg.set(ValueLayout.JAVA_LONG, 0, 555L);
            MemorySegment ro = seg.asReadOnly();
            check(ro.isReadOnly(), "asReadOnly marks the segment read-only");
            check((long) readLong.invoke(ro) == 555L, "C can read through a read-only segment");
            try {
                ro.set(ValueLayout.JAVA_LONG, 0, 1L);
                check(false, "Java write to read-only segment must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "Java write to read-only segment throws IllegalArgumentException");
            }
        }
    }

    // ---- MemorySegment.NULL round-trips as a null pointer ----
    static void testNullSegment() throws Throwable {
        MethodHandle isNull = downcall("jck_is_null",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            check((int) isNull.invoke(MemorySegment.NULL) == 1, "MemorySegment.NULL is NULL in C");
            check((int) isNull.invoke(a.allocate(8, 8)) == 0, "an allocated native segment is non-NULL in C");
            check(MemorySegment.NULL.address() == 0L, "NULL segment has address 0");
            check(MemorySegment.NULL.byteSize() == 0L, "NULL segment has zero size");
        }
    }

    // ---- heap segments must not cross into native calls ----
    static void testHeapRejected() throws Throwable {
        MethodHandle isNull = downcall("jck_is_null",
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        try {
            isNull.invoke(MemorySegment.ofArray(new long[2]));
            check(false, "on-heap long[] segment must be rejected by a native call");
        } catch (IllegalArgumentException expected) {
            check(true, "heap segment passed as ADDRESS throws IllegalArgumentException");
        }
        try {
            isNull.invoke(MemorySegment.ofArray(new byte[2]));
            check(false, "on-heap byte[] segment must be rejected by a native call");
        } catch (IllegalArgumentException expected) {
            check(true, "heap byte[] segment passed as ADDRESS throws IllegalArgumentException");
        }
    }

    static MemorySegment libc(String name) {
        return LINKER.defaultLookup().find(name).orElseGet(() -> {
            try {
                return SymbolLookup.libraryLookup("libc.so.6", Arena.global()).findOrThrow(name);
            } catch (IllegalArgumentException e) {
                throw new AssertionError("cannot resolve " + name + ": " + e);
            }
        });
    }

    // ---- C string interop through segments ----
    static void testStringInterop() throws Throwable {
        MethodHandle strlen = LINKER.downcallHandle(libc("strlen"),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        MethodHandle strchr = LINKER.downcallHandle(libc("strchr"),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment s = a.allocateFrom("alpha");
            check(s.get(ValueLayout.JAVA_BYTE, 0) == (byte) 'a', "first byte of Java-allocated C string");
            check(s.get(ValueLayout.JAVA_BYTE, 5) == (byte) 0, "NUL terminator materialized");
            check((long) strlen.invoke(s) == 5L, "C strlen agrees with the string length");
            MemorySegment hit = (MemorySegment) strchr.invoke(s, (int) 'p');
            check(hit.address() - s.address() == 2L, "pointer returned by C maps to offset 2 in the same segment");
            MemorySegment miss = (MemorySegment) strchr.invoke(s, (int) 'z');
            check(miss.equals(MemorySegment.NULL), "C NULL return maps to MemorySegment.NULL");
        }
    }

    public static void main(String[] args) throws Throwable {
        if (!gccAvailable()) {
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        LINKER = Linker.nativeLinker();
        System.load(compileLib().toString());
        OWN = SymbolLookup.loaderLookup();
        testRoundTrip();
        testSlicingOffsets();
        testIndexedAccess();
        testReadOnly();
        testNullSegment();
        testHeapRejected();
        testStringInterop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
