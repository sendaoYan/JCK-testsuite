/*
 * @test
 * @summary java.lang.foreign.Linker real upcall behavior: Java MethodHandles
 *          passed to C as function pointers (qsort comparator via a
 *          self-written callback-driven C function), stub lifecycle and
 *          negative linkage cases.
 * @run main/othervm --enable-native-access=ALL-UNNAMED LinkerUpcallTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class LinkerUpcallTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // Callback-driven C function (self-written): calls f(acc, i) for i in 1..n,
    // accumulates the results and reports the number of invocations through *calls.
    static final String C_SRC = """
            int jck_apply_and_count(int (*f)(int, int), int n, int *calls) {
                int acc = 0;
                *calls = 0;
                for (int i = 1; i <= n; i++) { acc += f(acc, i); (*calls)++; }
                return acc;
            }
            """;

    static Path compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("LinkerUpcallTest_jckffm.c");
        Path so = dir.resolve("libLinkerUpcallTest_jckffm.so");
        Files.writeString(src, C_SRC);
        ProcessBuilder pb = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), src.toString());
        pb.inheritIO();
        Process p = pb.start();
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
    static int cmpCalls;

    // qsort comparator: dereferences both element pointers handed to Java.
    // targetLayout(JAVA_INT) on the ADDRESS layouts is what gives the upcall
    // segments a usable size/shape for dereference.
    static int intComparator(MemorySegment a, MemorySegment b) {
        cmpCalls++;
        return Integer.compare(a.get(ValueLayout.JAVA_INT, 0), b.get(ValueLayout.JAVA_INT, 0));
    }

    static int descendingComparator(MemorySegment a, MemorySegment b) {
        return Integer.compare(b.get(ValueLayout.JAVA_INT, 0), a.get(ValueLayout.JAVA_INT, 0));
    }

    static int adder(int acc, int i) { return acc + i; }

    static MethodHandle qsortHandle(Arena arena) throws Throwable {
        MemorySegment qsort = LINKER.defaultLookup().find("qsort").orElseGet(() -> {
            // documented primary path is defaultLookup; fall back to an explicit
            // libc libraryLookup on platforms where the symbol is not exported
            try {
                return SymbolLookup.libraryLookup("libc.so.6", arena).findOrThrow("qsort");
            } catch (IllegalArgumentException e) {
                throw new AssertionError("cannot resolve qsort: " + e);
            }
        });
        return LINKER.downcallHandle(qsort,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG,
                        ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
    }

    static MemorySegment stubOf(Arena arena, MethodHandle target,
                                FunctionDescriptor desc) {
        return LINKER.upcallStub(target, desc, arena);
    }

    // ---- qsort with an ascending Java comparator ----
    static void testQsortAscending() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment arr = a.allocateFrom(ValueLayout.JAVA_INT,
                    9, 3, 7, 1, 8, 2, 6, 0, 5, 15, 4, 11, 13, 10, 14, 12);
            cmpCalls = 0;
            MemorySegment stub = stubOf(a,
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "intComparator",
                            MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)));
            qsortHandle(a).invoke(arr, 16L, ValueLayout.JAVA_INT.byteSize(), stub);
            int[] out = arr.toArray(ValueLayout.JAVA_INT);
            int[] expect = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
            check(Arrays.equals(out, expect), "qsort ascending produced the exact sorted array");
            check(cmpCalls >= 15, "comparator invoked at least n-1 times (observed " + cmpCalls + ")");
            check(cmpCalls <= 16 * 16, "comparator call count in sane range (observed " + cmpCalls + ")");
        }
    }

    // ---- qsort with a descending comparator and element-size awareness ----
    static void testQsortDescending() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment arr = a.allocateFrom(ValueLayout.JAVA_INT, 4, 2, 9, 1);
            MemorySegment stub = stubOf(a,
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "descendingComparator",
                            MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)));
            qsortHandle(a).invoke(arr, 4L, ValueLayout.JAVA_INT.byteSize(), stub);
            check(Arrays.equals(arr.toArray(ValueLayout.JAVA_INT), new int[]{9, 4, 2, 1}),
                    "qsort descending produced the exact reverse-sorted array");
        }
    }

    // ---- comparator dereferences see the real elements: union of observed values ----
    static void testComparatorSeesElements() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            int[] input = {42, 7, 19};
            MemorySegment arr = a.allocateFrom(ValueLayout.JAVA_INT, input);
            java.util.BitSet seen = new java.util.BitSet();
            MethodHandle observer = MethodHandles.insertArguments(
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "recordingComparator",
                            MethodType.methodType(int.class, java.util.BitSet.class,
                                    MemorySegment.class, MemorySegment.class)),
                    0, seen);
            MemorySegment stub = stubOf(a, observer, FunctionDescriptor.of(ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
                    ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)));
            qsortHandle(a).invoke(arr, 3L, ValueLayout.JAVA_INT.byteSize(), stub);
            check(Arrays.equals(arr.toArray(ValueLayout.JAVA_INT), new int[]{7, 19, 42}),
                    "observing comparator still sorts correctly");
            check(seen.get(42) && seen.get(7) && seen.get(19),
                    "every input value was observed through a dereferenced upcall pointer");
            check(seen.cardinality() == 3, "comparator never saw a value outside the input set");
        }
    }

    static int recordingComparator(java.util.BitSet seen, MemorySegment a, MemorySegment b) {
        seen.set(a.get(ValueLayout.JAVA_INT, 0));
        seen.set(b.get(ValueLayout.JAVA_INT, 0));
        return Integer.compare(a.get(ValueLayout.JAVA_INT, 0), b.get(ValueLayout.JAVA_INT, 0));
    }

    // ---- qsort edge sizes: 0 and 1 elements need no comparison ----
    static void testQsortEdges() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment empty = a.allocate(0, 1);
            MemorySegment stub = stubOf(a,
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "intComparator",
                            MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)));
            cmpCalls = 0;
            qsortHandle(a).invoke(empty, 0L, ValueLayout.JAVA_INT.byteSize(), stub);
            check(cmpCalls == 0, "qsort with 0 elements invokes no comparator (observed " + cmpCalls + ")");
            MemorySegment one = a.allocateFrom(ValueLayout.JAVA_INT, 5);
            qsortHandle(a).invoke(one, 1L, ValueLayout.JAVA_INT.byteSize(), stub);
            check(one.toArray(ValueLayout.JAVA_INT)[0] == 5, "qsort with 1 element leaves it untouched");
        }
    }

    // ---- C calls back into Java n times; values flow both ways ----
    static void testApplyAndCount() throws Throwable {
        MethodHandle apply = LINKER.downcallHandle(OWN.findOrThrow("jck_apply_and_count"),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment stub = stubOf(a,
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "adder",
                            MethodType.methodType(int.class, int.class, int.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
            MemorySegment calls = a.allocate(4, 4);
            // acc_{i} = acc_{i-1} + (acc_{i-1} + i): 1, 4, 11, 26, 57
            int acc = (int) apply.invoke(stub, 5, calls);
            check(acc == 57, "accumulator callback sequence over 5 calls ends at 57, got " + acc);
            check(calls.get(ValueLayout.JAVA_INT, 0) == 5,
                    "C-side invocation count (5) read back from an out-parameter segment");
            int acc0 = (int) apply.invoke(stub, 0, calls);
            check(acc0 == 0 && calls.get(ValueLayout.JAVA_INT, 0) == 0,
                    "callback function invoked zero times for n=0");
        }
    }

    // ---- the same stub can be handed to several downcalls ----
    static void testStubReuse() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MemorySegment stub = stubOf(a,
                    MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "intComparator",
                            MethodType.methodType(int.class, MemorySegment.class, MemorySegment.class)),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT),
                            ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT)));
            MemorySegment first = a.allocateFrom(ValueLayout.JAVA_INT, 2, 1);
            MemorySegment second = a.allocateFrom(ValueLayout.JAVA_INT, 30, 20, 10);
            qsortHandle(a).invoke(first, 2L, ValueLayout.JAVA_INT.byteSize(), stub);
            qsortHandle(a).invoke(second, 3L, ValueLayout.JAVA_INT.byteSize(), stub);
            check(Arrays.equals(first.toArray(ValueLayout.JAVA_INT), new int[]{1, 2}),
                    "stub reusable: first sort correct");
            check(Arrays.equals(second.toArray(ValueLayout.JAVA_INT), new int[]{10, 20, 30}),
                    "stub reusable: second sort correct");
        }
    }

    // ---- stub segments die with their arena ----
    static void testDeadStub() throws Throwable {
        MethodHandle apply = LINKER.downcallHandle(OWN.findOrThrow("jck_apply_and_count"),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        Arena arena = Arena.ofConfined();
        MemorySegment stub = stubOf(arena,
                MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "adder",
                        MethodType.methodType(int.class, int.class, int.class)),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        arena.close();
        try (Arena a = Arena.ofConfined()) {
            try {
                apply.invoke(stub, 1, a.allocate(4, 4));
                check(false, "downcall with a stub from a closed arena must throw");
            } catch (IllegalStateException expected) {
                check(true, "dead stub passed to downcall throws IllegalStateException");
            }
        }
    }

    // ---- upcallStub rejects method handles whose type does not match the descriptor ----
    static void testStubTypeMismatch() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MethodHandle twoInts = MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "adder",
                    MethodType.methodType(int.class, int.class, int.class));
            try {
                stubOf(a, twoInts, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
                check(false, "upcallStub with wrong handle type must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "upcallStub with wrong handle type throws IllegalArgumentException");
            }
            // descriptor with a return but a void handle is likewise rejected
            MethodHandle voidHandle = MethodHandles.lookup().findStatic(LinkerUpcallTest.class, "sink",
                    MethodType.methodType(void.class, int.class));
            try {
                stubOf(a, voidHandle, FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
                check(false, "upcallStub with void handle and non-void descriptor must throw");
            } catch (IllegalArgumentException expected) {
                check(true, "upcallStub with void handle and non-void descriptor throws IllegalArgumentException");
            }
        }
    }

    static void sink(int x) { }

    public static void main(String[] args) throws Throwable {
        LINKER = Linker.nativeLinker();
        System.load(compileLib().toString());
        OWN = SymbolLookup.loaderLookup();
        check(OWN.find("jck_apply_and_count").isPresent(), "callback library symbol visible");

        testQsortAscending();
        testQsortDescending();
        testComparatorSeesElements();
        testQsortEdges();
        testApplyAndCount();
        testStubReuse();
        testDeadStub();
        testStubTypeMismatch();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
