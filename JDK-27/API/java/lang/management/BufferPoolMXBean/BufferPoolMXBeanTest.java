/*
 * @test
 * @summary java.lang.management.BufferPoolMXBean conformance: white-box structure + black-box behavior.
 * @run main BufferPoolMXBeanTest
 */
import java.lang.management.*;
import java.util.List;

public class BufferPoolMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.BufferPoolMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("getCount").getReturnType() == long.class, "getCount returns long");
        check(c.getMethod("getTotalCapacity").getReturnType() == long.class, "getTotalCapacity returns long");
        check(c.getMethod("getMemoryUsed").getReturnType() == long.class, "getMemoryUsed returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        check(!pools.isEmpty(), "buffer pools present");
        boolean sawDirect = false;
        for (BufferPoolMXBean b : pools) {
            check(b.getName() != null && !b.getName().isEmpty(), "pool name non-empty");
            check(b.getCount() >= 0, "count non-negative");
            check(b.getTotalCapacity() >= 0, "capacity non-negative");
            check(b.getMemoryUsed() >= 0, "used non-negative");
            check(b.getObjectName() != null, "objectName non-null");
            if (b.getName().toLowerCase(java.util.Locale.ROOT).contains("direct")) sawDirect = true;
        }
        check(sawDirect, "direct buffer pool present");
    }


    static void testDeepPoolNamesAndCounts() {
        java.util.List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        check(pools.size() >= 2, "at least 'direct' and 'mapped' pools, got " + pools.size());
        boolean sawDirect = false;
        boolean sawMapped = false;
        for (BufferPoolMXBean p : pools) {
            check(p.getCount() >= 0 && p.getTotalCapacity() >= 0 && p.getMemoryUsed() >= 0,
                  "count/capacity/used non-negative for " + p.getName());
            // getCanonicalName() sorts keys alphabetically: name comes before type
            check(("java.nio:name=" + p.getName() + ",type=BufferPool").equals(p.getObjectName().getCanonicalName()),
                  "BufferPool ObjectName canonical form exact for " + p.getName() + ": " + p.getObjectName().getCanonicalName());
            check(p.getObjectName().getDomain().equals("java.nio"), "domain java.nio");
            if (p.getName().equals("direct")) {
                sawDirect = true;
            }
            if (p.getName().equals("mapped")) {
                sawMapped = true;
            }
        }
        check(sawDirect, "'direct' pool present");
        check(sawMapped, "'mapped' pool present (27 adds a non-volatile variant)");
    }

    static void testDeepDirectAllocationAccounting() {
        BufferPoolMXBean direct = null;
        for (BufferPoolMXBean p : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            if (p.getName().equals("direct")) {
                direct = p;
            }
        }
        check(direct != null, "direct pool located");
        long countBefore = direct.getCount();
        long usedBefore = direct.getMemoryUsed();
        long capBefore = direct.getTotalCapacity();
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(1024);
        check(direct.getCount() == countBefore + 1, "allocateDirect bumps getCount by exactly 1");
        check(direct.getMemoryUsed() >= usedBefore + 1024, "memoryUsed accounts the 1024 bytes");
        check(direct.getTotalCapacity() >= capBefore + 1024, "totalCapacity accounts the 1024 bytes");
        check(buf.remaining() == 1024, "buffer usable");
    }

    static void testDeepListStable() {
        java.util.List<BufferPoolMXBean> a = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        java.util.List<BufferPoolMXBean> b = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        check(a.size() == b.size(), "pool list size stable across calls");
        for (int i = 0; i < a.size(); i++) {
            check(a.get(i).getName().equals(b.get(i).getName()), "pool identity/order stable");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepPoolNamesAndCounts();
        testDeepDirectAllocationAccounting();
        testDeepListStable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
