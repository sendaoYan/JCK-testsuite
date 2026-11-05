/*
 * @test
 * @summary java.lang.management.MemoryManagerMXBean conformance: name/pools/validity.
 * @run main MemoryManagerMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.List;

public class MemoryManagerMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryManagerMXBean() {
        List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(mgrs.size() > 0, "has managers");
        MemoryManagerMXBean mgr = mgrs.get(0);
        check(mgr.getName() != null, "getName");
        check(mgr.getMemoryPoolNames() != null, "getMemoryPoolNames");
        check(mgr.getObjectName() != null, "getObjectName");
        check(mgr.isValid(), "isValid");
    }


    static void testDeepPerManagerContract() {
        java.util.List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        java.util.Set<String> poolUniverse = new java.util.HashSet<String>();
        for (java.lang.management.MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            poolUniverse.add(p.getName());
        }
        java.util.Set<String> seen = new java.util.HashSet<String>();
        int nonGcManagers = 0;
        for (MemoryManagerMXBean m : mgrs) {
            check(m.getName() != null && !m.getName().isEmpty(), "manager name non-empty");
            check(seen.add(m.getName()), "manager names unique");
            check(m.isValid(), "manager valid");
            String[] pools = m.getMemoryPoolNames();
            check(pools != null, "pool names array non-null");
            for (String pn : pools) {
                check(poolUniverse.contains(pn),
                      "manager " + m.getName() + " references existing pool " + pn);
            }
            if (!(m instanceof java.lang.management.GarbageCollectorMXBean)) {
                nonGcManagers++;
            } else {
                check(m.getObjectName().getKeyProperty("type").equals("GarbageCollector"),
                      "GC manager keeps type=GarbageCollector");
            }
        }
        check(nonGcManagers >= 1, "at least one non-GC manager exists (code cache / metaspace)");
    }

    static void testDeepGcSubsetOfManagers() {
        java.util.Set<String> managerNames = new java.util.HashSet<String>();
        for (MemoryManagerMXBean m : ManagementFactory.getMemoryManagerMXBeans()) {
            managerNames.add(m.getName());
        }
        for (java.lang.management.GarbageCollectorMXBean g : ManagementFactory.getGarbageCollectorMXBeans()) {
            check(managerNames.contains(g.getName()), "GC " + g.getName() + " is in the manager list");
        }
        check(ManagementFactory.getMemoryManagerMXBeans().size()
                >= ManagementFactory.getGarbageCollectorMXBeans().size(),
              "manager list is a superset of the GC list");
    }

    public static void main(String[] args) throws Exception {
        testMemoryManagerMXBean();
        testDeepPerManagerContract();
        testDeepGcSubsetOfManagers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
