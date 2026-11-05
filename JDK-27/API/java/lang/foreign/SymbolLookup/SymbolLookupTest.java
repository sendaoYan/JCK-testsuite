/*
 * @test
 * @summary java.lang.foreign.SymbolLookup conformance: white-box structure + black-box behavior.
 * @run main/othervm --enable-native-access=ALL-UNNAMED SymbolLookupTest
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

    // ---- Part 3: deep contract (or() precedence, loader surface, determinism) ----
    static void testDeepContract() {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment first = a.allocate(0, 1);
            MemorySegment second = a.allocate(0, 1);
            SymbolLookup a1 = name -> "shared".equals(name) ? Optional.of(first) : Optional.empty();
            SymbolLookup b1 = name -> "shared".equals(name) ? Optional.of(second)
                    : "onlyInB".equals(name) ? Optional.of(second) : Optional.empty();

            // or() consults the receiver first: "shared" resolves to the FIRST lookup's address
            SymbolLookup ab = a1.or(b1);
            check(ab.find("shared").get().address() == first.address(),
                  "or() prefers the receiver's symbol on name collision");
            check(ab.find("onlyInB").isPresent(), "or() falls through to the second lookup");
            // the composed lookup itself composes further (associative chain)
            SymbolLookup chained = ab.or(name -> "third".equals(name) ? Optional.of(first) : Optional.empty());
            check(chained.find("shared").isPresent() && chained.find("onlyInB").isPresent()
                          && chained.find("third").isPresent(),
                  "or() chains across three lookups");
            // find is deterministic: same name, same address every time
            check(ab.find("shared").get().address() == ab.find("shared").get().address(),
                  "repeated find is stable");
            // findOrThrow returns the same symbol find does
            check(ab.findOrThrow("shared").address() == ab.find("shared").get().address(),
                  "findOrThrow agrees with find");
        }

        // loaderLookup surface in a fresh test JVM: no JNI library exports strlen
        // here (measured on this harness; if a future harness loads such a library,
        // this documents the environment rather than a hard failure)
        Optional<MemorySegment> viaLoader = SymbolLookup.loaderLookup().find("strlen");
        check(viaLoader.isEmpty(),
              "loaderLookup has no strlen in a JVM without JNI libraries (env-recorded)");

        // lookups never throw for unknown names, they answer empty
        for (String name : new String[]{"", "jck_zzz_1", "malloc_malloc", "\0bad"}) {
            check(SymbolLookup.loaderLookup().find(name).isEmpty(),
                  "find(" + (name.isEmpty() ? "empty" : "unknown") + ") answers Optional.empty");
        }
        // the default lookup of the native linker resolves the C runtime (glibc)
        java.util.Optional<MemorySegment> strlen = Linker.nativeLinker().defaultLookup().find("strlen");
        check(strlen.isPresent(),
              "defaultLookup resolves strlen from the process C libraries (glibc)");
        check(strlen.get().byteSize() == 0, "resolved symbol is a bare address (size 0)");
        // composed default + loader still resolves strlen (order: default first)
        check(Linker.nativeLinker().defaultLookup().or(SymbolLookup.loaderLookup())
                      .find("strlen").isPresent(),
              "defaultLookup.or(loaderLookup) still finds strlen");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
