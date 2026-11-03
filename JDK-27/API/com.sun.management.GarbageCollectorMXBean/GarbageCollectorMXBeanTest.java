/*
 * @test
 * @summary com.sun.management.GarbageCollectorMXBean conformance: white-box structure + black-box behavior.
 * @run main GarbageCollectorMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

public class GarbageCollectorMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.GarbageCollectorMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.lang.management.GarbageCollectorMXBean"), "extends java GC MXBean");
        check(methodNames(c).contains("getLastGcInfo"), "getLastGcInfo");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        List<java.lang.management.GarbageCollectorMXBean> beans =
            ManagementFactory.getGarbageCollectorMXBeans();
        check(beans != null && !beans.isEmpty(), "at least one GC MXBean");

        // Trigger an explicit GC so getLastGcInfo() is likely populated. This is
        // only a hint; the per-bean assertions below tolerate a null GcInfo.
        System.gc();

        boolean sawComSunBean = false;
        boolean sawNonNullGcInfo = false;
        for (java.lang.management.GarbageCollectorMXBean b : beans) {
            check(b.getName() != null, "GC bean name non-null");
            check(b.getCollectionCount() >= 0, "collectionCount >= 0");
            check(b.getCollectionTime() >= 0, "collectionTime >= 0");
            if (b instanceof com.sun.management.GarbageCollectorMXBean) {
                sawComSunBean = true;
                com.sun.management.GcInfo gi =
                    ((com.sun.management.GarbageCollectorMXBean) b).getLastGcInfo();
                if (gi != null) {
                    sawNonNullGcInfo = true;
                    check(gi.getId() >= 0, "GcInfo id >= 0");
                    check(gi.getStartTime() >= 0, "GcInfo startTime >= 0");
                    check(gi.getEndTime() >= gi.getStartTime(), "GcInfo endTime >= startTime");
                    check(gi.getDuration() >= 0, "GcInfo duration >= 0");
                    Map<String, MemoryUsage> before = gi.getMemoryUsageBeforeGc();
                    Map<String, MemoryUsage> after = gi.getMemoryUsageAfterGc();
                    check(before != null, "memoryUsageBeforeGc non-null");
                    check(after != null, "memoryUsageAfterGc non-null");
                    for (Map.Entry<String, MemoryUsage> e : before.entrySet()) {
                        check(e.getKey() != null && e.getValue() != null, "before entry non-null");
                        check(after.get(e.getKey()) != null, "after has entry for " + e.getKey());
                    }
                }
            }
        }
        check(sawComSunBean, "platform GC beans implement com.sun GCMXBean");
        // On HotSpot System.gc() reliably populates GcInfo, but keep tolerant across
        // collectors (e.g. when getLastGcInfo() has not been recorded yet).
        if (!sawNonNullGcInfo) {
            System.out.println("NOTE: getLastGcInfo() returned null for all collectors; tolerated.");
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
