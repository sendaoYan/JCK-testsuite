/*
 * @test
 * @summary java.lang.management.MemoryType conformance: HEAP/NON_HEAP enum.
 * @run main MemoryTypeTest
 */

import java.lang.management.MemoryType;

public class MemoryTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryType() {
        check("HEAP".equals(MemoryType.HEAP.name()), "HEAP");
        check("NON_HEAP".equals(MemoryType.NON_HEAP.name()), "NON_HEAP");
        check(MemoryType.valueOf("HEAP") == MemoryType.HEAP, "valueOf HEAP");
        check(MemoryType.values().length == 2, "two values");
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
        testMemoryType();
        testDeepEnumContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
