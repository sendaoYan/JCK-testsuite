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

    // ---- Part 3: deep contract (union geometry + overlay semantics) ----
    static void testDeepContract() {
        // all members overlay at offset 0; size and alignment are the member maxima
        UnionLayout un = MemoryLayout.unionLayout(ValueLayout.JAVA_BYTE, ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG, ValueLayout.JAVA_DOUBLE);
        check(un.byteSize() == 8, "size is the largest member (8)");
        check(un.byteAlignment() == 8, "alignment is the largest member alignment");
        for (int i = 0; i < un.memberLayouts().size(); i++)
            check(un.byteOffset(MemoryLayout.PathElement.groupElement(i)) == 0,
                  "member " + i + " sits at offset 0");
        // member order preserved
        check(un.memberLayouts().get(0).equals(ValueLayout.JAVA_BYTE)
                      && un.memberLayouts().get(3).equals(ValueLayout.JAVA_DOUBLE),
              "member order preserved");
        // smaller member does not grow the union
        UnionLayout grown = MemoryLayout.unionLayout(ValueLayout.JAVA_LONG,
                MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
        check(grown.byteSize() == 8, "8-byte struct member keeps union size 8");
        // empty union is legal (JDK-27 measured)
        check(MemoryLayout.unionLayout().byteSize() == 0, "empty union size 0");
        // alignment of union governs allocate
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(un);
            check(seg.byteSize() == 8, "allocated union segment is 8 bytes");
            check(seg.address() % 8 == 0, "union allocation honors alignment 8");
        }
        // overlay access: writing the smallest member keeps the rest of the bytes
        try (Arena a = Arena.ofConfined()) {
            MemorySegment seg = a.allocate(un);
            seg.set(ValueLayout.JAVA_LONG, 0, -1L);       // all 0xFF
            seg.set(ValueLayout.JAVA_BYTE, 0, (byte) 0);  // overlay the first byte only
            check(seg.get(ValueLayout.JAVA_BYTE, 0) == 0, "overlaid byte reads back 0");
            check(seg.get(ValueLayout.JAVA_BYTE, 1) == (byte) 0xFF
                          && seg.get(ValueLayout.JAVA_BYTE, 7) == (byte) 0xFF,
                  "overlay write leaves the other members' bytes intact");
        }
        // withName keeps the union a union (covariant chain usable fluently)
        UnionLayout named = un.withName("u").withoutName();
        check(named.name().isEmpty() && named.byteSize() == 8, "withName/withoutName covariant chain");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
