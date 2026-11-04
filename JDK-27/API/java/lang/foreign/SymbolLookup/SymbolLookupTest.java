/*
 * @test
 * @summary java.lang.foreign.SymbolLookup conformance: white-box structure + black-box behavior.
 * @run main SymbolLookupTest
 */
import java.lang.foreign.*;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SymbolLookupTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.SymbolLookup");
        check(c.isInterface(), "interface");
        check(c.getMethod("find", String.class).getReturnType() == Optional.class, "find returns Optional");
        check(c.getMethod("findOrThrow", String.class).getReturnType() == MemorySegment.class,
              "findOrThrow returns MemorySegment");
        check(c.getMethod("or", SymbolLookup.class).getReturnType() == SymbolLookup.class, "or returns SymbolLookup");
        check(c.getMethod("loaderLookup").getReturnType() == SymbolLookup.class, "loaderLookup returns SymbolLookup");
        check(c.getMethod("libraryLookup", String.class, Arena.class).getReturnType() == SymbolLookup.class,
              "libraryLookup(String,Arena) returns SymbolLookup");
        check(c.getMethod("libraryLookup", Path.class, Arena.class).getReturnType() == SymbolLookup.class,
              "libraryLookup(Path,Arena) returns SymbolLookup");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SymbolLookup loader = SymbolLookup.loaderLookup();
        check(loader != null, "loaderLookup non-null");
        Optional<MemorySegment> none = loader.find("jck_nonexistent_symbol_xyz_123");
        check(!none.isPresent(), "find nonexistent is empty");
        try {
            loader.findOrThrow("jck_nonexistent_symbol_xyz_123");
            check(false, "findOrThrow should throw");
        } catch (NoSuchElementException expected) {
            check(true, "findOrThrow throws NoSuchElementException");
        }

        // or() composition: a synthetic lookup contributes a symbol
        try (Arena a = Arena.ofConfined()) {
            MemorySegment marker = a.allocate(0, 1);
            SymbolLookup synthetic = name -> "jck_synthetic_symbol".equals(name)
                    ? Optional.of(marker) : Optional.empty();
            SymbolLookup combined = loader.or(synthetic);
            check(combined.find("jck_synthetic_symbol").isPresent(), "or() finds symbol from second lookup");
            check(combined.find("jck_nonexistent_symbol_xyz_123").isEmpty(), "or() still misses nonexistent");
        }

        // libraryLookup with a bogus library always fails deterministically
        try (Arena a = Arena.ofConfined()) {
            try {
                SymbolLookup bad = SymbolLookup.libraryLookup("jck_no_such_library_xyz", a);
                bad.find("malloc");
                check(false, "bogus libraryLookup should throw");
            } catch (IllegalArgumentException expected) {
                check(true, "bogus libraryLookup throws IllegalArgumentException");
            }
        }

        // best-effort libc symbol existence on common Unix libc names
        try (Arena a = Arena.ofConfined()) {
            boolean opened = false;
            for (String lib : new String[]{"libc.so.6", "libc.so.7", "libc.dylib", "ucrtbase", "msvcrt"}) {
                try {
                    SymbolLookup libc = SymbolLookup.libraryLookup(lib, a);
                    Optional<MemorySegment> malloc = libc.find("malloc");
                    if (malloc.isPresent()) {
                        opened = true;
                        check(malloc.get().byteSize() == 0, "symbol segment has zero size");
                        check(malloc.get().scope().isAlive(), "symbol segment scope alive");
                        break;
                    }
                } catch (IllegalArgumentException ignored) {
                    // not this platform's libc name; try next
                }
            }
            check(opened, "a standard C library exposing malloc was found on this platform");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
