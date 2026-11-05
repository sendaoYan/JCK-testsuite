/*
 * @test
 * @summary java.lang.management.GarbageCollectorMXBean conformance: count/time/name.
 * @run main GarbageCollectorMXBeanTest
 */

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class GarbageCollectorMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGarbageCollectorMXBean() {
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        check(gcs.size() > 0, "has GC beans");
        GarbageCollectorMXBean gc = gcs.get(0);
        check(gc.getName() != null, "getName");
        check(gc.getCollectionCount() >= 0, "getCollectionCount");
        check(gc.getCollectionTime() >= 0, "getCollectionTime");
        check(gc.getMemoryPoolNames() != null, "getMemoryPoolNames");
        check(gc.getObjectName() != null, "getObjectName");
        check(gc.isValid(), "isValid");
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
        testGarbageCollectorMXBean();
        testDeepPerGcContract();
        testDeepGcCountAdvances();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
