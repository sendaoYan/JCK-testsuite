/*
 * @test
 * @summary java.lang.foreign.GroupLayout conformance: white-box structure + black-box behavior.
 * @run main GroupLayoutTest
 */
import java.lang.foreign.*;
import java.util.List;

public class GroupLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.GroupLayout");
        check(c.isInterface(), "interface");
        check(MemoryLayout.class.isAssignableFrom(c), "extends MemoryLayout");
        check(c.getMethod("memberLayouts").getReturnType() == List.class, "memberLayouts returns List");
        check(c.getMethod("withName", String.class).getReturnType() == GroupLayout.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == GroupLayout.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == GroupLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        GroupLayout struct = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("i"),
                ValueLayout.JAVA_INT.withName("j"), ValueLayout.JAVA_LONG.withName("l"));
        check(struct instanceof StructLayout, "structLayout is a GroupLayout");
        check(struct.memberLayouts().size() == 3, "struct has three members");
        check(struct.byteSize() == 16, "struct byteSize 16");
        GroupLayout named = struct.withName("point");
        check("point".equals(named.name().orElse(null)), "withName sets name");
        check(named.withoutName().name().isEmpty(), "withoutName clears name");

        GroupLayout union = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(union instanceof UnionLayout, "unionLayout is a GroupLayout");
        check(union.memberLayouts().size() == 2, "union has two members");
        check(union.byteSize() == 8, "union byteSize is max member size");

        GroupLayout aligned = union.withByteAlignment(16);
        check(aligned.byteAlignment() == 16, "withByteAlignment sets alignment");
        check(union.byteAlignment() == 8, "original union alignment unchanged");
    }

    // ---- Part 3: deep contract (group construction rules) ----
    static void testDeepContract() {
        // group alignment is the max member alignment; no implicit padding (JDK-27)
        GroupLayout mixed = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_BYTE));
        check(mixed.byteAlignment() == 4, "alignment is max member alignment (4)");
        check(mixed.byteSize() == 7, "tight packing: 4 + 3, no trailing padding");
        // padding members are visible in memberLayouts and count toward size
        GroupLayout padded = MemoryLayout.structLayout(ValueLayout.JAVA_BYTE,
                MemoryLayout.paddingLayout(3), ValueLayout.JAVA_INT);
        check(padded.byteSize() == 8 && padded.memberLayouts().size() == 3,
              "padding member visible in memberLayouts");
        check(padded.memberLayouts().get(1) instanceof PaddingLayout,
              "middle member is a PaddingLayout");
        // a struct member with lowered alignment reduces the group alignment
        GroupLayout relaxed = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG.withByteAlignment(4));
        check(relaxed.byteAlignment() == 4, "member withByteAlignment lowers group alignment");
        // both group families implement GroupLayout; empty groups are allowed
        check(MemoryLayout.structLayout() instanceof GroupLayout
                      && MemoryLayout.unionLayout() instanceof GroupLayout,
              "empty struct and union are still GroupLayouts");
        check(MemoryLayout.structLayout().byteSize() == 0
                      && MemoryLayout.unionLayout().byteSize() == 0,
              "empty groups have zero size");
        // union member order is preserved and each overlays at 0
        GroupLayout union = MemoryLayout.unionLayout(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
        check(union.memberLayouts().get(0).byteSize() == 1
                      && union.memberLayouts().get(1).byteSize() == 4,
              "union member order preserved by size");
        // equals is value-based over members, order-sensitive (both shapes valid,
        // both 12 bytes: struct(L,I) vs struct(I, L relaxed to align 4)
        check(!MemoryLayout.structLayout(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
                       .equals(MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                               ValueLayout.JAVA_LONG.withByteAlignment(4))),
              "member order participates in equals");
        // memberLayouts returns an unmodifiable snapshot (measured)
        try {
            padded.memberLayouts().add(ValueLayout.JAVA_BYTE);
            check(false, "memberLayouts must be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            check(true, "memberLayouts.add -> UnsupportedOperationException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
