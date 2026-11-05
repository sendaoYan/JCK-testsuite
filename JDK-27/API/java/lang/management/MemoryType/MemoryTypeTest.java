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


    static void testDeepEnumContract() {
        check("Heap memory".equals(MemoryType.HEAP.toString()), "HEAP toString display name");
        check("Non-heap memory".equals(MemoryType.NON_HEAP.toString()), "NON_HEAP toString display name");
        check(MemoryType.values()[0] == MemoryType.HEAP, "values()[0] is HEAP (declaration order)");
        check(MemoryType.values()[1] == MemoryType.NON_HEAP, "values()[1] is NON_HEAP");
        MemoryType[] first = MemoryType.values();
        first[0] = MemoryType.NON_HEAP; // caller mutates the returned array
        check(MemoryType.values()[0] == MemoryType.HEAP, "values() returns a fresh array each call");
        check(MemoryType.valueOf("HEAP") == MemoryType.HEAP, "valueOf HEAP identity");
        check(java.lang.Enum.valueOf(MemoryType.class, "NON_HEAP") == MemoryType.NON_HEAP,
              "Enum.valueOf path identity");
        check(MemoryType.HEAP.ordinal() == 0 && MemoryType.NON_HEAP.ordinal() == 1, "ordinals 0,1");
        check(MemoryType.HEAP.compareTo(MemoryType.NON_HEAP) < 0, "HEAP < NON_HEAP");
        check(MemoryType.NON_HEAP.compareTo(MemoryType.HEAP) > 0, "NON_HEAP > HEAP");
        check(MemoryType.HEAP.compareTo(MemoryType.HEAP) == 0, "compareTo self == 0");
        check(MemoryType.HEAP.getDeclaringClass() == MemoryType.class, "getDeclaringClass");
        check(MemoryType.HEAP.hashCode() == MemoryType.HEAP.hashCode(), "hashCode stable (Enum uses identity hash)");
        check(!MemoryType.HEAP.equals(MemoryType.NON_HEAP), "distinct constants not equal");
        check(!MemoryType.HEAP.equals(null), "equals(null) false");
        try {
            MemoryType.valueOf(null);
            check(false, "valueOf(null) should throw");
        } catch (NullPointerException expected) {
            check(true, "valueOf(null) throws NPE");
        }
        try {
            MemoryType.valueOf("bogus");
            check(false, "valueOf(bogus) should throw");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf(bogus) throws IAE");
        }
        try {
            MemoryType.valueOf("heap");
            check(false, "valueOf is case sensitive");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf lowercase rejected");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepEnumContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
