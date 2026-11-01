/*
 * @test
 * @summary java.lang.management.MemoryMXBean conformance: heap/non-heap usage and gc.
 * @run main MemoryMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class MemoryMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryMXBean() {
        MemoryMXBean mmx = ManagementFactory.getMemoryMXBean();
        check(mmx.getHeapMemoryUsage() != null, "getHeapMemoryUsage");
        check(mmx.getNonHeapMemoryUsage() != null, "getNonHeapMemoryUsage");
        check(mmx.getObjectPendingFinalizationCount() >= 0, "getObjectPendingFinalizationCount");
        check(mmx.getObjectName() != null, "getObjectName");
        boolean vb = mmx.isVerbose();
        mmx.setVerbose(vb);
        mmx.gc();
        check(true, "gc/setVerbose callable");
    }

    public static void main(String[] args) {
        testMemoryMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
