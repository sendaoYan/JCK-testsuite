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

    // ---- Part 3: deep contract (JDK-27 alignment rules, validation matrix, paths) ----
    static void testStructAlignmentRules() {
        // JDK-27 measured rule set (differs from pre-24 FFM):
        // structLayout does NOT insert implicit padding; every member must land on
        // its own byteAlignment, otherwise IllegalArgumentException.
        try {
            MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
            check(false, "struct(int,long) must be rejected (long would sit at offset 4)");
        } catch (IllegalArgumentException expected) {
            check(true, "struct(I,L) -> IAE (no implicit padding in JDK 27)");
        }
        try {
            MemoryLayout.structLayout(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
            check(false, "struct(byte,int) must be rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "struct(B,I) -> IAE");
        }
        // legal shapes: explicit padding, or a member with reduced alignment
        StructLayout padded = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                MemoryLayout.paddingLayout(4), ValueLayout.JAVA_LONG);
        check(padded.byteSize() == 16 && padded.byteAlignment() == 8,
              "struct(I,pad4,L) size 16 align 8");
        check(padded.memberLayouts().size() == 3
                      && padded.memberLayouts().get(1) instanceof PaddingLayout,
              "explicit padding appears as a member layout");
        StructLayout relaxed = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG.withByteAlignment(4));
        check(relaxed.byteSize() == 12 && relaxed.byteAlignment() == 4,
              "struct(I,long/align4) size 12 align 4 (no trailing padding either)");
        // trailing padding is NOT appended: struct(long,int) stays 12 bytes
        StructLayout trailing = MemoryLayout.structLayout(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);
        check(trailing.byteSize() == 12, "struct(L,I) size 12: no trailing padding");
        check(trailing.byteAlignment() == 8, "struct(L,I) alignment is max member alignment");
        // empty groups are legal zero-sized layouts
        check(MemoryLayout.structLayout().byteSize() == 0, "empty struct allowed, size 0");
        check(MemoryLayout.unionLayout().byteSize() == 0, "empty union allowed, size 0");
        // mixed struct where a nested sequence does not force padding
        StructLayout mixed = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_BYTE));
        check(mixed.byteSize() == 7, "struct(I,seq(3,B)) size 7: members pack tightly");
    }

    static void testValidationMatrix() {
        // alignment must be a positive power of two
        for (long bad : new long[]{0, 3, -8}) {
            try {
                ValueLayout.JAVA_INT.withByteAlignment(bad);
                check(false, "withByteAlignment(" + bad + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "withByteAlignment(" + bad + ") -> IAE");
            }
        }
        check(ValueLayout.JAVA_INT.withByteAlignment(16).byteAlignment() == 16,
              "withByteAlignment(16) above natural alignment is legal");
        // padding sizes must be positive
        try { MemoryLayout.paddingLayout(0); check(false, "paddingLayout(0) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "paddingLayout(0) -> IAE"); }
        try { MemoryLayout.paddingLayout(-1); check(false, "paddingLayout(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "paddingLayout(-1) -> IAE"); }
        // padding layouts may carry a name (measured) and always have alignment 1
        PaddingLayout named = MemoryLayout.paddingLayout(4).withName("gap");
        check("gap".equals(named.name().orElse(null)), "padding can be named");
        check(MemoryLayout.paddingLayout(8).byteAlignment() == 1, "padding alignment 1");
        // JDK-27 removed variable-length sequences: negative counts are rejected outright
        try { MemoryLayout.sequenceLayout(-1, ValueLayout.JAVA_INT); check(false, "seq(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "sequenceLayout(-1) -> IAE (varlen removed)"); }
        try { MemoryLayout.sequenceLayout(-2, ValueLayout.JAVA_INT); check(false, "seq(-2) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "sequenceLayout(-2) -> IAE"); }
        try { MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_INT).withElementCount(-1); check(false, "wEC(-1) rejected"); }
        catch (IllegalArgumentException expected) { check(true, "withElementCount(-1) -> IAE"); }
        // zero-count sequence is legal with size 0
        check(MemoryLayout.sequenceLayout(0, ValueLayout.JAVA_INT).byteSize() == 0,
              "seq(0) size 0");
    }

    static void testPathArithmetic() {
        StructLayout st = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("i"),
                ValueLayout.JAVA_INT.withName("j"),
                ValueLayout.JAVA_LONG.withName("l"),
                MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_BYTE).withName("tail"));
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("i")) == 0, "offset i == 0");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("j")) == 4, "offset j == 4");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("l")) == 8, "offset l == 8");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("tail")) == 16,
              "offset tail == 16");
        // nested sequenceElement(i) index math: base + i * elementSize
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("tail"),
                            MemoryLayout.PathElement.sequenceElement(3)) == 19,
              "tail[3] at byte 19");
        // select walks the same paths; the selected member keeps its name
        check(st.select(MemoryLayout.PathElement.groupElement("l"))
                      .equals(ValueLayout.JAVA_LONG.withName("l")),
              "select(groupElement l) returns the named member layout");
        check("l".equals(st.select(MemoryLayout.PathElement.groupElement("l")).name().orElse(null)),
              "selected member keeps its name");
        check(st.select(MemoryLayout.PathElement.groupElement("tail"))
                      .equals(MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_BYTE).withName("tail")),
              "select nested sequence by value (name retained)");
        // unknown names fail with IllegalArgumentException
        try {
            st.byteOffset(MemoryLayout.PathElement.groupElement("nope"));
            check(false, "unknown path element rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "byteOffset(unknown name) -> IAE");
        }
        // scale() multiplies by the layout's own byteSize
        check(st.scale(7L, 3L) == 7L + 3L * st.byteSize(), "scale(base,factor) arithmetic");
    }

    static void testDeepContract() {
        testStructAlignmentRules();
        testValidationMatrix();
        testPathArithmetic();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
