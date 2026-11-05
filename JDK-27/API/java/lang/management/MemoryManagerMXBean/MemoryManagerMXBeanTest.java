/*
 * @test
 * @summary java.lang.management.MemoryManagerMXBean conformance: white-box structure + black-box behavior.
 * @run main MemoryManagerMXBeanTest
 */
import java.lang.management.*;
import java.util.*;

public class MemoryManagerMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryManagerMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("isValid").getReturnType() == boolean.class, "isValid returns boolean");
        check(c.getMethod("getMemoryPoolNames").getReturnType() == String[].class, "getMemoryPoolNames returns String[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(!mgrs.isEmpty(), "memory managers present");
        List<String> names = new ArrayList<>();
        for (MemoryManagerMXBean m : mgrs) {
            check(m.getName() != null && !m.getName().isEmpty(), "manager name non-empty");
            check(m.isValid(), "manager valid");
            check(m.getMemoryPoolNames() != null, "pool names non-null");
            check(m.getObjectName() != null, "objectName non-null");
            names.add(m.getName());
        }
        Collections.sort(names);
        for (int i = 1; i < names.size(); i++) check(!names.get(i).equals(names.get(i - 1)), "manager names distinct");
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
        testWhiteBox();
        testBlackBox();
        testDeepPerManagerContract();
        testDeepGcSubsetOfManagers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
