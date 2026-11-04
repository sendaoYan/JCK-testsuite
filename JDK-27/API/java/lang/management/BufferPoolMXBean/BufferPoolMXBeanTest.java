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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
