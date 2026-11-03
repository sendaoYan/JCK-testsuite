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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
