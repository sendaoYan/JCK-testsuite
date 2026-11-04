/*
 * @test
 * @summary java.lang.management.MemoryUsage conformance: accessors.
 * @run main MemoryUsageTest
 */

import java.lang.management.MemoryUsage;

public class MemoryUsageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryUsage() {
        MemoryUsage mu = new MemoryUsage(100, 50, 100, 100);
        check(mu.getInit() == 100, "getInit");
        check(mu.getUsed() == 50, "getUsed");
        check(mu.getCommitted() == 100, "getCommitted");
        check(mu.getMax() == 100, "getMax");
        check(mu.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testMemoryUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
