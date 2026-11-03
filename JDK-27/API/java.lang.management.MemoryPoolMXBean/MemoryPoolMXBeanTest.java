/*
 * @test
 * @summary java.lang.management.MemoryPoolMXBean conformance: white-box structure + black-box behavior.
 * @run main MemoryPoolMXBeanTest
 */
import java.lang.management.*;
import java.util.*;

public class MemoryPoolMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryPoolMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("getType").getReturnType() == MemoryType.class, "getType returns MemoryType");
        check(c.getMethod("getUsage").getReturnType() == MemoryUsage.class, "getUsage returns MemoryUsage");
        check(c.getMethod("getPeakUsage").getReturnType() == MemoryUsage.class, "getPeakUsage returns MemoryUsage");
        check(c.getMethod("resetPeakUsage").getReturnType() == void.class, "resetPeakUsage returns void");
        check(c.getMethod("isUsageThresholdSupported").getReturnType() == boolean.class,
              "isUsageThresholdSupported returns boolean");
        check(c.getMethod("getUsageThreshold").getReturnType() == long.class, "getUsageThreshold returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        check(!pools.isEmpty(), "memory pools present");
        boolean sawHeap = false, sawNonHeap = false;
        for (MemoryPoolMXBean p : pools) {
            check(p.getName() != null && !p.getName().isEmpty(), "pool name non-empty");
            MemoryType t = p.getType();
            check(t == MemoryType.HEAP || t == MemoryType.NON_HEAP, "type is HEAP or NON_HEAP");
            if (t == MemoryType.HEAP) sawHeap = true; else sawNonHeap = true;
            MemoryUsage usage = p.getUsage();
            check(usage != null && usage.getUsed() >= 0, "usage used non-negative");
            check(p.getPeakUsage() != null && p.getPeakUsage().getUsed() >= 0, "peak usage used non-negative");
            check(p.getMemoryManagerNames() != null, "manager names non-null");
            check(p.isValid(), "pool valid");
            check(p.getObjectName() != null, "objectName non-null");
            if (p.isUsageThresholdSupported()) {
                check(p.getUsageThreshold() >= 0, "usage threshold non-negative when supported");
                check(p.getUsageThresholdCount() >= 0, "usage threshold count non-negative");
            }
            if (p.isCollectionUsageThresholdSupported()) {
                check(p.getCollectionUsageThreshold() >= 0, "collection threshold non-negative when supported");
                check(p.getCollectionUsageThresholdCount() >= 0, "collection threshold count non-negative");
            }
        }
        check(sawHeap && sawNonHeap, "both heap and non-heap pools present");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
