/*
 * @test
 * @summary java.lang.foreign.StructLayout conformance: white-box structure + black-box behavior.
 * @run main StructLayoutTest
 */
import java.lang.foreign.*;
import java.util.List;

public class StructLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.StructLayout");
        check(c.isInterface(), "interface");
        check(GroupLayout.class.isAssignableFrom(c), "extends GroupLayout");
        check(c.getMethod("withName", String.class).getReturnType() == StructLayout.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == StructLayout.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == StructLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        StructLayout st = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("i"),
                ValueLayout.JAVA_INT.withName("j"),
                ValueLayout.JAVA_LONG.withName("l"));
        List<MemoryLayout> members = st.memberLayouts();
        check(members.size() == 3, "three members");
        check(st.byteSize() == 16, "struct byteSize 16");
        check(st.byteAlignment() == 8, "struct alignment 8");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("i")) == 0, "member i offset");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("l")) == 8, "member l offset");
        check("i".equals(members.get(0).name().orElse(null)), "member name preserved");

        StructLayout named = st.withName("Point");
        check("Point".equals(named.name().orElse(null)), "withName");
        check(named.withoutName().name().isEmpty(), "withoutName");
        check(named.withByteAlignment(16).byteAlignment() == 16, "withByteAlignment");

        // equality and immutability
        StructLayout again = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("i"),
                ValueLayout.JAVA_INT.withName("j"),
                ValueLayout.JAVA_LONG.withName("l"));
        check(st.equals(again), "struct equals");
        check(st.hashCode() == again.hashCode(), "struct hashCode");
    }

    // ---- Part 3: deep contract (JDK-27 no-padding struct geometry) ----
    static void testDeepContract() {
        // JDK-27: no implicit padding. A misaligned member placement is an error.
        try {
            MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
            check(false, "struct(I,L) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "struct(I,L) -> IAE: no implicit padding in JDK 27");
        }
        // explicit padding restores C layout: i4 + pad4 + j8 == 16
        StructLayout cLike = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                MemoryLayout.paddingLayout(4), ValueLayout.JAVA_LONG);
        check(cLike.byteSize() == 16, "struct(I,pad4,L) size 16");
        check(cLike.memberLayouts().size() == 3, "explicit padding is a member layout");
        check(cLike.byteOffset(MemoryLayout.PathElement.groupElement(1)) == 4,
              "padding member sits at offset 4");
        check(cLike.byteOffset(MemoryLayout.PathElement.groupElement(2)) == 8,
              "long member sits at offset 8 after padding");
        // trailing padding is NOT added: L at 0, I at 8 -> size 12
        StructLayout trailing = MemoryLayout.structLayout(ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT);
        check(trailing.byteSize() == 12, "struct(L,I) size 12 (no trailing padding)");
        check(trailing.byteOffset(MemoryLayout.PathElement.groupElement(1)) == 8,
              "int member follows the long at 8");
        // reducing a member's alignment avoids the padding need entirely
        StructLayout relaxed = MemoryLayout.structLayout(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG.withByteAlignment(4));
        check(relaxed.byteSize() == 12 && relaxed.byteAlignment() == 4,
              "struct(I,L/align4) packs to 12 with alignment 4");
        // byte/int needs 3 bytes of padding to place an int
        try {
            MemoryLayout.structLayout(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT);
            check(false, "struct(B,I) must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "struct(B,I) -> IAE");
        }
        check(MemoryLayout.structLayout(ValueLayout.JAVA_BYTE,
                        MemoryLayout.paddingLayout(3), ValueLayout.JAVA_INT).byteSize() == 8,
              "struct(B,pad3,I) size 8");
        // nested struct reuse: the struct itself is a layout
        StructLayout inner = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("a"));
        StructLayout outer = MemoryLayout.structLayout(inner.withName("in"),
                ValueLayout.JAVA_INT.withName("b"));
        check(outer.byteSize() == 8, "nested struct packs 4+4");
        check(outer.byteOffset(MemoryLayout.PathElement.groupElement("in"),
                               MemoryLayout.PathElement.groupElement("a")) == 0,
              "path into nested struct resolves");
        // member order is preserved in memberLayouts
        check(cLike.memberLayouts().get(0).equals(ValueLayout.JAVA_INT)
                      && cLike.memberLayouts().get(2).equals(ValueLayout.JAVA_LONG),
              "member order preserved");
        // struct geometry drives real segment access
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(cLike);
            check(seg.byteSize() == 16, "allocated struct segment size 16");
            seg.set(ValueLayout.JAVA_LONG,
                    cLike.byteOffset(MemoryLayout.PathElement.groupElement(2)), 42L);
            check(seg.get(ValueLayout.JAVA_LONG, 8) == 42L, "offset math drives access");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
