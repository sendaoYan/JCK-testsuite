/*
 * @test
 * @summary java.lang.foreign.Linker conformance: white-box structure + black-box behavior.
 * @run main/othervm --enable-native-access=ALL-UNNAMED LinkerTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;

public class LinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.Linker");
        check(c.isInterface(), "interface");
        check(c.getMethod("nativeLinker").getReturnType() == Linker.class, "nativeLinker returns Linker");
        check(c.getMethod("downcallHandle", MemorySegment.class, FunctionDescriptor.class, Linker.Option[].class)
                .getReturnType() == MethodHandle.class, "downcallHandle returns MethodHandle");
        check(c.getMethod("downcallHandle", FunctionDescriptor.class, Linker.Option[].class)
                .getReturnType() == MethodHandle.class, "downcallHandle(fd) returns MethodHandle");
        check(c.getMethod("upcallStub", MethodHandle.class, FunctionDescriptor.class, Arena.class, Linker.Option[].class)
                .getReturnType() == MemorySegment.class, "upcallStub returns MemorySegment");
        check(c.getMethod("defaultLookup").getReturnType() == SymbolLookup.class, "defaultLookup returns SymbolLookup");
        check(c.getMethod("canonicalLayouts").getReturnType() == Map.class, "canonicalLayouts returns Map");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Linker l = Linker.nativeLinker();
        check(l != null, "nativeLinker non-null");
        Map<String, MemoryLayout> layouts = l.canonicalLayouts();
        check(!layouts.isEmpty(), "canonicalLayouts non-empty");
        check(layouts.get("int") == ValueLayout.JAVA_INT, "canonical int layout");
        check(layouts.get("long") == ValueLayout.JAVA_LONG, "canonical long layout");
        check(layouts.get("double") == ValueLayout.JAVA_DOUBLE, "canonical double layout");
        check(layouts.get("short") == ValueLayout.JAVA_SHORT, "canonical short layout");
        check(layouts.get("float") == ValueLayout.JAVA_FLOAT, "canonical float layout");
        check(layouts.get("bool") == ValueLayout.JAVA_BOOLEAN, "canonical bool layout");
        check(layouts.get("char") == ValueLayout.JAVA_BYTE, "C char is one byte (JAVA_BYTE)");
        check(layouts.get("int8_t") == ValueLayout.JAVA_BYTE, "int8_t is one byte");
        check(layouts.get("int32_t") == ValueLayout.JAVA_INT, "int32_t is four bytes");
        check(layouts.get("int64_t") == ValueLayout.JAVA_LONG, "int64_t is eight bytes");
        SymbolLookup dl = l.defaultLookup();
        check(dl != null, "defaultLookup non-null");
        check(!dl.find("jck_nonexistent_symbol_xyz_123").isPresent(),
              "defaultLookup does not find nonexistent symbols");
    }

    // ---- Part 3: deep contract (strlen downcall closure, options, upcall lifecycle) ----
    static void testDeepContract() throws Throwable {
        Linker l = Linker.nativeLinker();

        // nativeLinker yields the same instance (measured) with a stable canonical map
        check(Linker.nativeLinker() == l, "nativeLinker returns the same instance");
        Map<String, MemoryLayout> layouts = l.canonicalLayouts();
        check(layouts.containsKey("size_t"), "canonicalLayouts has size_t");
        check(layouts.containsKey("long long"), "canonicalLayouts has long long");
        check(layouts.containsKey("wchar_t"), "canonicalLayouts has wchar_t");
        check(layouts.size() >= 20, "canonicalLayouts covers the C type table (" + layouts.size() + ")");
        check(layouts.get("size_t").byteSize() == 8
                      || layouts.get("size_t").byteSize() == 4,
              "size_t is pointer-width");
        try {
            layouts.put("jck_new", ValueLayout.JAVA_INT);
            check(false, "canonicalLayouts must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "canonicalLayouts.put -> UnsupportedOperationException");
        }

        // downcallHandle(fd) without an address prepends MemorySegment (the call target)
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
        MethodHandle unbound = l.downcallHandle(fd);
        check(unbound.type().parameterCount() == 2
                      && unbound.type().parameterType(0) == MemorySegment.class,
              "unbound downcall takes the function address first");

        // null matrix
        try {
            l.downcallHandle(null, fd);
            check(false, "downcallHandle(null addr, fd) rejected");
        } catch (NullPointerException expected) {
            check(true, "downcallHandle(null, fd) -> NPE");
        }
        try {
            l.downcallHandle(fd, (Linker.Option[]) null);
            check(false, "downcallHandle(fd, null options) rejected");
        } catch (NullPointerException expected) {
            check(true, "downcallHandle(fd, null options) -> NPE");
        }
        try {
            l.upcallStub(null, fd, Arena.ofConfined());
            check(false, "upcallStub(null handle) rejected");
        } catch (NullPointerException expected) {
            check(true, "upcallStub(null, ...) -> NPE");
        }
        try {
            l.upcallStub(MethodHandles.lookup().findStatic(LinkerTest.class, "adder",
                    MethodType.methodType(int.class, int.class, int.class)),
                    null, Arena.ofConfined());
            check(false, "upcallStub(null descriptor) rejected");
        } catch (NullPointerException expected) {
            check(true, "upcallStub(handle, null, ...) -> NPE");
        }

        // Option.critical exists in JDK 27 (isTrivial was renamed); a critical
        // downcall links and runs like a normal one
        check(Linker.Option.critical(true) != null, "Option.critical(true) constructible");
        check(Linker.Option.firstVariadicArg(1) != null, "Option.firstVariadicArg constructible");
        check(Linker.Option.captureCallState("errno") != null, "Option.captureCallState constructible");
        StructLayout csl = Linker.Option.captureStateLayout();
        check(csl != null && csl.byteSize() > 0, "captureStateLayout is a non-empty struct");

        // REAL downcall closure through the process C runtime: strlen via defaultLookup
        java.util.Optional<MemorySegment> strlen = l.defaultLookup().find("strlen");
        if (strlen.isPresent()) {
            MethodHandle h = l.downcallHandle(strlen.get(), fd);
            try (Arena a = Arena.ofConfined()) {
                check((long) h.invoke(a.allocateFrom("hello")) == 5L,
                      "strlen(\"hello\") == 5 via bound downcall (glibc closure)");
                check((long) h.invoke(a.allocateFrom("")) == 0L,
                      "strlen(\"\") == 0");
                MethodHandle crit = l.downcallHandle(strlen.get(), fd, Linker.Option.critical(true));
                check((long) crit.invoke(a.allocateFrom("abcd")) == 4L,
                      "critical(true) downcall also runs strlen(\"abcd\") == 4");
            }
        } else {
            // environment note: defaultLookup resolves the C libraries linked with the
            // runtime; on glibc/musl/msvc runtimes strlen is present (measured true here)
            check(false, "defaultLookup should expose strlen on this platform's C runtime");
        }

        // upcall stub lifecycle: alive in its arena, dead after close (contents access ISE)
        Arena arena = Arena.ofConfined();
        MethodHandle target = MethodHandles.lookup().findStatic(LinkerTest.class, "adder",
                MethodType.methodType(int.class, int.class, int.class));
        MemorySegment stub = l.upcallStub(target,
                FunctionDescriptor.of(ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT), arena);
        check(stub != null && stub.isNative(), "upcall stub is a native segment");
        check(stub.scope().isAlive(), "stub alive while its arena is open");
        arena.close();
        check(!stub.scope().isAlive(), "stub scope dead after arena close");
        // the stub itself is a bare (zero-size) address: geometry stays queryable
        // after close while its scope is dead; passing it to a downcall throws ISE
        // (covered with a real C round-trip in LinkerUpcallTest)
        check(stub.byteSize() == 0, "stub is a zero-length address segment");
        check(stub.address() != 0, "stub address remains non-zero after close");
    }

    public static int adder(int x, int y) { return x + y; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
