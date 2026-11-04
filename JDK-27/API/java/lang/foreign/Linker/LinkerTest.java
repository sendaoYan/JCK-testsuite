/*
 * @test
 * @summary java.lang.foreign.Linker conformance: white-box structure + black-box behavior.
 * @run main LinkerTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
