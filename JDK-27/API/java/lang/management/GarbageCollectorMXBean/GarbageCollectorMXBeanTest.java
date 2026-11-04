/*
 * @test
 * @summary java.lang.management.GarbageCollectorMXBean conformance: white-box structure + black-box behavior.
 * @run main GarbageCollectorMXBeanTest
 */
import java.lang.management.*;
import java.util.*;

public class GarbageCollectorMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.GarbageCollectorMXBean");
        check(c.isInterface(), "interface");
        check(MemoryManagerMXBean.class.isAssignableFrom(c), "extends MemoryManagerMXBean");
        check(c.getMethod("getCollectionCount").getReturnType() == long.class, "getCollectionCount returns long");
        check(c.getMethod("getCollectionTime").getReturnType() == long.class, "getCollectionTime returns long");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        check(!gcs.isEmpty(), "garbage collectors present");
        List<String> names = new ArrayList<>();
        for (GarbageCollectorMXBean g : gcs) {
            check(g.getName() != null && !g.getName().isEmpty(), "GC name non-empty");
            check(g.getCollectionCount() >= 0, "collection count non-negative");
            check(g.getCollectionTime() >= 0, "collection time non-negative");
            check(g.isValid(), "GC valid");
            check(g.getMemoryPoolNames() != null && g.getMemoryPoolNames().length > 0,
                  "GC manages at least one pool");
            check(g.getObjectName() != null, "objectName non-null");
            names.add(g.getName());
        }
        Collections.sort(names);
        for (int i = 1; i < names.size(); i++) check(!names.get(i).equals(names.get(i - 1)), "GC names distinct");
        check(ManagementFactory.getGarbageCollectorMXBeans().size() == gcs.size(), "bean list stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
