/*
 * @test
 * @summary java.lang.foreign.UnionLayout conformance: white-box structure + black-box behavior.
 * @run main UnionLayoutTest
 */
import java.lang.foreign.*;
import java.util.List;

public class UnionLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.UnionLayout");
        check(c.isInterface(), "interface");
        check(GroupLayout.class.isAssignableFrom(c), "extends GroupLayout");
        check(c.getMethod("withName", String.class).getReturnType() == UnionLayout.class, "withName covariant");
        check(c.getMethod("withoutName").getReturnType() == UnionLayout.class, "withoutName covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == UnionLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UnionLayout un = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        List<MemoryLayout> members = un.memberLayouts();
        check(members.size() == 2, "two members");
        check(un.byteSize() == 8, "union byteSize is max member size");
        check(un.byteAlignment() == 8, "union alignment is max member alignment");
        check(un.byteOffset(MemoryLayout.PathElement.groupElement(0)) == 0, "first member offset 0");
        check(un.byteOffset(MemoryLayout.PathElement.groupElement(1)) == 0, "second member offset 0 (overlap)");

        UnionLayout named = un.withName("variant");
        check("variant".equals(named.name().orElse(null)), "withName");
        check(named.withoutName().name().isEmpty(), "withoutName");
        check(named.withByteAlignment(16).byteAlignment() == 16, "withByteAlignment");

        UnionLayout again = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
        check(un.equals(again), "union equals");
        check(un.hashCode() == again.hashCode(), "union hashCode");
        check(!un.toString().isEmpty(), "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
