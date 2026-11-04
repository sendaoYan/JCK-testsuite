/*
 * @test
 * @summary java.lang.foreign.Arena lifecycle in native-call context: segments
 *          flowing into real C functions, cross-language data exchange, death
 *          semantics after close, cross-thread access rules and the
 *          libraryLookup arena lifetime.
 * @run main/othervm --enable-native-access=ALL-UNNAMED ArenaNativeTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ArenaNativeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final String C_SRC = """
            long jck_read_long(const long *p) { return *p; }
            void jck_write_long(long *p, long v) { *p = v; }
            """;

    static Path so;

    static void compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("ArenaNativeTest_jckffm.c");
        so = dir.resolve("libArenaNativeTest_jckffm.so");
        Files.writeString(src, C_SRC);
        Process p = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), src.toString()).inheritIO().start();
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new IllegalStateException("gcc exited " + p.exitValue());
    }

    static Linker LINKER;
    static MethodHandle READ, WRITE;

    static void link() {
        SymbolLookup own = SymbolLookup.loaderLookup();
        LINKER = Linker.nativeLinker();
        try {
            READ = LINKER.downcallHandle(own.findOrThrow("jck_read_long"),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
            WRITE = LINKER.downcallHandle(own.findOrThrow("jck_write_long"),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    // ---- C writes into a Java-owned arena segment, Java reads it back ----
    static void testCrossLanguageExchange() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(8, 8);
            WRITE.invoke(seg, 1122334455L);
            check(seg.get(ValueLayout.JAVA_LONG, 0) == 1122334455L,
                    "value written by C readable from Java");
            seg.set(ValueLayout.JAVA_LONG, 0, -7L);
            check((long) READ.invoke(seg) == -7L, "value written by Java readable from C");
        }
    }

    // ---- confined arena: closing kills the segment for native calls ----
    static void testConfinedCloseKillsForNative() throws Throwable {
        Arena a = Arena.ofConfined();
        MemorySegment seg = a.allocate(8, 8);
        WRITE.invoke(seg, 1L);
        check((long) READ.invoke(seg) == 1L, "segment usable before close");
        a.close();
        check(!a.scope().isAlive(), "scope dead after close");
        try {
            READ.invoke(seg);
            check(false, "downcall with segment from closed arena must throw");
        } catch (IllegalStateException expected) {
            check(true, "downcall with closed-arena segment throws IllegalStateException");
        }
        try {
            WRITE.invoke(seg, 2L);
            check(false, "downcall write with segment from closed arena must throw");
        } catch (IllegalStateException expected) {
            check(true, "downcall write with closed-arena segment throws IllegalStateException");
        }
        // double close is rejected as well
        try {
            a.close();
            check(false, "second close of a confined arena must throw");
        } catch (IllegalStateException expected) {
            check(true, "second close throws IllegalStateException");
        }
    }

    // ---- confined segments are single-threaded for native calls too ----
    static void testConfinedWrongThread() throws Throwable {
        Arena a = Arena.ofConfined();
        MemorySegment seg = a.allocate(8, 8);
        WRITE.invoke(seg, 5L);
        final Throwable[] hit = {null};
        Thread other = new Thread(() -> {
            try { READ.invoke(seg); } catch (Throwable t) { hit[0] = t; }
        });
        other.start();
        other.join();
        check(hit[0] instanceof WrongThreadException,
                "downcall with confined segment from wrong thread throws WrongThreadException, got " + hit[0]);
        a.close();
    }

    // ---- shared arenas are usable for native calls from any thread ----
    static void testSharedCrossThreadNative() throws Throwable {
        try (Arena a = Arena.ofShared()) {
            MemorySegment seg = a.allocate(8, 8);
            WRITE.invoke(seg, 42L);
            final long[] fromOther = {-1};
            Thread other = new Thread(() -> {
                try { fromOther[0] = (long) READ.invoke(seg); } catch (Throwable t) { throw new RuntimeException(t); }
            });
            other.start();
            other.join();
            check(fromOther[0] == 42L, "shared-arena segment readable by C from another thread");
        }
    }

    // ---- global and auto arenas keep native segments alive indefinitely ----
    static void testGlobalAndAuto() throws Throwable {
        MemorySegment g = Arena.global().allocate(8, 8);
        WRITE.invoke(g, 9L);
        check((long) READ.invoke(g) == 9L, "global-arena segment usable for native calls");
        MemorySegment auto = Arena.ofAuto().allocate(8, 8);
        WRITE.invoke(auto, 10L);
        check((long) READ.invoke(auto) == 10L, "auto-arena segment usable for native calls");
    }

    // ---- allocation alignment is honored on native segments ----
    static void testAlignmentHonored() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            for (long align : new long[]{8, 64, 256}) {
                MemorySegment seg = a.allocate(8, align);
                check(seg.address() % align == 0, "allocate(8," + align + ") honors alignment");
                check(seg.byteSize() == 8, "allocate(8," + align + ") honors size");
            }
        }
    }

    // ---- libraryLookup lifetime is tied to its arena ----
    static void testLibraryLookupLifetime() throws Throwable {
        Arena libArena = Arena.ofConfined();
        SymbolLookup lookup = SymbolLookup.libraryLookup(so, libArena);
        MemorySegment sym = lookup.findOrThrow("jck_read_long");
        check(sym.byteSize() == 0, "symbol segment has zero size (bare function address)");
        check(sym.isNative(), "symbol segment is native");
        MethodHandle read = LINKER.downcallHandle(sym,
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment p = a.allocate(8, 8);
            p.set(ValueLayout.JAVA_LONG, 0, 321L);
            check((long) read.invoke(p) == 321L, "function resolved via libraryLookup(Path) is callable");
        }
        libArena.close();
        try {
            lookup.find("jck_read_long");
            check(false, "find after libraryLookup arena close must throw");
        } catch (IllegalStateException expected) {
            check(true, "find after libraryLookup arena close throws IllegalStateException");
        }
        try {
            lookup.findOrThrow("jck_read_long");
            check(false, "findOrThrow after libraryLookup arena close must throw");
        } catch (IllegalStateException expected) {
            check(true, "findOrThrow after arena close throws IllegalStateException (not NoSuchElementException)");
        }
        // Path overload with a nonexistent library is rejected
        try (Arena a = Arena.ofConfined()) {
            try {
                SymbolLookup.libraryLookup(Paths.get("/tmp/jck_no_such_lib_xyz.so"), a);
                check(false, "libraryLookup of nonexistent path must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "libraryLookup of nonexistent path throws IllegalArgumentException");
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        compileLib();
        System.load(so.toString());
        link();
        testCrossLanguageExchange();
        testConfinedCloseKillsForNative();
        testConfinedWrongThread();
        testSharedCrossThreadNative();
        testGlobalAndAuto();
        testAlignmentHonored();
        testLibraryLookupLifetime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
