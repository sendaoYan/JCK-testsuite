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

    public static void main(String[] args) {
        testMemoryType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
