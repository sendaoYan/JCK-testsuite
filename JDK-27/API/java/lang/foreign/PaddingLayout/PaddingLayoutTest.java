/*
 * @test
 * @summary java.lang.foreign.PaddingLayout conformance: white-box structure + black-box behavior.
 * @run main PaddingLayoutTest
 */
import java.lang.foreign.*;

public class PaddingLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.PaddingLayout");
        check(c.isInterface(), "interface");
        check(MemoryLayout.class.isAssignableFrom(c), "extends MemoryLayout");
        check(c.getMethod("withName", String.class).getReturnType() == PaddingLayout.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == PaddingLayout.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == PaddingLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PaddingLayout pad = MemoryLayout.paddingLayout(8);
        check(pad.byteSize() == 8, "paddingLayout byteSize 8");
        check(pad.byteAlignment() == 1, "paddingLayout alignment 1");

        PaddingLayout named = pad.withName("pad");
        check("pad".equals(named.name().orElse(null)), "withName sets name");
        check(named.withoutName().name().isEmpty(), "withoutName clears name");

        PaddingLayout aligned = pad.withByteAlignment(8);
        check(aligned.byteAlignment() == 8, "withByteAlignment sets alignment");
        check(pad.byteAlignment() == 1, "original unchanged");

        // padding participates in a struct to align a following long
        StructLayout st = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("i"),
                MemoryLayout.paddingLayout(4), ValueLayout.JAVA_LONG.withName("l"));
        check(st.byteSize() == 16, "padded struct byteSize 16");
        check(st.byteOffset(MemoryLayout.PathElement.groupElement("l")) == 8, "padding aligns following member");
    }

    // ---- Part 3: deep contract (JDK-27 padding validation surface) ----
    static void testDeepContract() {
        // sizes must be strictly positive (JDK-27 measured)
        for (long bad : new long[]{0, -1, -8}) {
            try {
                MemoryLayout.paddingLayout(bad);
                check(false, "paddingLayout(" + bad + ") must be rejected");
            } catch (IllegalArgumentException expected) {
                check(true, "paddingLayout(" + bad + ") -> IAE");
            }
        }
        // padding CAN carry a name in JDK 27 (unlike earlier previews)
        PaddingLayout named = MemoryLayout.paddingLayout(2).withName("gap");
        check("gap".equals(named.name().orElse(null)), "padding can be named");
        check(named.withoutName().name().isEmpty(), "padding withoutName clears it");
        check(named.byteSize() == 2, "naming does not change size");
        // alignment is always 1 and cannot be raised by withByteAlignment? (measured: it can)
        check(MemoryLayout.paddingLayout(8).byteAlignment() == 1, "natural alignment 1");
        PaddingLayout raised = MemoryLayout.paddingLayout(8).withByteAlignment(8);
        check(raised.byteAlignment() == 8, "withByteAlignment(8) raises padding alignment");
        // padding as a group member: tight packing after a byte
        StructLayout packed = MemoryLayout.structLayout(ValueLayout.JAVA_BYTE,
                MemoryLayout.paddingLayout(3), ValueLayout.JAVA_INT);
        check(packed.byteSize() == 8, "B + pad3 + I packs to 8");
        check(packed.byteAlignment() == 4, "padding does not lift group alignment");
        // padding contributes to byteOffset paths by index
        check(packed.byteOffset(MemoryLayout.PathElement.groupElement(2)) == 4,
              "member after padding starts at 4");
        // a pure-padding struct is legal
        StructLayout onlyPad = MemoryLayout.structLayout(MemoryLayout.paddingLayout(16));
        check(onlyPad.byteSize() == 16 && onlyPad.byteAlignment() == 1,
              "struct of one padding keeps alignment 1");
        // equals is value-based on size and name
        check(MemoryLayout.paddingLayout(4).withName("g").equals(
                      MemoryLayout.paddingLayout(4).withName("g")),
              "padding equals by size+name");
        check(!MemoryLayout.paddingLayout(4).equals(MemoryLayout.paddingLayout(8)),
              "different sizes not equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
