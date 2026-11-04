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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
