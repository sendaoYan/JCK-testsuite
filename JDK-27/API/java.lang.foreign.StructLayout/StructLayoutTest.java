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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
