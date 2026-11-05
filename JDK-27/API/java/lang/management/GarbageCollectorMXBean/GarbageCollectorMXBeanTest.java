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


    static void testDeepPerGcContract() {
        java.util.List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        check(gcs.size() >= 1, "at least one GC bean");
        java.util.Set<String> poolUniverse = new java.util.HashSet<String>();
        for (java.lang.management.MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            poolUniverse.add(p.getName());
        }
        java.util.Set<String> managerUniverse = new java.util.HashSet<String>();
        for (java.lang.management.MemoryManagerMXBean m : ManagementFactory.getMemoryManagerMXBeans()) {
            managerUniverse.add(m.getName());
        }
        java.util.Set<String> seen = new java.util.HashSet<String>();
        for (GarbageCollectorMXBean g : gcs) {
            check(g.getName() != null && !g.getName().isEmpty(), "GC name non-empty");
            check(seen.add(g.getName()), "GC names unique");
            check(g.isValid(), "GC valid");
            check(g instanceof java.lang.management.MemoryManagerMXBean, "GC bean is also a MemoryManagerMXBean");
            check(g.getCollectionCount() >= 0, "collection count >= 0");
            check(g.getCollectionTime() >= 0, "collection time >= 0");
            check(managerUniverse.contains(g.getName()), "GC listed among memory managers");
            check(("java.lang:name=" + g.getName() + ",type=GarbageCollector")
                    .equals(g.getObjectName().getCanonicalName()),
                  "GC ObjectName pattern exact for " + g.getName() + ": " + g.getObjectName());
            String[] pools = g.getMemoryPoolNames();
            check(pools != null && pools.length >= 1, "GC manages >= 1 pool");
            for (String pn : pools) {
                check(poolUniverse.contains(pn), "GC pool " + pn + " exists");
            }
        }
    }

    static void testDeepGcCountAdvances() {
        long before = 0;
        for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
            before += Math.max(g.getCollectionCount(), 0);
        }
        ManagementFactory.getMemoryMXBean().gc(); // System.gc()
        long after = 0;
        for (GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
            after += Math.max(g.getCollectionCount(), 0);
        }
        check(after >= before, "collection counts never decrease");
        check(after > before, "MemoryMXBean.gc() advances the total collection count");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepPerGcContract();
        testDeepGcCountAdvances();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
