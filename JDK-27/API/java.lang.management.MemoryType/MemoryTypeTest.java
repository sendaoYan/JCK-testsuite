/*
 * @test
 * @summary java.lang.management.MemoryType conformance: white-box structure + black-box behavior.
 * @run main MemoryTypeTest
 */
import java.lang.management.MemoryType;

public class MemoryTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryType");
        check(c.isEnum(), "enum");
        check(Enum.class.isAssignableFrom(c), "extends Enum");
        check(c.getEnumConstants().length == 2, "two enum constants");
        check(c.getMethod("values").getReturnType() == MemoryType[].class, "values returns MemoryType[]");
        check(c.getMethod("valueOf", String.class).getReturnType() == MemoryType.class, "valueOf returns MemoryType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        MemoryType[] all = MemoryType.values();
        check(all.length == 2, "values length 2");
        check(MemoryType.valueOf("HEAP") == MemoryType.HEAP, "valueOf HEAP");
        check(MemoryType.valueOf("NON_HEAP") == MemoryType.NON_HEAP, "valueOf NON_HEAP");
        check("HEAP".equals(MemoryType.HEAP.name()), "HEAP name");
        check("NON_HEAP".equals(MemoryType.NON_HEAP.name()), "NON_HEAP name");
        check("Heap memory".equals(MemoryType.HEAP.toString()), "HEAP toString");
        check("Non-heap memory".equals(MemoryType.NON_HEAP.toString()), "NON_HEAP toString");
        check(MemoryType.HEAP.ordinal() == 0, "HEAP ordinal 0");
        check(MemoryType.NON_HEAP.ordinal() == 1, "NON_HEAP ordinal 1");
        try {
            MemoryType.valueOf("BOGUS");
            check(false, "valueOf bogus should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf bogus throws IllegalArgumentException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
