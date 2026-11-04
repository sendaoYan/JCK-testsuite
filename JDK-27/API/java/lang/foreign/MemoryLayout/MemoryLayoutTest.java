/*
 * @test
 * @summary java.lang.foreign.MemoryLayout conformance: white-box structure + black-box behavior.
 * @run main MemoryLayoutTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

public class MemoryLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.MemoryLayout");
        check(c.isInterface(), "interface");
        check(c.getMethod("byteSize").getReturnType() == long.class, "byteSize returns long");
        check(c.getMethod("byteAlignment").getReturnType() == long.class, "byteAlignment returns long");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == MemoryLayout.class,
              "withByteAlignment returns MemoryLayout");
        check(c.getMethod("scale", long.class, long.class).getReturnType() == long.class, "scale returns long");
        check(c.getMethod("scaleHandle").getReturnType() == MethodHandle.class, "scaleHandle returns MethodHandle");
        check(c.getMethod("byteOffset", MemoryLayout.PathElement[].class).getReturnType() == long.class,
              "byteOffset returns long");
        check(c.getMethod("byteOffsetHandle", MemoryLayout.PathElement[].class).getReturnType() == MethodHandle.class,
              "byteOffsetHandle returns MethodHandle");
        check(c.getMethod("varHandle", MemoryLayout.PathElement[].class).getReturnType() == VarHandle.class,
              "varHandle returns VarHandle");
        check(c.getMethod("arrayElementVarHandle", MemoryLayout.PathElement[].class).getReturnType() == VarHandle.class,
              "arrayElementVarHandle returns VarHandle");
        check(c.getMethod("select", MemoryLayout.PathElement[].class).getReturnType() == MemoryLayout.class,
              "select returns MemoryLayout");
        check(c.getMethod("paddingLayout", long.class).getReturnType() == PaddingLayout.class,
              "paddingLayout returns PaddingLayout");
        check(c.getMethod("sequenceLayout", long.class, MemoryLayout.class).getReturnType() == SequenceLayout.class,
              "sequenceLayout returns SequenceLayout");
        check(c.getMethod("structLayout", MemoryLayout[].class).getReturnType() == StructLayout.class,
              "structLayout returns StructLayout");
        check(c.getMethod("unionLayout", MemoryLayout[].class).getReturnType() == UnionLayout.class,
              "unionLayout returns UnionLayout");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PaddingLayout pad = MemoryLayout.paddingLayout(8);
        check(pad.byteSize() == 8, "paddingLayout byteSize 8");
        check(pad.byteAlignment() == 1, "paddingLayout alignment 1");

        SequenceLayout seq = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
        check(seq.byteSize() == 12, "sequenceLayout byteSize");
        check(seq.byteAlignment() == 4, "sequenceLayout alignment");

        StructLayout st = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("i"), ValueLayout.JAVA_INT.withName("j"),
                ValueLayout.JAVA_LONG.withName("l"));
        check(st.byteSize() == 16, "structLayout byteSize");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("i")) == 0, "first member offset 0");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("j")) == 4, "second member offset 4");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("l")) == 8, "third member offset 8");

        UnionLayout un = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(un.byteSize() == 8, "unionLayout byteSize");

        MemoryLayout named = seq.withName("arr");
        check("arr".equals(named.name().orElse(null)), "withName sets name");
        check(named.withoutName().name().isEmpty(), "withoutName clears name");
        check(named.withByteAlignment(8).byteAlignment() == 8, "withByteAlignment");

        check(seq.scale(0L, 2) == 2 * seq.byteSize(), "scale(0,2) scales by full byteSize");
        try {
            MethodHandle sh = seq.scaleHandle();
            check((long) sh.invokeExact(0L, 2L) == 2 * seq.byteSize(), "scaleHandle roundtrip");
        } catch (Throwable t) { throw new AssertionError(t); }

        MemoryLayout sel = seq.select(MemoryLayout.PathElement.sequenceElement());
        check(sel == ValueLayout.JAVA_INT, "select(sequenceElement) returns element layout");

        VarHandle vh = seq.varHandle(MemoryLayout.PathElement.sequenceElement());
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(seq.byteSize(), seq.byteAlignment());
            vh.set(seg, 0L, 2, 99);
            check((int) vh.get(seg, 0L, 2) == 99, "varHandle set/get");
        }
        VarHandle avh = seq.arrayElementVarHandle(MemoryLayout.PathElement.sequenceElement());
        check(avh != null && avh.varType() == int.class, "arrayElementVarHandle non-null with int varType");

        MemoryLayout copy = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
        check(seq.equals(copy), "equal layouts are equals");
        check(seq.hashCode() == copy.hashCode(), "equal layouts have equal hashCode");
        check(!seq.toString().isEmpty(), "toString non-empty");
        check(!seq.equals(MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT)), "different count not equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
