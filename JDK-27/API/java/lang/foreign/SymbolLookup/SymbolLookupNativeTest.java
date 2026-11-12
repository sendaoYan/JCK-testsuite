/*
 * @test
 * @summary java.lang.foreign.SymbolLookup behavior around real symbol
 *          resolution: defaultLookup vs libraryLookup for libc symbols,
 *          loaderLookup of a self-compiled library, symbol stability,
 *          composition via or(), and the miss/close negative paths.
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED SymbolLookupNativeTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SymbolLookupNativeTest {
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
            int jck_is_null(const void *p) { return p == 0; }
            long jck_read_long(const long *p) { return *p; }
            """;

    static Path compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("SymbolLookupNativeTest_jckffm.c");
        Path so = dir.resolve("libSymbolLookupNativeTest_jckffm.so");
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
    static Path SO;

    // libraryLookup fallback chain for platforms where defaultLookup lacks the symbol
    static SymbolLookup libcLookup(Arena arena) {
        SymbolLookup first = LINKER.defaultLookup();
        for (String lib : new String[]{"libc.so.6", "libc.so", "libc.dylib"}) {
            try {
                SymbolLookup l = SymbolLookup.libraryLookup(lib, arena);
                if (l.find("strlen").isPresent()) return first.or(l);
            } catch (IllegalArgumentException notHere) {
                // next platform name
            }
        }
        return first;
    }

    // ---- defaultLookup resolves documented standard C library functions ----
    static void testDefaultLookupResolvesLibc() throws Throwable {
        SymbolLookup dl = LINKER.defaultLookup();
        Optional<MemorySegment> strlen = dl.find("strlen");
        check(strlen.isPresent(), "defaultLookup resolves strlen");
        MethodHandle mh = LINKER.downcallHandle(strlen.orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            check((long) mh.invoke(a.allocateFrom("jck")) == 3L,
                    "strlen resolved by defaultLookup is callable and exact");
        }
    }

    // ---- libraryLookup(String) opens the platform C library explicitly ----
    static void testLibraryLookupString() throws Throwable {
        String found = null;
        for (String lib : new String[]{"libc.so.6", "libc.so", "libc.dylib"}) {
            try (Arena a = Arena.ofConfined()) {
                SymbolLookup lookup = SymbolLookup.libraryLookup(lib, a);
                if (lookup.find("strlen").isPresent()) { found = lib; break; }
            } catch (IllegalArgumentException notHere) {
                // next platform name
            }
        }
        check(found != null, "libraryLookup(String) opens the platform C library");
        try (Arena a = Arena.ofConfined()) {
            SymbolLookup lookup = SymbolLookup.libraryLookup(found, a);
            MemorySegment sym = lookup.findOrThrow("strlen");
            check(sym.byteSize() == 0, "library symbol segment carries a bare address (size 0)");
            check(sym.isNative(), "library symbol segment is native");
            MethodHandle mh = LINKER.downcallHandle(sym,
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
            try (Arena data = Arena.ofConfined()) {
                check((long) mh.invoke(data.allocateFrom("abcde")) == 5L,
                        "strlen from libraryLookup(String) callable and exact");
            }
        }
    }

    // ---- libraryLookup(Path) with an absolute path to our own library ----
    static void testLibraryLookupPath() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            SymbolLookup byPath = SymbolLookup.libraryLookup(SO, a);
            MemorySegment sym = byPath.findOrThrow("jck_is_null");
            MethodHandle isNull = LINKER.downcallHandle(sym,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            check((int) isNull.invoke(MemorySegment.NULL) == 1,
                    "libraryLookup(Path) symbol callable (NULL -> 1)");
            check((int) isNull.invoke(a.allocate(8, 8)) == 0,
                    "libraryLookup(Path) symbol callable (segment -> 0)");
        }
    }

    // ---- loaderLookup exposes libraries loaded via System.load ----
    static void testLoaderLookup() throws Throwable {
        SymbolLookup before = SymbolLookup.loaderLookup();
        check(before.find("jck_is_null").isEmpty(),
                "loaderLookup does not see the library before System.load");
        System.load(SO.toString());
        SymbolLookup after = SymbolLookup.loaderLookup();
        MemorySegment sym = after.findOrThrow("jck_is_null");
        MethodHandle isNull = LINKER.downcallHandle(sym,
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        check((int) isNull.invoke(MemorySegment.NULL) == 1,
                "loaderLookup resolves a System.load-ed library symbol");
        check(after.find("jck_no_such_function_xyz").isEmpty(),
                "loaderLookup misses functions the library does not define");
        // repeated System.load of the same absolute path is idempotent
        System.load(SO.toString());
        check(true, "second System.load of the same library completes without error");
    }

    // ---- a symbol resolves to one stable address ----
    static void testSymbolAddressStability() throws Throwable {
        SymbolLookup dl = LINKER.defaultLookup();
        long a1 = dl.find("strlen").orElseThrow().address();
        long a2 = dl.find("strlen").orElseThrow().address();
        check(a1 == a2, "repeated find() returns the same symbol address");
        try (Arena a = Arena.ofConfined()) {
            SymbolLookup byPath = SymbolLookup.libraryLookup(SO, a);
            long p1 = byPath.find("jck_is_null").orElseThrow().address();
            long p2 = byPath.findOrThrow("jck_is_null").address();
            check(p1 == p2, "libraryLookup find()/findOrThrow() agree on the symbol address");
        }
    }

    // ---- or() composes lookups across sources ----
    static void testOrComposition() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            SymbolLookup byPath = SymbolLookup.libraryLookup(SO, a);
            SymbolLookup combined = LINKER.defaultLookup().or(byPath);
            check(combined.find("strlen").isPresent(), "composed lookup still resolves libc symbols");
            check(combined.find("jck_is_null").isPresent(),
                    "composed lookup resolves symbols from the second lookup");
            check(combined.find("jck_never_defined_anywhere_12345").isEmpty(),
                    "composed lookup misses symbols absent from both");
            // first lookup wins: a symbol present in both comes from the first
            System.load(SO.toString());
            check(SymbolLookup.loaderLookup().or(byPath).find("jck_is_null").isPresent(),
                    "or() resolves a symbol present in both lookups");
        }
    }

    // ---- misses are empty Optionals; findOrThrow carries a precise exception ----
    static void testMissPaths() throws Throwable {
        SymbolLookup dl = LINKER.defaultLookup();
        check(dl.find("jck_never_defined_anywhere_12345").isEmpty(),
                "find() of an undefined symbol is an empty Optional");
        try {
            dl.findOrThrow("jck_never_defined_anywhere_12345");
            check(false, "findOrThrow of an undefined symbol must throw");
        } catch (NoSuchElementException expected) {
            check(true, "findOrThrow of an undefined symbol throws NoSuchElementException");
        }
        try {
            dl.find(null);
            check(false, "find(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "find(null) throws NullPointerException");
        }
        try (Arena a = Arena.ofConfined()) {
            try {
                SymbolLookup.libraryLookup("jck_no_such_library_xyz_987", a);
                check(false, "libraryLookup of a nonexistent name must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "libraryLookup of a nonexistent name throws IllegalArgumentException");
            }
        }
    }

    // ---- a lookup dies with its arena ----
    static void testLookupLifetime() throws Throwable {
        Arena arena = Arena.ofConfined();
        SymbolLookup lookup = SymbolLookup.libraryLookup(SO, arena);
        check(lookup.find("jck_is_null").isPresent(), "lookup alive while its arena is open");
        arena.close();
        try {
            lookup.find("jck_is_null");
            check(false, "find() after arena close must throw");
        } catch (IllegalStateException expected) {
            check(true, "find() after arena close throws IllegalStateException");
        }
        // loaderLookup survives: its lifetime is the class loader, not an arena
        System.load(SO.toString());
        check(SymbolLookup.loaderLookup().find("jck_is_null").isPresent(),
                "loaderLookup unaffected by another lookup's arena closing");
    }

    public static void main(String[] args) throws Throwable {
        if (!gccAvailable()) {
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        LINKER = Linker.nativeLinker();
        SO = compileLib();
        testDefaultLookupResolvesLibc();
        testLibraryLookupString();
        testLibraryLookupPath();
        testLoaderLookup();
        testSymbolAddressStability();
        testOrComposition();
        testMissPaths();
        testLookupLifetime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
